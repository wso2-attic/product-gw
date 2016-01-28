/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.gateway.internal.mediation.camel;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.internal.util.uri.URITemplate;
import org.wso2.carbon.gateway.internal.util.uri.URITemplateException;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.FaultHandler;
import org.wso2.carbon.messaging.MessageProcessorException;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.TransportConstants;
import org.wso2.carbon.transport.http.netty.latency.metrics.ConnectionMetricsHolder;
import org.wso2.carbon.transport.http.netty.latency.metrics.RequestMetricsHolder;
import org.wso2.carbon.transport.http.netty.latency.metrics.ResponseMetricsHolder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 * Responsible for receive the client message and send it in to camel
 * and send back the response message to client.
 */
@SuppressWarnings("unchecked")
public class CamelMediationEngine implements CarbonMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(CamelMediationEngine.class);
    private final ConcurrentHashMap<String, CamelMediationConsumer> consumers = new ConcurrentHashMap<>();

    public CamelMediationEngine() {
    }

    /**
     * Client messages will receive here.
     *
     * @param cMsg            carbon message implementation.
     * @param requestCallback callback object to notify response is ready.
     */
    public boolean receive(CarbonMessage cMsg, CarbonCallback requestCallback) {
        //start mediation
        if (log.isDebugEnabled()) {
            log.debug("Channel: {} received body: {}");
        }
        Map<String, String> transportHeaders = cMsg.getHeaders();
        CamelMediationConsumer consumer = decideConsumer((String) cMsg.getProperty(Constants.TO),
                cMsg.getProperty(Constants.HTTP_METHOD).toString(),
                transportHeaders);
        if (consumer != null) {

            final Exchange exchange = consumer.getEndpoint().createExchange(transportHeaders, cMsg);
            cMsg.getFaultHandlerStack().push(new CamelMediationEngineFaultHandler(exchange));
            exchange.setPattern(ExchangePattern.InOut);
            //need to close the unit of work finally
            try {
                consumer.createUoW(exchange);
            } catch (Exception e) {
                String msg = "Unit of Work creation failed" + e.getMessage();
                CarbonMessage carbonMessage = CarbonCamelMessageUtil.createHttpCarbonResponse
                        (msg, 500, Constants.TEXT_PLAIN);
                requestCallback.done(carbonMessage);
            }
            processAsynchronously(exchange, consumer, requestCallback);
        } else {
            String msg = "Message consumer not found.";
            log.error(msg);
            CarbonMessage carbonMessage = CarbonCamelMessageUtil.createHttpCarbonResponse
                    (msg, 404, Constants.TEXT_PLAIN);
            requestCallback.done(carbonMessage);
        }
        return true;
    }

    /**
     * Set the transport sender for the engine implementation.
     *
     * @param transportSender Transport Sender
     */
    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public String getId() {
        return "camel-engine";
    }

    private void processAsynchronously(final Exchange exchange, final CamelMediationConsumer consumer,
                                       final CarbonCallback requestCallback) {

        consumer.getAsyncProcessor().process(exchange, done -> {

            CarbonMessage mediatedResponse = null;

            if (null == exchange.getException()) {
                Map<String, Object> mediatedHeaders = null;
                if (null != exchange.getOut().getBody()) {
                    mediatedResponse = exchange.getOut().getBody(CarbonMessage.class);
                    mediatedHeaders = exchange.getOut().getHeaders();
                } else if (null != exchange.getIn().getBody()) {
                    mediatedResponse = exchange.getIn().getBody(CarbonMessage.class);
                    mediatedHeaders = exchange.getIn().getHeaders();
                } else {
                    log.error("Error while reading the response carbon message...");
                }
                if (!mediatedHeaders.isEmpty() && mediatedResponse != null) {
                    try {
                        int statusCode = Integer.parseInt((String) mediatedHeaders.get(Exchange.HTTP_RESPONSE_CODE));
                        mediatedHeaders.remove(Exchange.HTTP_RESPONSE_CODE);
                        mediatedResponse.setProperty(Constants.HTTP_STATUS_CODE, statusCode);
                    } catch (ClassCastException classCastException) {
                        log.info("Response Http Status code is invalid. response code : " +
                                mediatedHeaders.get(Exchange.HTTP_RESPONSE_CODE));
                    }
                    mediatedHeaders.remove(Exchange.HTTP_RESPONSE_CODE);
                    mediatedResponse.removeHeader(Exchange.HTTP_RESPONSE_CODE);

                    for (Map.Entry<String, Object> entry : mediatedHeaders.entrySet()) {
                        mediatedResponse.setHeader(entry.getKey(), (String) entry.getValue());
                    }
                }

            } else {
                int code = 500;
                String contentType = Constants.TEXT_PLAIN;

                if (exchange.getProperty(Constants.HTTP_STATUS_CODE) != null) {
                    code = Integer.parseInt((String) exchange.getProperty(Constants.HTTP_STATUS_CODE));
                }

                if (exchange.getProperty(Constants.HTTP_CONTENT_TYPE) != null) {
                    contentType = (String) exchange.getProperty(Constants.HTTP_CONTENT_TYPE);
                }

                if (exchange.getProperty(Constants.HTTP_STATUS_CODE) == null && exchange.getOut() instanceof
                        CamelHttp4Message && ((CamelHttp4Message) exchange.getOut()).
                        getCarbonMessage().
                        getProperty(Constants.HTTP_STATUS_CODE) != null) {
                    String value = (String) ((CamelHttp4Message) exchange.getOut()).getCarbonMessage().
                            getProperty(Constants.HTTP_STATUS_CODE);
                    code = Integer.parseInt(value);
                }
                mediatedResponse =
                        CarbonCamelMessageUtil.createHttpCarbonResponse
                                (exchange.getException().getMessage(), code, contentType);
            }
            try {
                requestCallback.done(mediatedResponse);
            } finally {
                if (mediatedResponse != null) {
                    ResponseMetricsHolder clientResponseMetricsDataHolder = (ResponseMetricsHolder)
                            mediatedResponse.getProperty(TransportConstants.CLIENT_RESPONSE_METRICS_HOLDER);
                    ResponseMetricsHolder serverResponseMetricsDataHolder = (ResponseMetricsHolder)
                            mediatedResponse.getProperty(TransportConstants.SERVER_REQUEST_METRICS_HOLDER);
                    RequestMetricsHolder serverRequestMetricsHolder = (RequestMetricsHolder)
                            mediatedResponse.getProperty(TransportConstants.SERVER_REQUEST_METRICS_HOLDER);
                    RequestMetricsHolder clientRequestMetricsHolder = (RequestMetricsHolder)
                            mediatedResponse.getProperty(TransportConstants.CLIENT_REQUEST_METRICS_HOLDER);
                    ConnectionMetricsHolder serverConnectionMetricsHolder = (ConnectionMetricsHolder)
                            mediatedResponse.getProperty(TransportConstants.SERVER_CONNECTION_METRICS_HOLDER);
                    ConnectionMetricsHolder clientConnectionMetricsHolder = (ConnectionMetricsHolder)
                            mediatedResponse.getProperty(TransportConstants.CLIENT_CONNECTION_METRICS_HOLDER);

                    log.info("===============================================");
                    log.info("Type: " + clientResponseMetricsDataHolder.getType());
                    log.info("Response Life Time: " +
                            String.valueOf(clientResponseMetricsDataHolder.getResponseLifeTime().getCount()));
                    log.info("Response Body Read Time: " +
                            String.valueOf(clientResponseMetricsDataHolder.getResponseBodyReadTime().getCount()));
                    log.info("Response Header Read Time: " +
                            String.valueOf(clientResponseMetricsDataHolder.getResponseHeaderReadTime().getCount()));
                    log.info("===============================================\n");

                    log.info("Type: " + serverResponseMetricsDataHolder.getType());
                    log.info("Response Life Time" +
                            String.valueOf(serverResponseMetricsDataHolder.getResponseLifeTime().getCount()));
                    log.info("Response Header Read Time: " +
                            String.valueOf(serverResponseMetricsDataHolder.getResponseHeaderReadTime().getCount()));
                    log.info("Response Body Read Time: " +
                            String.valueOf(serverResponseMetricsDataHolder.getResponseBodyReadTime().getCount()));
                    log.info("===============================================\n");

                    log.info("Type: " + serverRequestMetricsHolder.getType());
                    log.info("Request Header Read Time: " +
                            String.valueOf(serverRequestMetricsHolder.getRequestHeaderReadTimer().getCount()));
                    log.info("Request Body Read Time: " +
                            String.valueOf(serverRequestMetricsHolder.getRequestBodyReadTimer().getCount()));
                    log.info("Request Read Time: " +
                            String.valueOf(serverRequestMetricsHolder.getRequestLifeTimer().getCount()));
                    log.info("===============================================\n");

                    log.info("Type: " + clientRequestMetricsHolder.getType());
                    log.info("Request Read Time: " +
                            String.valueOf(clientRequestMetricsHolder.getRequestLifeTimer().getCount()));
                    log.info("Request Header Read Time: " +
                            String.valueOf(clientRequestMetricsHolder.getRequestHeaderReadTimer().getCount()));
                    log.info("Request Body Read Time: " +
                            String.valueOf(clientRequestMetricsHolder.getRequestBodyReadTimer().getCount()));
                    log.info("===============================================\n");

                    log.info("Type: " + "Client Connection");
                    log.info("Connection Life Time: " +
                            String.valueOf(clientConnectionMetricsHolder.getConnectionTimer().getCount()));
                    log.info("===============================================\n");

                    log.info("Type: Server Connection");
                    log.info("Connection Life Time: " +
                            String.valueOf(serverConnectionMetricsHolder.getConnectionTimer().getCount()));
                    log.info("===============================================\n");
                }
                consumer.doneUoW(exchange);
            }
        });
    }

    private CamelMediationConsumer decideConsumer(String uri, String httpMethod,
                                                  Map<String, String> transportHeaders) {

        for (String consumerKey : consumers.keySet()) {
            if (!consumerKey.contains("?httpMethodRestrict=")) {
                if (uri.contains(consumerKey)) {
                    return consumers.get(consumerKey);
                }
            }
        }

        /*Processing requests to REST interfaces */
        for (String consumerKey : consumers.keySet()) {
            if (consumerKey.contains("?httpMethodRestrict=")) {
                Map<String, String> variables = new HashMap<String, String>();
                URITemplate uriTemplate = null;
                try {
                    /* Extracting the context information from registered REST consumers. */
                    String[] urlTokens = consumerKey.split(":\\d+");
                    if (urlTokens.length > 0) {
                        String consumerContextPath = urlTokens[1];
                        String decodeConsumerURI = URLDecoder.decode(consumerContextPath, "UTF-8");
                        uriTemplate = new URITemplate(decodeConsumerURI);
                        boolean isMatch = uriTemplate.matches(uri + "?httpMethodRestrict=" + httpMethod, variables);
                        if (variables.size() != 0) {
                            for (Map.Entry<String, String> entry : variables.entrySet()) {
                                transportHeaders.put(entry.getKey(), entry.getValue());
                            }
                        }
                        if (isMatch) {
                            return consumers.get(consumerKey);
                        }
                    }
                } catch (URITemplateException e) {
                    log.error("URI Template " + consumerKey + " is invalid. " + e);
                } catch (UnsupportedEncodingException e) {
                    log.error("URI Template " + consumerKey + " encoding error. " + e);
                }
            }
        }

        return null;
    }

    public void addConsumer(String key, CamelMediationConsumer consumer) {
        consumers.put(key, consumer);
    }

    public void removeConsumer(String endpointKey) {
        consumers.remove(endpointKey);
    }


    private static class CamelMediationEngineFaultHandler implements FaultHandler {

        private Exchange exchange;


        public CamelMediationEngineFaultHandler(Exchange exchange) {
            this.exchange = exchange;
        }

        @Override
        public void handleFault(String s) {

        }

        @Override
        public void handleFault() {

        }

        @Override
        public void handleFault(String s, CarbonCallback carbonCallback) {
            Throwable throwable = new MessageProcessorException(s);
            exchange.setException(throwable);
            DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
            defaultCarbonMessage.setProperty(Constants.HTTP_STATUS_CODE, "500");
            defaultCarbonMessage.setProperty(Constants.EXCHANGE, throwable);
            carbonCallback.done(defaultCarbonMessage);
        }

        @Override
        public void handleFault(String statusCode, Throwable throwable, CarbonCallback carbonCallback) {
            exchange.setException(throwable);
            DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
            defaultCarbonMessage.setProperty(Constants.HTTP_STATUS_CODE, statusCode);
            defaultCarbonMessage.setProperty(Constants.EXCHANGE, throwable);
            carbonCallback.done(defaultCarbonMessage);
        }
    }

}

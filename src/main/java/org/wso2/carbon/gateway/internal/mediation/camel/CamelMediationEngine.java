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
import org.wso2.carbon.gateway.internal.common.CarbonCallback;
import org.wso2.carbon.gateway.internal.common.CarbonMessage;
import org.wso2.carbon.gateway.internal.common.CarbonMessageProcessor;
import org.wso2.carbon.gateway.internal.common.TransportSender;
import org.wso2.carbon.gateway.internal.transport.common.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for receive the client message and send it in to camel
 * and send back the response message to client.
 */
@SuppressWarnings("unchecked") public class CamelMediationEngine implements CarbonMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(CamelMediationEngine.class);
    private final ConcurrentHashMap<String, CamelMediationConsumer> consumers = new ConcurrentHashMap<>();
    private CamelMediationConsumer consumer = null;
    private TransportSender sender;

    public CamelMediationEngine(TransportSender sender) {
        this.sender = sender;
        // this.sender.setCarbonMessageProcessor(this);
    }

    //Client messages will receive here
    public boolean receive(CarbonMessage cMsg, CarbonCallback requestCallback) {
        //start mediation
        if (log.isDebugEnabled()) {
            log.debug("Channel: {} received body: {}");
        }
        Map<String, Object> transportHeaders = (Map<String, Object>) cMsg.getProperty(Constants.TRANSPORT_HEADERS);
        CamelMediationConsumer consumer = decideConsumer(cMsg.getURI());
        if (consumer != null) {
            final Exchange exchange = consumer.getEndpoint().createExchange(transportHeaders, cMsg);
            exchange.setPattern(ExchangePattern.InOut);
            // we want to handle the UoW
            try {
                consumer.createUoW(exchange);
            } catch (Exception e) {
                log.error("Unit of Work creation failed");
            }
            processAsynchronously(exchange, consumer, requestCallback);

        }
        return true;
    }

    public TransportSender getSender() {
        return sender;
    }

    private void processAsynchronously(final Exchange exchange, final CamelMediationConsumer consumer,
                                       final CarbonCallback requestCallback) {
        consumer.getAsyncProcessor().process(exchange, done -> {

            CarbonMessage mediatedResponse = exchange.getOut().getBody(CarbonMessage.class);
            Map<String, Object> mediatedHeaders = exchange.getOut().getHeaders();
            mediatedResponse.setProperty(Constants.TRANSPORT_HEADERS, mediatedHeaders);

            try {
                requestCallback.done(mediatedResponse);
            } finally {
                consumer.doneUoW(exchange);
            }
        });
    }

    private CamelMediationConsumer decideConsumer(String uri) {
        if (consumer != null) {
            return consumer;
        }
        if (consumers.size() == 1) {
            String key = consumers.keySet().iterator().next();
            if (uri.contains(key)) {
                consumer = consumers.get(key);
                return consumer;
            }
        }
        for (String key : consumers.keySet()) {
            if (key.contains(uri)) {
                return consumers.get(key);
            }
        }
        log.info("No route found for the message URI : " + uri);
        return null;
    }

    public void addConsumer(String key, CamelMediationConsumer consumer) {
        consumers.put(key, consumer);
    }

    public void removeConsumer(String endpointKey) {
        consumers.remove(endpointKey);
    }
}

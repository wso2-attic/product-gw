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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.URISupport;
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.internal.common.CarbonGatewayConstants;
import org.wso2.carbon.gateway.internal.common.CarbonMessage;
import org.wso2.carbon.gateway.internal.common.Pipe;
import org.wso2.carbon.gateway.internal.transport.common.Constants;
import org.wso2.carbon.gateway.internal.transport.common.HTTPContentChunk;
import org.wso2.carbon.gateway.internal.transport.common.PipeImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Carbon-Camel header transformation.
 */
public class CarbonCamelMessageUtil {

    private static Logger log = LoggerFactory.getLogger(CarbonCamelMessageUtil.class);

    /**
     * Get carbon headers from client request and set in the camel exchange in message.
     *
     * @param exchange         camel exchange
     * @param transportHeaders http headers
     * @param request          http request carbon message.
     */
    public void setCamelHeadersToClientRequest(Exchange exchange, Map<String, Object> transportHeaders,
                                               CarbonMessage request) {
        ConcurrentHashMap<String, Object> headers = new ConcurrentHashMap<>();

        if (request.getProperty(Constants.HTTP_METHOD) != null) {
            headers.put(Exchange.HTTP_METHOD, request.getProperty(Constants.HTTP_METHOD));
        }
        if (request.getProperty(Constants.HTTP_VERSION) != null) {
            headers.put(Exchange.HTTP_PROTOCOL_VERSION, request.getProperty(Constants.HTTP_VERSION));
        }

        // strip query parameters from the uri
        String s = request.getURI();
        if (s.contains("?")) {
            s = ObjectHelper.before(s, "?");
        }

        // we want the full path for the url, as the client may provide the url in the HTTP headers as
        // absolute or relative, eg
        //   /foo
        //   http://servername/foo
        String http = request.getProtocol() + "://";
        if (!s.startsWith(http)) {
            s = http + transportHeaders.get(Constants.HTTP_HOST) + s;
        }

        headers.put(Exchange.HTTP_URL, s);
        // uri is without the host and port
        URI uri = null;
        try {
            uri = new URI(request.getURI());
        } catch (URISyntaxException e) {
            log.error("Could not decode the URI in the message : " + request.getURI());
        }

        if (uri != null) {
            String path = uri.getPath();
            if (path != null) {
                // uri is path and query parameters
                headers.put(Exchange.HTTP_URI, path);
                headers.put(Exchange.HTTP_PATH, path);
            }
            if (uri.getQuery() != null) {
                headers.put(Exchange.HTTP_QUERY, uri.getQuery());
            }
            if (uri.getRawQuery() != null) {
                headers.put(Exchange.HTTP_RAW_QUERY, uri.getRawQuery());
            }
        }

        if (transportHeaders.get(Constants.HTTP_CONTENT_TYPE) != null) {
            headers.put(Exchange.CONTENT_TYPE, transportHeaders.get(Constants.HTTP_CONTENT_TYPE));
        }
        if (transportHeaders.get(Constants.HTTP_SOAP_ACTION) != null) {
            headers.put(Exchange.SOAP_ACTION, transportHeaders.get(Constants.HTTP_SOAP_ACTION));
        }
        if (transportHeaders.get(Constants.HTTP_CONTENT_ENCODING) != null) {
            headers.put(Exchange.CONTENT_ENCODING, transportHeaders.get(Constants.HTTP_CONTENT_ENCODING));
        }

        Iterator it = transportHeaders.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            if (!Constants.HTTP_CONTENT_TYPE.equals(pair.getKey()) &&
                    !Constants.HTTP_SOAP_ACTION.equals(pair.getKey()) &&
                    !Constants.HTTP_CONTENT_ENCODING.equals(pair.getKey())) {
                headers.put((String) pair.getKey(), pair.getValue());
            }
            it.remove();
        }

        exchange.getIn().setHeaders(headers);
    }

    /**
     * Get camel headers from mediated request and set in carbon message.
     *
     * @param exchange camel exchange
     * @param host     endpoint host address
     * @param port     endpoint port
     * @param uri      endpoint uri
     */
    public void setCarbonHeadersToBackendRequest(Exchange exchange, String host, int port, String uri) {

        //CarbonMessage request = (CarbonMessage) exchange.getIn().getBody();
        CarbonMessage request = exchange.getIn().getBody(CarbonMessage.class);
        Map<String, Object> headers = exchange.getIn().getHeaders();

        if (request != null) {

            ConcurrentHashMap<String, Object> carbonBackEndRequestHeaders = new ConcurrentHashMap<>();

            request.setHost(host);
            request.setPort(port);

            try {
                request.setURI(createURI(exchange, uri));
            } catch (URISyntaxException e) {
                log.error("Error while generating the URL for to endpoint : " + uri);
            }

            Iterator it = headers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String key = (String) pair.getKey();
                if (key.equals(Exchange.CONTENT_TYPE)) {
                    carbonBackEndRequestHeaders.put(Constants.HTTP_CONTENT_TYPE, pair.getValue());
                } else if (key.equals(Exchange.SOAP_ACTION)) {
                    carbonBackEndRequestHeaders.put(Constants.HTTP_SOAP_ACTION, pair.getValue());
                } else if (key.equals(Exchange.CONTENT_ENCODING)) {
                    carbonBackEndRequestHeaders.put(Constants.HTTP_CONTENT_ENCODING, pair.getValue());
                } else if (key.equals(Exchange.HTTP_METHOD)) {
                    request.setProperty(Constants.HTTP_METHOD, pair.getValue());
                } else if (key.equals(Exchange.HTTP_PROTOCOL_VERSION)) {
                    request.setProperty(Constants.HTTP_VERSION, pair.getValue());
                } else if (key.equals(Exchange.CONTENT_LENGTH)) {
                    //Content has been replaced. Take the new content-length
                    if (request.getPipe().isEmpty() && (request.getPipe().getMessageBytes() != null)) {
                        carbonBackEndRequestHeaders.put(Constants.HTTP_CONTENT_LENGTH, request.getPipe()
                                .getMessageBytes().readableBytes());
                    } else {
                        carbonBackEndRequestHeaders.put(Constants.HTTP_CONTENT_LENGTH, pair.getValue());
                    }
                } else if (!key.startsWith("Camel")) {
                    carbonBackEndRequestHeaders.put(key, pair.getValue());
                }
                it.remove();
            }

            if (port != 80) {
                carbonBackEndRequestHeaders.put(Constants.HTTP_HOST, host + ":" + port);
            } else {
                carbonBackEndRequestHeaders.put(Constants.HTTP_HOST, host);
            }

            if (request.getPipe().isEmpty() && (request.getPipe().getMessageBytes() != null)) {
                if (carbonBackEndRequestHeaders.contains(Constants.HTTP_CONTENT_LENGTH)) {
                    carbonBackEndRequestHeaders.remove(Constants.HTTP_CONTENT_LENGTH);
                }
                carbonBackEndRequestHeaders.put(Constants.HTTP_CONTENT_LENGTH, request.getPipe()
                        .getMessageBytes().readableBytes());
            }

            request.setProperty(Constants.TRANSPORT_HEADERS, carbonBackEndRequestHeaders);

            //Set source handler and disruptor if they are not available
            CarbonMessage originalMessage = (CarbonMessage) exchange
                    .getProperty(CarbonGatewayConstants.ORIGINAL_MESSAGE);
            if (request.getProperty(Constants.SRC_HNDLR) == null) {
                if (originalMessage != null) {
                    request.setProperty(Constants.SRC_HNDLR, originalMessage.getProperty(Constants.SRC_HNDLR));
                }
            }

            if (request.getProperty(Constants.DISRUPTOR) == null) {
                if (originalMessage != null) {
                    request.setProperty(Constants.DISRUPTOR, originalMessage.getProperty(Constants.DISRUPTOR));
                }
            }

            //Set the Camel Message back to the exchange for sending through the producer
            CamelHttp4Message answer = new CamelHttp4Message();
            answer.setCarbonMessage(request);
            answer.setExchange(exchange);
            exchange.setIn(answer);
        }
    }

    private String createURI(Exchange exchange, String url) throws URISyntaxException {
        URI uri = new URI(url);
        String queryString = exchange.getIn().getHeader(Exchange.HTTP_QUERY, String.class);
        if (queryString == null) {
            queryString = uri.getRawQuery();
        }
        if (queryString != null) {
            // need to encode query string
            queryString = UnsafeUriCharactersEncoder.encodeHttpURI(queryString);
            uri = URISupport.createURIWithQuery(uri, queryString);
        }
        return uri.toString();
    }

    /**
     * Get carbon headers from backend response and set in camel exchange out message.
     *
     * @param exchange         camel exchange
     * @param transportHeaders backend response http headers
     */
    public void setCamelHeadersToBackendResponse(Exchange exchange, Map<String, Object> transportHeaders) {
        exchange.getOut().setHeaders(transportHeaders);
    }

    /**
     * Create Camel Message which encapsulate Carbon message.
     *
     * @param carbonMessage Carbon message
     * @param exchange      Camel Exchange.
     * @return Camel Message
     * @throws Exception
     */
    public static Message createCamelMessage(CarbonMessage carbonMessage, Exchange exchange) throws Exception {
        CamelHttp4Message answer = new CamelHttp4Message();
        answer.setCarbonMessage(carbonMessage);
        answer.setExchange(exchange);
        return answer;
    }

    /**
     * Generate Http Error Responses as a Carbon Message.
     *
     * @param errorMessage Error message in the http message body.
     * @param code         Error Code of the Http response.
     * @return Http Error Response as a Carbon message.
     */
    public static CarbonMessage createHttpCarbonResponse(String errorMessage, int code) {

        CarbonMessage response = new CarbonMessage(Constants.PROTOCOL_NAME);
        ByteBuf bbuf = Unpooled.copiedBuffer(errorMessage, StandardCharsets.UTF_8);
        DefaultLastHttpContent lastHttpContent = new DefaultLastHttpContent(bbuf);
        HTTPContentChunk contentChunk = new HTTPContentChunk(lastHttpContent);
        Pipe pipe = new PipeImpl(bbuf.readableBytes());
        pipe.addContentChunk(contentChunk);
        response.setPipe(pipe);

        response.setDirection(CarbonMessage.RESPONSE);

        Map<String, Object> transportHeaders = new HashMap<>();
        transportHeaders.put(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        transportHeaders.put(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        transportHeaders.put(HttpHeaders.Names.CONTENT_TYPE, "text/xml");
        transportHeaders.put(HttpHeaders.Names.CONTENT_LENGTH, bbuf.readableBytes());
        response.setProperty(Constants.TRANSPORT_HEADERS, transportHeaders);

        response.setProperty(Constants.HTTP_STATUS_CODE, code);

        return response;
    }
}

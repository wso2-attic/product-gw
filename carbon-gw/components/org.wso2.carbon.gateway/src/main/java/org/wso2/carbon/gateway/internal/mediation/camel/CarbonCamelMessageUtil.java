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
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.URISupport;
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.internal.common.CarbonGatewayConstants;
import org.wso2.carbon.messaging.CarbonMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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

        if (request.getProperty(CarbonGatewayConstants.HTTP_METHOD) != null) {
            headers.put(Exchange.HTTP_METHOD, request.getProperty(CarbonGatewayConstants.HTTP_METHOD));
        }
        if (request.getProperty(CarbonGatewayConstants.HTTP_VERSION) != null) {
            headers.put(Exchange.HTTP_PROTOCOL_VERSION, request.getProperty(CarbonGatewayConstants.HTTP_VERSION));
        }

        // strip query parameters from the uri
        String s = (String) request.getProperty("TO");
        if (s.contains("?")) {
            s = ObjectHelper.before(s, "?");
        }

        // we want the full path for the url, as the client may provide the url in the HTTP headers as
        // absolute or relative, eg
        //   /foo
        //   http://servername/foo
        String http = request.getProperty("PROTOCOL") + "://";
        if (!s.startsWith(http)) {
            s = http + transportHeaders.get(CarbonGatewayConstants.HTTP_HOST) + s;
        }

        headers.put(Exchange.HTTP_URL, s);
        // uri is without the host and port
        URI uri = null;
        try {
            uri = new URI((String) request.getProperty("TO"));
        } catch (URISyntaxException e) {
            log.error("Could not decode the URI in the message : " + request.getProperty("TO"));
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

        if (transportHeaders.get(CarbonGatewayConstants.HTTP_CONTENT_TYPE) != null) {
            headers.put(Exchange.CONTENT_TYPE, transportHeaders.get(CarbonGatewayConstants.HTTP_CONTENT_TYPE));
        }
        if (transportHeaders.get(CarbonGatewayConstants.HTTP_SOAP_ACTION) != null) {
            headers.put(Exchange.SOAP_ACTION, transportHeaders.get(CarbonGatewayConstants.HTTP_SOAP_ACTION));
        }
        if (transportHeaders.get(CarbonGatewayConstants.HTTP_CONTENT_ENCODING) != null) {
            headers.put(Exchange.CONTENT_ENCODING, transportHeaders.get(CarbonGatewayConstants.HTTP_CONTENT_ENCODING));
        }

        Iterator it = transportHeaders.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            if (!CarbonGatewayConstants.HTTP_CONTENT_TYPE.equals(pair.getKey()) &&
                    !CarbonGatewayConstants.HTTP_SOAP_ACTION.equals(pair.getKey()) &&
                    !CarbonGatewayConstants.HTTP_CONTENT_ENCODING.equals(pair.getKey())) {
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

        CarbonMessage request = exchange.getIn().getBody(CarbonMessage.class);
        Map<String, Object> headers = exchange.getIn().getHeaders();

        if (request != null) {

            ConcurrentHashMap<String, Object> carbonBackEndRequestHeaders = new ConcurrentHashMap<>();

            request.setProperty("HOST", host);
            request.setProperty("PORT", port);

            try {
                request.setProperty("TO", createURI(exchange, uri));
            } catch (URISyntaxException e) {
                log.error("Error while generating the URL for to endpoint : " + uri);
            }

            Iterator it = headers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String key = (String) pair.getKey();
                if (key.equals(Exchange.CONTENT_TYPE)) {
                    carbonBackEndRequestHeaders.put(CarbonGatewayConstants.HTTP_CONTENT_TYPE, pair.getValue());
                } else if (key.equals(Exchange.SOAP_ACTION)) {
                    carbonBackEndRequestHeaders.put(CarbonGatewayConstants.HTTP_SOAP_ACTION, pair.getValue());
                } else if (key.equals(Exchange.CONTENT_ENCODING)) {
                    carbonBackEndRequestHeaders.put(CarbonGatewayConstants.HTTP_CONTENT_ENCODING, pair.getValue());
                } else if (key.equals(Exchange.HTTP_METHOD)) {
                    request.setProperty(CarbonGatewayConstants.HTTP_METHOD, pair.getValue());
                } else if (key.equals(Exchange.HTTP_PROTOCOL_VERSION)) {
                    request.setProperty(CarbonGatewayConstants.HTTP_VERSION, pair.getValue());
                } else if (!key.startsWith("Camel")) {
                    carbonBackEndRequestHeaders.put(key, pair.getValue());
                }
                it.remove();
            }

            if (port != 80) {
                carbonBackEndRequestHeaders.put(CarbonGatewayConstants.HTTP_HOST, host + ":" + port);
            } else {
                carbonBackEndRequestHeaders.put(CarbonGatewayConstants.HTTP_HOST, host);
            }

            request.setProperty(CarbonGatewayConstants.TRANSPORT_HEADERS, carbonBackEndRequestHeaders);
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

        CarbonMessage response = new CarbonMessage();
        byte[] errorMessageBytes = errorMessage.getBytes(Charset.defaultCharset());
        response.setMessageBody(errorMessageBytes);
        response.setProperty("DIRECTION", "response");

        Map<String, Object> transportHeaders = new HashMap<>();
        // TODO: 12/8/15 introduce a constants
        transportHeaders.put("Connection", "keep-alive");
        transportHeaders.put("Accept-Encoding", "gzip");
        transportHeaders.put("Content-Type", "text/xml");
        transportHeaders.put("Content-Length", errorMessageBytes.length);
        response.setProperty(CarbonGatewayConstants.TRANSPORT_HEADERS, transportHeaders);

        response.setProperty(CarbonGatewayConstants.HTTP_STATUS_CODE, code);

        return response;
    }

}

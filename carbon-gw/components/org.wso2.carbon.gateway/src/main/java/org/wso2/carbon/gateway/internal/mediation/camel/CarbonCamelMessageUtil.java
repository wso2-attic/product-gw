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
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

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
    public void setCamelHeadersToClientRequest(Exchange exchange, Map<String, String> transportHeaders,
                                               CarbonMessage request) {
        ConcurrentHashMap<String, Object> headers = new ConcurrentHashMap<>();

        if (request.getProperty(Constants.HTTP_METHOD) != null) {
            headers.put(Exchange.HTTP_METHOD, request.getProperty(Constants.HTTP_METHOD));
        }
        if (request.getProperty(Constants.HTTP_VERSION) != null) {
            headers.put(Exchange.HTTP_PROTOCOL_VERSION, request.getProperty(Constants.HTTP_VERSION));
        }

        // strip query parameters from the uri
        String s = (String) request.getProperty(Constants.TO);
        if (s.contains("?")) {
            s = ObjectHelper.before(s, "?");
        }

        // we want the full path for the url, as the client may provide the url in the HTTP headers as
        // absolute or relative, eg
        //   /foo
        //   http://servername/foo
        String http = request.getProperty(Constants.PROTOCOL) + "://";
        if (!s.startsWith(http)) {
            s = http + transportHeaders.get(Constants.HTTP_HOST) + s;
        }

        headers.put(Exchange.HTTP_URL, s);
        // uri is without the host and port
        URI uri = null;
        try {
            uri = new URI((String) request.getProperty(Constants.TO));
        } catch (URISyntaxException e) {
            log.error("Could not decode the URI in the message : " + request.getProperty(Constants.TO));
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

        CarbonMessage request = exchange.getIn().getBody(CarbonMessage.class);
        Map<String, Object> headers = exchange.getIn().getHeaders();

        if (request != null) {

            ConcurrentHashMap<String, String> carbonBackEndRequestHeaders = new ConcurrentHashMap<>();

            request.setProperty(Constants.HOST, host);
            request.setProperty(Constants.PORT, port);

            try {
                request.setProperty(Constants.TO, createURI(exchange, uri));
            } catch (URISyntaxException e) {
                log.error("Error while generating the URL for to endpoint : " + uri);
            }

            Iterator it = headers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String key = (String) pair.getKey();
                if (key.equals(Exchange.CONTENT_TYPE)) {
                    carbonBackEndRequestHeaders.put(Constants.HTTP_CONTENT_TYPE,
                            pair.getValue().toString());
                } else if (key.equals(Exchange.SOAP_ACTION)) {
                    carbonBackEndRequestHeaders.put(Constants.HTTP_SOAP_ACTION,
                            pair.getValue().toString());
                } else if (key.equals(Exchange.CONTENT_ENCODING)) {
                    carbonBackEndRequestHeaders.put(Constants.HTTP_CONTENT_ENCODING,
                            pair.getValue().toString());
                } else if (key.equals(Exchange.HTTP_METHOD)) {
                    request.setProperty(Constants.HTTP_METHOD, pair.getValue());
                } else if (key.equals(Exchange.HTTP_PROTOCOL_VERSION)) {
                    request.setProperty(Constants.HTTP_VERSION, pair.getValue());
                } else if (!key.startsWith("Camel")) {
                    carbonBackEndRequestHeaders.put(key, pair.getValue().toString());
                }
                it.remove();
            }

            if (port != 80) {
                carbonBackEndRequestHeaders.put(Constants.HTTP_HOST, host + ":" + port);
            } else {
                carbonBackEndRequestHeaders.put(Constants.HTTP_HOST, host);
            }

            request.setHeaders(carbonBackEndRequestHeaders);
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
    public void setCamelHeadersToBackendResponse(Exchange exchange, Map<String, String> transportHeaders) {
        transportHeaders.forEach((k, v) -> exchange.getOut().setHeader(k, v));
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

        DefaultCarbonMessage response = new DefaultCarbonMessage();
        response.setStringMessageBody(errorMessage);
        byte[] errorMessageBytes = errorMessage.getBytes(Charset.defaultCharset());

        Map<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(Constants.HTTP_CONNECTION, Constants.KEEP_ALIVE);
        transportHeaders.put(Constants.HTTP_CONTENT_ENCODING, Constants.GZIP);
        transportHeaders.put(Constants.HTTP_CONTENT_TYPE, Constants.TEXT_XML);
        transportHeaders.put(Constants.HTTP_CONTENT_LENGTH, (String.valueOf(errorMessageBytes.length)));

        response.setHeaders(transportHeaders);

        response.setProperty(Constants.HTTP_STATUS_CODE, code);
        response.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);

        return response;
    }

}

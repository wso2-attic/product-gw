/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.gateway.internal.transport.common;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.wso2.carbon.gateway.internal.common.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Includes utility methods for creating http requests and responses and their related properties
 */
public class Util {

    private static final String DEFAULT_HTTP_METHOD_POST = "POST";
    private static final String DEFAULT_VERSION_HTTP_1_1 = "HTTP/1.1";

    public static Map<String, String> getHeaders(HttpMessage message) {
        Map<String, String> headers = new HashMap<>();
        if (message.headers() != null) {
            for (String k : message.headers().names()) {
                headers.put(k, message.headers().get(k));
            }
        }

        return headers;
    }

    public static void setHeaders(HttpMessage message, Map<String, String> headers) {
        HttpHeaders httpHeaders = message.headers();
        for (Map.Entry<String, String> e : headers.entrySet()) {
            httpHeaders.add(e.getKey(), e.getValue());
        }
    }

    public static String getStringValue(CarbonMessage msg, String key, String defaultValue) {
        String value = (String) msg.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public static int getIntValue(CarbonMessage msg, String key, int defaultValue) {
        Integer value = (Integer) msg.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public static HttpResponse createHttpResponse(CarbonMessage msg) {
        HttpVersion httpVersion = new HttpVersion(Util.getStringValue(msg,
                Constants.HTTP_VERSION, HTTP_1_1.text()), true);

        int statusCode = Util.getIntValue(msg, Constants.HTTP_STATUS_CODE, 200);

        HttpResponseStatus httpResponseStatus = new HttpResponseStatus(statusCode,
                HttpResponseStatus.valueOf(statusCode).reasonPhrase());

        DefaultHttpResponse outgoingResponse = new DefaultHttpResponse(httpVersion,
                httpResponseStatus, false);

        Map<String, String> headerMap = (Map) msg.getProperty(Constants.TRANSPORT_HEADERS);

        Util.setHeaders(outgoingResponse, headerMap);

        return outgoingResponse;
    }

    @SuppressWarnings("unchecked")
    public static HttpRequest createHttpRequest(CarbonMessage msg) {
        HttpMethod httpMethod;
        if (null != msg.getProperty(Constants.HTTP_METHOD)) {
            httpMethod = new HttpMethod((String) msg.getProperty(Constants.HTTP_METHOD));
        } else {
            httpMethod = new HttpMethod(DEFAULT_HTTP_METHOD_POST);
        }
        HttpVersion httpVersion;
        if (null != msg.getProperty(Constants.HTTP_VERSION)) {
            httpVersion = new HttpVersion((String) msg.getProperty(Constants.HTTP_VERSION), true);
        } else {
            httpVersion = new HttpVersion(DEFAULT_VERSION_HTTP_1_1, true);
        }
        HttpRequest outgoingRequest = new DefaultHttpRequest(httpVersion, httpMethod, msg.getURI(), false);
        Map headers = (Map) msg.getProperty(Constants.TRANSPORT_HEADERS);
        Util.setHeaders(outgoingRequest, headers);
        return outgoingRequest;
    }
}

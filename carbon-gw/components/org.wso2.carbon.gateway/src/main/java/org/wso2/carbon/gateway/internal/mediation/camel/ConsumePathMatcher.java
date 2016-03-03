/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.internal.mediation.camel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.internal.util.uri.URITemplate;
import org.wso2.carbon.gateway.internal.util.uri.URITemplateException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for matching the path.
 */
public class ConsumePathMatcher {

    private static final Logger log = LoggerFactory.getLogger(ConsumePathMatcher.class);

    /**
     * Does the incoming request match the given consumer path (ignore case).
     *
     * @param requestPath      the incoming request context path
     * @param consumerPath     a consumer path
     * @param matchOnUriPrefix whether to use the matchOnPrefix option
     * @return <tt>true</tt> if matched, <tt>false</tt> otherwise
     */
    private static boolean matchPath(String requestPath, String consumerPath, boolean matchOnUriPrefix) {
        // deal with null parameters
        if (requestPath == null && consumerPath == null) {
            return true;
        }
        if (requestPath == null || consumerPath == null) {
            return false;
        }

        String p1 = requestPath.toLowerCase(Locale.ENGLISH);
        String p2 = consumerPath.toLowerCase(Locale.ENGLISH);

        if (p1.equals(p2)) {
            return true;
        }

        if (matchOnUriPrefix && p1.startsWith(p2)) {
            return true;
        }

        return false;
    }

    /**
     * Finds the best matching of the list of consumer paths that should service the incoming request.
     *
     * @param requestMethod    the incoming request HTTP method
     * @param requestPath      the incoming request context path
     * @param consumerMap      the map of consumers
     * @param transportHeaders transport header map
     * @return the best matched consumer, or <tt>null</tt> if none could be determined.
     */
    public static String matchBestPath(String requestMethod, String requestPath,
                                       Map<String, CamelMediationConsumer> consumerMap,
                                       Map<String, String> transportHeaders) {
        String answer = null;

        List<String> candidates = new ArrayList<>();

        //first match by http method
        for (Map.Entry<String, CamelMediationConsumer> consumer : consumerMap.entrySet()) {
            if (matchRestMethod(requestMethod, consumer.getValue().getEndpoint().getHttpMethodRestrict())) {
                candidates.add(consumer.getKey());
            }
        }

        // then see if we got a direct match
        Iterator<String> it = candidates.iterator();
        while (it.hasNext()) {
            String consumer = it.next();
            if (matchRestPath(requestPath, consumer, false, requestMethod, transportHeaders)) {
                answer = consumer;
                break;
            }
        }

        try {

            for (Map.Entry entry : consumerMap.entrySet()) {
                String key = (String) entry.getKey();
                String decodedPath = URLDecoder.decode(key, "UTF-8");
                if (wildCardMatch(requestPath, decodedPath, transportHeaders)) {
                    return key;
                }

            }
        } catch (UnsupportedEncodingException e) {
            log.error("Exception occured while processing request headers", e);
            return null;
        }


        // then match by wildcard path
        if (answer == null) {
            it = candidates.iterator();
            while (it.hasNext()) {
                String consumer = it.next();
                // filter non matching paths
                if (!matchRestPath(requestPath, consumer, true, requestMethod, transportHeaders)) {
                    it.remove();
                }
            }

            // if there is multiple candidates with wildcards then pick anyone with the least number of wildcards
            int bestWildcard = Integer.MAX_VALUE;
            String best = null;
            if (candidates.size() > 1) {
                it = candidates.iterator();
                while (it.hasNext()) {
                    String entry = it.next();
                    int wildcards = countWildcards(entry);
                    if (wildcards > 0) {
                        if (best == null || wildcards < bestWildcard) {
                            best = entry;
                            bestWildcard = wildcards;
                        }
                    }
                }

                if (best != null) {
                    // pick the best among the wildcards
                    answer = best;
                }
            }

            // if there is one left then its our answer
            if (answer == null && candidates.size() == 1) {
                answer = candidates.get(0);
            }
        }

        return answer;
    }

    /**
     * Matches the given request HTTP method with the configured HTTP method of the consumer.
     *
     * @param method   the request HTTP method
     * @param restrict the consumer configured HTTP restrict method
     * @return <tt>true</tt> if matched, <tt>false</tt> otherwise
     */
    private static boolean matchRestMethod(String method, String restrict) {
        if (restrict == null) {
            return true;
        }

        return restrict.toLowerCase(Locale.ENGLISH).contains(method.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Matches the given request path with the configured consumer path.
     *
     * @param requestPath  the request path
     * @param consumerPath the consumer path which may use { } tokens
     * @return <tt>true</tt> if matched, <tt>false</tt> otherwise
     */
    private static boolean matchRestPath(String requestPath, String consumerPath, boolean wildcard, String httpMethod,
                                         Map<String, String> transportHeaders) {

        if (consumerPath.contains("?httpMethodRestrict=")) {
            Map<String, String> variables = new HashMap<>();
            URITemplate uriTemplate = null;
            try {
                    /* Extracting the context information from registered REST consumers. */
                String[] urlTokens = consumerPath.split(":\\d+");
                if (urlTokens.length > 0) {
                    String consumerContextPath = urlTokens[1];
                    String decodeConsumerURI = URLDecoder.decode(consumerContextPath, "UTF-8");
                    uriTemplate = new URITemplate(decodeConsumerURI);
                    boolean isMatch = uriTemplate.matches(requestPath + "?httpMethodRestrict=" + httpMethod, variables);
                    if (variables.size() != 0) {
                        for (Map.Entry<String, String> entry : variables.entrySet()) {
                            transportHeaders.put(entry.getKey(), entry.getValue());
                        }
                    }
                    return isMatch;
                }
            } catch (URITemplateException e) {
                log.error("URI Template " + consumerPath + " is invalid. " + e);
            } catch (UnsupportedEncodingException e) {
                log.error("URI Template " + consumerPath + " encoding error. " + e);
            }
        }

        // remove starting/ending slashes
        if (requestPath.startsWith("/")) {
            requestPath = requestPath.substring(1);
        }
        if (requestPath.endsWith("/")) {
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        }
        // remove starting/ending slashes
        if (consumerPath.startsWith("/")) {
            consumerPath = consumerPath.substring(1);
        }
        if (consumerPath.endsWith("/")) {
            consumerPath = consumerPath.substring(0, consumerPath.length() - 1);
        }

        // split using single char / is optimized in the jdk
        String[] requestPaths = requestPath.split("/");
        String[] consumerPaths = consumerPath.split("/");

        // must be same number of path's
        if (requestPaths.length != consumerPaths.length) {
            return false;
        }

        for (int i = 0; i < requestPaths.length; i++) {
            String p1 = requestPaths[i];
            String p2 = consumerPaths[i];

            if (wildcard && (p2.startsWith("{") || p2.startsWith("%7B")) && (p2.endsWith("}") || p2.endsWith("%7D"))) {
                // always matches
                continue;
            }

            if (!matchPath(p1, p2, false)) {
                return false;
            }
        }

        // assume matching
        return true;
    }

    /**
     * Counts the number of wildcards in the path.
     *
     * @param consumerPath the consumer path which may use { } tokens
     * @return number of wildcards, or <tt>0</tt> if no wildcards
     */
    private static int countWildcards(String consumerPath) {
        int wildcards = 0;

        // remove starting/ending slashes
        if (consumerPath.startsWith("/")) {
            consumerPath = consumerPath.substring(1);
        }
        if (consumerPath.endsWith("/")) {
            consumerPath = consumerPath.substring(0, consumerPath.length() - 1);
        }

        String[] consumerPaths = consumerPath.split("/");
        for (String p2 : consumerPaths) {
            if (p2.startsWith("{") && p2.endsWith("}")) {
                wildcards++;
            }
        }

        return wildcards;
    }


    private static boolean wildCardMatch(String requestPath, String decodedPath, Map<String, String> map)
               throws UnsupportedEncodingException {
        String[] requestPaths = requestPath.trim().split("/");
        //   Map<String, String> candidateMap = new HashMap<>();


        if (decodedPath.contains("{") && decodedPath.contains("}")) {
            String[] consumerPath = decodedPath.split("/");
            for (int i = 1; i < consumerPath.length; i++) {
                if (!consumerPath[i].startsWith("{")) {
                    if (!consumerPath[i].equals(requestPaths[i])) {
                        return false;
                    }
                } else {
                    String param = consumerPath[i].substring(consumerPath[i].indexOf("{") + 1,
                                                             consumerPath[i].indexOf("}"));
                    if (i == (consumerPath.length - 1)) {
                        // candidateMap.put(consumerPath[i],requestPath.substring
                        // (requestPath.indexOf(consumerPath[i])));
                        String paramValue = requestPath.substring(requestPath.indexOf(requestPaths[i]) +
                                                                  requestPaths[i].length() + 1);
                        map.put(param, paramValue);

                        return true;
                    } else {

                        map.put(param, requestPaths[i]);
                        String val = requestPath.substring(requestPath.indexOf(requestPaths[i]));
                        return wildCardMatch(val, decodedPath.substring(decodedPath.indexOf(consumerPath[i])), map);
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }
}

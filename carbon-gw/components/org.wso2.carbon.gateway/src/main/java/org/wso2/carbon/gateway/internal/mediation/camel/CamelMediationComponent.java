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

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.spi.RestConfiguration;
import org.apache.camel.spi.RestConsumerFactory;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.HostUtils;
import org.wso2.carbon.gateway.internal.common.TransportSender;
import org.wso2.carbon.gateway.internal.transport.sender.NettySender;
import org.wso2.carbon.gateway.internal.transport.sender.channel.pool.ConnectionManager;

import java.util.Locale;
import java.util.Map;

/**
 * Represents the component that manages {@link CamelMediationEndpoint}.
 */
public class CamelMediationComponent extends DefaultComponent implements RestConsumerFactory {

    private CamelMediationEngine engine;
    private ConnectionManager connectionManager;
    private int queueSize = 32544;

    public CamelMediationComponent() {
        NettySender.Config config = new NettySender.Config("netty-gw-sender").setQueueSize(this.queueSize);
        connectionManager = ConnectionManager.getInstance();
        TransportSender sender = new NettySender(config, connectionManager);
        engine = new CamelMediationEngine(sender);
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new CamelMediationEndpoint(uri, this, engine);
        setProperties(endpoint, parameters);
        return endpoint;
    }

    public Consumer createConsumer(CamelContext camelContext, Processor processor, String verb, String basePath,
                                   String uriTemplate, String consumes, String produces,
                                   RestConfiguration restConfiguration, Map<String, Object> parameters)
            throws Exception {
        String scheme = "http";

        String url = "wso2-gw:%s://%s:%s/%s?httpMethodRestrict=%s";
        String host = HostUtils.getLocalHostName();
        int port = 0;

        // if no explicit port/host configured, then use port from rest configuration
        RestConfiguration config = getCamelContext().getRestConfiguration();
        if (config.getComponent() == null || config.getComponent().equals("wso2-gw")) {
            if (config.getScheme() != null) {
                scheme = config.getScheme();
            }
            if (config.getHost() != null) {
                host = config.getHost();
            }
            int num = config.getPort();
            if (num > 0) {
                port = num;
            }
        }

        String path = basePath;
        if (uriTemplate != null) {
            // make sure to avoid double slashes
            if (uriTemplate.startsWith("/")) {
                path = path + uriTemplate;
            } else {
                path = path + "/" + uriTemplate;
            }
        }
        path = FileUtil.stripLeadingSeparator(path);

        String restrict = verb.toUpperCase(Locale.US);

        url = String.format(url, scheme, host, port, path, restrict);

        CamelMediationEndpoint camelMediationEndpoint = camelContext.getEndpoint(url, CamelMediationEndpoint.class);
        Consumer consumer = camelMediationEndpoint.createConsumer(processor);
        return consumer;
    }

    public CamelMediationEngine getEngine() {
        return engine;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}

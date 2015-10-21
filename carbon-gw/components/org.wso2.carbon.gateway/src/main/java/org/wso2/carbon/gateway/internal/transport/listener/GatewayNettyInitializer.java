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

package org.wso2.carbon.gateway.internal.transport.listener;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.apache.log4j.Logger;
import org.wso2.carbon.gateway.internal.mediation.camel.CamelMediationComponent;
import org.wso2.carbon.gateway.internal.mediation.camel.CamelMediationEngine;
import org.wso2.carbon.gateway.internal.transport.common.Constants;
import org.wso2.carbon.gateway.internal.transport.common.disruptor.config.DisruptorConfig;
import org.wso2.carbon.gateway.internal.transport.common.disruptor.config.DisruptorFactory;
import org.wso2.carbon.gateway.internal.transport.sender.channel.BootstrapConfiguration;
import org.wso2.carbon.gateway.internal.transport.sender.channel.pool.ConnectionManager;
import org.wso2.carbon.gateway.internal.transport.sender.channel.pool.PoolConfiguration;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * A class that responsible for create server side channels.
 */
public class GatewayNettyInitializer implements CarbonNettyServerInitializer {

    private static final Logger log = Logger.getLogger(GatewayNettyInitializer.class);
    private int queueSize = 32544;
    private ConnectionManager connectionManager;

    public static final String CAMEL_ROUTING_CONFIG_FILE = "repository" + File.separator + "conf" + File.separator +
                                                           "camel" + File.separator + "camel-routing.xml";

    public GatewayNettyInitializer() {

    }

    @Override
    public void setup(Map<String, String> parameters) {

        BootstrapConfiguration.createBootStrapConfiguration(parameters);
        PoolConfiguration.createPoolConfiguration(parameters);

        CamelContext context = new DefaultCamelContext();
        context.disableJMX();
        context.addComponent("wso2-gw", new CamelMediationComponent());

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(CAMEL_ROUTING_CONFIG_FILE);
            RoutesDefinition routes = context.loadRoutesDefinition(fis);
            context.addRouteDefinitions(routes.getRoutes());
            context.start();
        } catch (Exception e) {
            String msg = "Error while loading " + CAMEL_ROUTING_CONFIG_FILE + " configuration file";
            throw new RuntimeException(msg, e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("No Connection to close", e);
                }
            }
        }

        CamelMediationComponent component = (CamelMediationComponent) context.getComponent("wso2-gw");
        CamelMediationEngine engine = component.getEngine();
        connectionManager = component.getConnectionManager();

        if (parameters != null) {
            DisruptorConfig disruptorConfig =
                    new DisruptorConfig(parameters.get(Constants.DISRUPTOR_BUFFER_SIZE),
                                        parameters.get(Constants.DISRUPTOR_COUNT),
                                        parameters.get(Constants.DISRUPTOR_EVENT_HANDLER_COUNT),
                                        parameters.get(Constants.WAIT_STRATEGY),
                                        Boolean.parseBoolean(Constants.SHARE_DISRUPTOR_WITH_OUTBOUND));
            DisruptorFactory.createDisruptors(DisruptorFactory.DisruptorType.INBOUND, disruptorConfig, engine);
            String queueSize = parameters.get(Constants.CONTENT_QUEUE_SIZE);
            if (queueSize != null) {
                this.queueSize = Integer.parseInt(queueSize);
            }
        } else {
            log.warn("Disruptor specific parameters are not specified in configuration hence using default configs");
            DisruptorConfig disruptorConfig = new DisruptorConfig();
            DisruptorFactory.createDisruptors(DisruptorFactory.DisruptorType.INBOUND, disruptorConfig, engine);
        }


    }

    @Override
    public void initChannel(SocketChannel ch) {
        if (log.isDebugEnabled()) {
            log.info("Initializing source channel pipeline");
        }
        ChannelPipeline p = ch.pipeline();
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("encoder", new HttpResponseEncoder());
        try {
            p.addLast("handler", new SourceHandler(queueSize, connectionManager));
        } catch (Exception e) {
            log.error("Cannot Create SourceHandler ", e);
        }
    }


}

/*
 * *
 *  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.gw.emulator.http.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.apache.log4j.Logger;
import org.wso2.gw.emulator.dsl.EmulatorType;
import org.wso2.gw.emulator.dsl.Protocol;
import org.wso2.gw.emulator.http.ChannelPipelineInitializer;
import org.wso2.gw.emulator.http.client.contexts.*;
import org.wso2.gw.emulator.http.client.processors.HttpRequestInformationProcessor;

import java.util.Map;

public class HttpClientInitializer {

    private HttpClientInformationContext clientInformationContext;
    private static final Logger log = Logger.getLogger(HttpClientInitializer.class);
    private EventLoopGroup group;
    private Bootstrap bootstrap;

    public HttpClientInitializer(HttpClientInformationContext clientInformationContext) {
        this.clientInformationContext = clientInformationContext;

    }

    public void initialize() throws Exception {

        SslContext sslCtx = null;


        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        ChannelPipelineInitializer channelPipelineInitializer = new ChannelPipelineInitializer(sslCtx,
                                                                                               EmulatorType.HTTP_CLIENT,null);
        channelPipelineInitializer.setClientInformationContext(clientInformationContext);
        bootstrap.group(group).channel(NioSocketChannel.class).handler(channelPipelineInitializer);

        for (Map.Entry<HttpClientRequestBuilderContext, HttpClientResponseBuilderContext> entry :
                clientInformationContext.getRequestResponseCorrelation().entrySet()) {
            clientInformationContext.setExpectedResponse(entry.getValue());
            HttpClientRequestProcessorContext httpClientRequestProcessorContext = new
                    HttpClientRequestProcessorContext();
            httpClientRequestProcessorContext.setRequestBuilderContext(entry.getKey());
            httpClientRequestProcessorContext.setClientInformationContext(clientInformationContext);
            sendMessage(httpClientRequestProcessorContext);
        }
        shutdown();
    }

    private void sendMessage(HttpClientRequestProcessorContext httpClientProcessorContext) throws Exception {
        new HttpRequestInformationProcessor().process(httpClientProcessorContext);
            HttpClientConfigBuilderContext clientConfigBuilderContext = httpClientProcessorContext.getClientInformationContext()
                    .getClientConfigBuilderContext();
        if (clientConfigBuilderContext.getHost() != null && clientConfigBuilderContext.getPort() != 0) {
            Channel ch = bootstrap.connect(clientConfigBuilderContext.getHost(), clientConfigBuilderContext.getPort()).sync().channel();
            ch.isWritable();
            ch.writeAndFlush(httpClientProcessorContext.getRequest());
            ch.closeFuture().sync();
        }
        else{
            if (clientConfigBuilderContext.getHost() == null) {
                try {
                    throw new Exception("Host is not given");
                } catch (Exception e) {
                    log.info(e);
                    System.exit(0);
                }
            }
            else {
                try {
                    throw new Exception("Port is not given");
                } catch (Exception e) {
                    log.info(e);
                    System.exit(0);
                }
            }
        }
    }

    public void shutdown() {
        group.shutdownGracefully();
    }
}

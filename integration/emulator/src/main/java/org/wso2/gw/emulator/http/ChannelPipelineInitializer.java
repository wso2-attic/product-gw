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

package org.wso2.gw.emulator.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.wso2.gw.emulator.dsl.EmulatorType;
import org.wso2.gw.emulator.http.client.contexts.HttpClientInformationContext;
import org.wso2.gw.emulator.http.client.handler.HttpClientHandler;
import org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext;
import org.wso2.gw.emulator.http.server.contexts.MockServerThread;
import org.wso2.gw.emulator.http.server.handler.HttpChunkedWriteHandler;
import org.wso2.gw.emulator.http.server.handler.HttpServerHandler;
import org.wso2.gw.emulator.http.server.contexts.HttpServerInformationContext;

import javax.net.ssl.SSLContext;
import java.io.File;

public class ChannelPipelineInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext sslCtx;
    private EmulatorType emulatorType;
    private HttpServerInformationContext serverInformationContext;
    private HttpClientInformationContext clientInformationContext;
    private MockServerThread[] handlers;

    public ChannelPipelineInitializer(SslContext sslCtx, EmulatorType emulatorType, MockServerThread[] handlers) {
        this.sslCtx = sslCtx;
        this.emulatorType = emulatorType;
        this.handlers = handlers;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        if(EmulatorType.HTTP_SERVER.equals(emulatorType)) {
            initializeHttpServerChannel(ch);
        } else if(EmulatorType.HTTP_CLIENT.equals(emulatorType)) {
            initializeHttpClientChannel(ch);
        }
    }

    private void initializeHttpServerChannel(SocketChannel ch) {

       /* SSLConfig sslConfig = new SSLConfig(new File("/home/dilshank/A-Certificate/emulator.jks"),"abc123","abc123",new File("/home/dilshank/A-Certificate/clientTrustStore"),"abc123");
        SslHandler sslHandler = new SSLHandlerFactory(sslConfig).create();*/

        ChannelPipeline pipeline = ch.pipeline();

        /*if (sslHandler != null) {
            pipeline.addLast("sslHandler", sslHandler);
        }*/
        pipeline.addLast(new HttpServerCodec());
        //pipeline.addLast(new HttpContentDecompressor());
        pipeline.addLast(new HttpChunkedWriteHandler(serverInformationContext));
        ///////////////////////
        HttpServerHandler httpServerHandler = new HttpServerHandler(serverInformationContext);
        httpServerHandler.setHandlers(handlers);
        //////////////////////


        pipeline.addLast("httpResponseHandler", httpServerHandler);
        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));


    }

    private void initializeHttpClientChannel(SocketChannel ch) {

        ChannelPipeline pipeline = ch.pipeline();
        /*SSLConfig sslConfig = new SSLConfig(new File("/home/dilshank/A-Certificate/emulator.jks"),"abc123","abc123",new File("/home/dilshank/A-Certificate/clientTrustStore"),"abc123");
        SslHandler sslHandler = new SSLHandlerFactory(sslConfig).create();
        sslHandler.engine().setUseClientMode(true);*/

        // Enable HTTPS if necessary.
        /*if (sslHandler != null) {
            pipeline.addLast("ssl",sslHandler);
        }*/

        pipeline.addLast(new HttpClientCodec());
        //pipeline.addLast(new HttpContentDecompressor());

        pipeline.addLast(new HttpClientHandler(clientInformationContext));


        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
    }

    public void setServerInformationContext(HttpServerInformationContext serverInformationContext) {
        this.serverInformationContext = serverInformationContext;
    }

    public void setClientInformationContext(HttpClientInformationContext clientInformationContext) {
        this.clientInformationContext = clientInformationContext;
    }
}

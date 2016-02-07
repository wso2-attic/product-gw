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

package org.wso2.gw.emulator.http.server.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;
import org.wso2.gw.emulator.http.server.contexts.*;
import org.wso2.gw.emulator.http.server.processors.*;
import org.wso2.gw.emulator.http.server.contexts.HttpServerProcessorContext;
import org.wso2.gw.emulator.http.server.contexts.HttpRequestContext;
import org.wso2.gw.emulator.http.server.contexts.HttpServerInformationContext;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = Logger.getLogger(HttpServerHandler.class);
    private HttpRequestInformationProcessor httpRequestInformationProcessor;
    private HttpResponseProcessor httpResponseProcessor;
    private HttpServerInformationContext serverInformationContext;
    private HttpServerProcessorContext httpProcessorContext;
    private HttpRequestResponseMatchingProcessor requestResponseMatchingProcessor;
    private ScheduledExecutorService scheduledReadingExecutorService, scheduledLogicExecutorService;
    private int index, corePoolSize = 10;
    private int DELAY = 0;
    private MockServerThread[] handlers;
    private static ExecutorService executorService = Executors.newFixedThreadPool(160);

    public HttpServerHandler(HttpServerInformationContext serverInformationContext) {
        this.serverInformationContext = serverInformationContext;
        scheduledReadingExecutorService = Executors.newScheduledThreadPool(corePoolSize);
        scheduledLogicExecutorService = Executors.newScheduledThreadPool(corePoolSize);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        randomIndexGenerator(serverInformationContext.getServerConfigBuilderContext().isRandomConnectionClose());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        if (msg instanceof HttpRequest) {
            randomConnectionClose(ctx, this.index, 0);
            this.httpRequestInformationProcessor = new HttpRequestInformationProcessor();
            this.httpResponseProcessor = new HttpResponseProcessor();
            this.httpProcessorContext = new HttpServerProcessorContext();
            this.httpProcessorContext.setHttpRequestContext(new HttpRequestContext());
            this.httpProcessorContext.setServerInformationContext(serverInformationContext);

            HttpRequest httpRequest = (HttpRequest) msg;
            this.httpProcessorContext.setHttpRequest(httpRequest);

            if (HttpHeaders.is100ContinueExpected(httpRequest)) {
                send100Continue(ctx);
            }
            httpRequestInformationProcessor.process(httpProcessorContext);

        } else {
            readingDelay(serverInformationContext.getServerConfigBuilderContext().getReadingDelay(), ctx);
            if (msg instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) msg;
                if (httpContent.content().isReadable()) {
                    httpProcessorContext.setHttpContent(httpContent);
                    httpRequestInformationProcessor.process(httpProcessorContext);
                }
            }

            if (msg instanceof LastHttpContent) {
                HttpRequestCustomProcessor customProcessor = httpProcessorContext.getServerInformationContext()
                        .getServerConfigBuilderContext().getHttpRequestCustomProcessor();
                if (customProcessor != null) {
                    httpProcessorContext = customProcessor.process(httpProcessorContext);
                }
                this.requestResponseMatchingProcessor = new HttpRequestResponseMatchingProcessor();
                this.requestResponseMatchingProcessor.process(httpProcessorContext);
                ctx.fireChannelReadComplete();
            }
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws IOException {
        if (httpResponseProcessor != null) {
            randomConnectionClose(ctx, this.index, 1);
            businessLogicDelay(serverInformationContext.getServerConfigBuilderContext().getLogicDelay(), ctx);
            this.httpResponseProcessor.process(httpProcessorContext);

            int queues = httpProcessorContext.getServerInformationContext().getServerConfigBuilderContext().getQueues();
            DELAY = httpProcessorContext.getServerInformationContext().getServerConfigBuilderContext().getDelay();

            if (DELAY != 0 && queues > 0) {
                try {
                    executorService.execute(new Runnable() {
                        public ChannelHandlerContext getCtx() {
                            return ctx;
                        }

                        @Override
                        public void run() {
                            handlers[0].delayEvent(ctx, httpProcessorContext, DELAY, 0);
                        }
                    });

                } catch (Throwable throwable) {
                    System.out.println("CAUGHT ################ " + throwable);
                }
            }


            else {
                FullHttpResponse response = httpProcessorContext.getFinalResponse();
                if (httpProcessorContext.getHttpRequestContext().isKeepAlive()) {
                    randomConnectionClose(ctx, this.index, 2);
                    ctx.write(response);
                } else {
                    randomConnectionClose(ctx, this.index, 2);
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                }

            }
            randomConnectionClose(ctx, this.index, 3);

            HttpResponseCustomProcessor customProcessor = httpProcessorContext.getServerInformationContext()
                    .getServerConfigBuilderContext().getCustomResponseProcessor();
            if (customProcessor != null) {
                httpProcessorContext = customProcessor.process(httpProcessorContext);
            }
            ctx.flush();
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception occurred while processing the response", cause);
        ctx.close();
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    private void readingDelay(int delay,ChannelHandlerContext ctx) {

        if (delay != 0) {
            ScheduledFuture scheduledFuture =
                    scheduledReadingExecutorService.schedule(new Callable() {
                        public Object call() throws Exception {
                            return "Reading";
                        }
                    }, delay, TimeUnit.MILLISECONDS);
            try {
                log.info("result = " + scheduledFuture.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            scheduledReadingExecutorService.shutdown();
        }
    }

    private void businessLogicDelay(int delay, ChannelHandlerContext ctx) {
        if (delay != 0) {
            ScheduledFuture scheduledLogicFuture =
                    scheduledLogicExecutorService.schedule(new Callable() {
                        public Object call() throws Exception {
                            return "Logic delay";
                        }
                    }, delay, TimeUnit.MILLISECONDS);
            try {
                log.info("result = " + scheduledLogicFuture.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }finally {
                scheduledLogicExecutorService.shutdown();
            }
        }
    }

    private void randomConnectionClose(ChannelHandlerContext ctx, int randomIndex, int pointIndex) {
        if (randomIndex == pointIndex) {
            log.info("Random close");
            ctx.close();
        }
    }

    private void randomIndexGenerator(Boolean randomConnectionClose) {
        if (randomConnectionClose) {
            Random rn = new Random();
            index = (rn.nextInt(100) + 1) % 6;
        } else
            index = -1;
    }

    public MockServerThread[] getHandlers() {
        return handlers;
    }

    public void setHandlers(MockServerThread[] handlers) {
        this.handlers = handlers;
    }

}

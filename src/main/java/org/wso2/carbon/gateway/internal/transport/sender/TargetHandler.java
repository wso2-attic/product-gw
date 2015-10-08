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
package org.wso2.carbon.gateway.internal.transport.sender;

import com.lmax.disruptor.RingBuffer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.internal.common.CarbonCallback;
import org.wso2.carbon.gateway.internal.common.CarbonMessage;
import org.wso2.carbon.gateway.internal.common.Pipe;
import org.wso2.carbon.gateway.internal.transport.common.Constants;
import org.wso2.carbon.gateway.internal.transport.common.HTTPContentChunk;
import org.wso2.carbon.gateway.internal.transport.common.PipeImpl;
import org.wso2.carbon.gateway.internal.transport.common.Util;
import org.wso2.carbon.gateway.internal.transport.common.disruptor.publisher.CarbonEventPublisher;
import org.wso2.carbon.gateway.internal.transport.sender.channel.TargetChannel;
import org.wso2.carbon.gateway.internal.transport.sender.channel.pool.ConnectionManager;

import java.net.InetSocketAddress;

/**
 * A class responsible for handling responses coming from BE.
 */
public class TargetHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(TargetHandler.class);

    private CarbonCallback callback;
    private CarbonCallback continueCallback;
    private RingBuffer ringBuffer;
    private CarbonMessage cMsg;
    private int queuesize;
    private ConnectionManager connectionManager;
    private TargetChannel targetChannel;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;

            cMsg = new CarbonMessage(Constants.PROTOCOL_NAME);
            cMsg.setPort(((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
            cMsg.setHost(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
            cMsg.setDirection(CarbonMessage.RESPONSE);
            cMsg.setCarbonCallback(callback);

            Pipe pipe = new PipeImpl(queuesize);

            cMsg.setPipe(pipe);
            cMsg.setDirection(CarbonMessage.RESPONSE);
            cMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.getStatus().code());
            cMsg.setProperty(Constants.TRANSPORT_HEADERS, Util.getHeaders(httpResponse));

            if (continueCallback != null) {
                continueCallback.done(cMsg);
            }
            ringBuffer.publishEvent(new CarbonEventPublisher(cMsg));
        } else {
            HTTPContentChunk chunk;
            if (cMsg != null) {
                if (msg instanceof LastHttpContent) {
                    LastHttpContent lastHttpContent = (LastHttpContent) msg;
                    chunk = new HTTPContentChunk(lastHttpContent);
                    connectionManager.returnChannel(targetChannel);
                } else {
                    DefaultHttpContent httpContent = (DefaultHttpContent) msg;
                    chunk = new HTTPContentChunk(httpContent);
                }
                cMsg.getPipe().addContentChunk(chunk);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Target channel closed.");
    }

    public void setCallback(CarbonCallback callback) {
        this.callback = callback;
    }

    public void setRingBuffer(RingBuffer ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void setQueuesize(int queuesize) {
        this.queuesize = queuesize;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    public void setContinueCallback(CarbonCallback continueCallback) {
        this.continueCallback = continueCallback;
    }
}

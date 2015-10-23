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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.carbon.gateway.internal.common.CarbonCallback;
import org.wso2.carbon.gateway.internal.common.CarbonMessage;
import org.wso2.carbon.gateway.internal.common.Pipe;
import org.wso2.carbon.gateway.internal.transport.common.HTTPContentChunk;
import org.wso2.carbon.gateway.internal.transport.common.Util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * A Class responsible for handling the response.
 */
public class ResponseCallback implements CarbonCallback {

    private ChannelHandlerContext ctx;

    public ResponseCallback(ChannelHandlerContext channelHandlerContext) {
        this.ctx = channelHandlerContext;
    }

    public void done(CarbonMessage cMsg) {
        final Pipe pipe = cMsg.getPipe();
        if (pipe == null) {
            //final HttpResponse response = Util.createHttp202Response();

            HttpResponse response = Util.createHttp202Response();
            DefaultHttpContent responseBody = new DefaultHttpContent(Unpooled.wrappedBuffer(ByteBuffer.wrap
                    (((String) cMsg.getSimplePayload()).getBytes(StandardCharsets.UTF_8))));
            //ByteBufInputStream msgContent = cMsg.getContentStream();

            //response.setContent(ChannelBuffers.copiedBuffer("pong", CharsetUtil.UTF_8));
            //response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
            // ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            ctx.write(response);
            final ChannelFuture f = ctx.writeAndFlush(responseBody); // (3)
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    assert f == future;
                    ctx.close();
                }
            });
            return;
        }
        final HttpResponse response = Util.createHttpResponse(cMsg);
        ctx.write(response);
        while (true) {
            HTTPContentChunk chunk = (HTTPContentChunk) pipe.getContent();
            HttpContent httpContent = chunk.getHttpContent();
            if (httpContent instanceof LastHttpContent) {
                ctx.writeAndFlush(httpContent);
                break;
            }
            ctx.write(httpContent);
        }
    }
}

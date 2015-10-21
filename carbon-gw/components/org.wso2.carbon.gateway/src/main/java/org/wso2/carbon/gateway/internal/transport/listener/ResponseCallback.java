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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.carbon.gateway.internal.common.CarbonCallback;
import org.wso2.carbon.gateway.internal.common.CarbonMessage;
import org.wso2.carbon.gateway.internal.common.Pipe;
import org.wso2.carbon.gateway.internal.transport.common.HTTPContentChunk;
import org.wso2.carbon.gateway.internal.transport.common.Util;

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
        final HttpResponse response = Util.createHttpResponse(cMsg);
        ctx.write(response);
        if (pipe.hasContent()) {
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
}

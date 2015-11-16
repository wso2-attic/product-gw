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

package org.wso2.carbon.gateway.internal.mediation.camel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import org.apache.camel.Exchange;
import org.apache.camel.support.TypeConverterSupport;
import org.apache.log4j.Logger;
import org.wso2.carbon.gateway.internal.common.CarbonMessage;
import org.wso2.carbon.gateway.internal.common.Pipe;
import org.wso2.carbon.gateway.internal.transport.common.PipeImpl;

import java.io.UnsupportedEncodingException;


/**
 * A type converter which is used to convert to and from CarbonMessage to other types
 * Specailly in case of content aware mediation
 */

public class CarbonMessageReverseTypeConverter extends TypeConverterSupport {
    private static final Logger log = Logger.getLogger(CarbonMessageTypeConverter.class);

    @SuppressWarnings("unchecked")

    public <T> T convertTo(Class<T> type, Exchange exchange, Object value) {
        if (value instanceof String) {
            ByteBuf msgBytes = null;
            ByteBufInputStream byteBufInputStream = null;
            try {
                msgBytes = Unpooled.wrappedBuffer(((String) value).getBytes("UTF-8"));
                msgBytes.retain();
                byteBufInputStream = new ByteBufInputStream(msgBytes);
                CarbonMessage carbonMessage = new CarbonMessage("http");
                Pipe pipe = new PipeImpl(msgBytes.readableBytes());
                pipe.setInputStream(byteBufInputStream);
                pipe.setMessageBytes(msgBytes);
                carbonMessage.setPipe(pipe);
                return (T) carbonMessage;
            } catch (UnsupportedEncodingException e) {
                log.error("Encoding type is not supported", e);
            } finally {
                if (msgBytes != null) {
                    msgBytes.release();
                }
                //byteBufInputStream.close();
            }

        }
        return null;
    }

    /**
     * This method needs to return true for subsequent executions of the same type converter.
     *
     * @return
     */
    @Override
    public boolean allowNull() {
        return true;
    }


}



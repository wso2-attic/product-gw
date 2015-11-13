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

import io.netty.buffer.*;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.*;
import org.apache.camel.Exchange;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.camel.support.TypeConverterSupport;
import org.apache.log4j.Logger;
import org.wso2.carbon.gateway.internal.common.CarbonMessage;
import org.wso2.carbon.gateway.internal.common.ContentChunk;
import org.wso2.carbon.gateway.internal.common.Pipe;
import org.wso2.carbon.gateway.internal.transport.common.HTTPContentChunk;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import javax.xml.parsers.ParserConfigurationException;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isContentLengthSet;
import static io.netty.handler.codec.http.HttpHeaders.removeTransferEncodingChunked;


/**
 * A type converter which is used to convert to and from array types
 * particularly for derived types of array component types and dealing with
 * primitive array types.
 */

public class CarbonMessageTypeConverter extends TypeConverterSupport {
    //public final class CarbonMessageTypeConverter {
    private static final Logger log = Logger.getLogger(CarbonMessageTypeConverter.class);

    @SuppressWarnings("unchecked")

    public <T> T convertTo(Class<T> type, Exchange exchange, Object value) {
        if (value instanceof CarbonMessage) {
//            List<ByteBuf> listOfContentBuffers = new ArrayList<ByteBuf>();
            Pipe pipe = ((CarbonMessage) value).getPipe();
            ByteBufInputStream byteBufInputStream = null;
//            BlockingQueue<ContentChunk> clonedContent = pipe.getClonedContentQueue();
//            while (true) {
//                HTTPContentChunk httpContentChunk = null;
//                try {
//                    if (clonedContent.isEmpty()) {
//                        break;
//                    } else {
//                        httpContentChunk = (HTTPContentChunk) clonedContent.take();
//                        listOfContentBuffers.add(httpContentChunk.getHttpContent().duplicate().content());
//                    }
//                } catch (InterruptedException e) {
//                    log.error("Error occurred during conversion from CarbonMessage", e);
//                }
//
//            }
//            ByteBuf compositeBuffer
//                    = Unpooled.wrappedBuffer(listOfContentBuffers.toArray(new ByteBuf[listOfContentBuffers.size()]));
//            byteBufInputStream = new ByteBufInputStream(pipe.getCompositeBuffer());
            byteBufInputStream = aggregateChunks(pipe);
            XmlConverter xmlConverter = new XmlConverter();
            try {
                return (T) xmlConverter.toDOMDocument(byteBufInputStream, exchange);
            } catch (IOException e) {
                log.error("IO Error occurred during conversion to XML", e);
            } catch (SAXException e) {
                log.error("SAX Parser Error occurred during conversion to XML", e);
            } catch (ParserConfigurationException e) {
                log.error("Parser Configuration Error occurred during conversion to XML", e);
            }
            //return (T)byteBufInputStream;
        }
        return null;
    }

    private ByteBufInputStream aggregateChunks(Pipe pipe) {
        ByteBufInputStream byteBufInputStream = null;
        BlockingQueue<ContentChunk> clonedContent = pipe.getClonedContentQueue();
        CompositeByteBuf content = new CompositeByteBuf(new PooledByteBufAllocator(), false, 1024);
        ;

        try {
            //Get the first chunk
            //content = (CompositeByteBuf)(((HTTPContentChunk) clonedContent.take()).getHttpContent().duplicate().content());
            while (true) {
                if (!clonedContent.isEmpty()) {
                    HttpContent chunk = ((HTTPContentChunk) clonedContent.take()).getHttpContent();
                    //listOfContentBuffers.add(httpContentChunk.getHttpContent().duplicate().content());
                    // Append the content of the chunk
                    if (chunk.content().isReadable()) {
                        chunk.retain();
                        content.addComponent(chunk.content());
                        content.writerIndex(content.writerIndex() + chunk.content().readableBytes());
                    }

                } else {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error occurred during conversion from CarbonMessage", e);
        }
        byteBufInputStream = new ByteBufInputStream(content);
        return byteBufInputStream;
    }

}


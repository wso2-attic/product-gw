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
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.HttpContent;

import org.apache.camel.Exchange;
import org.apache.camel.support.TypeConverterSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ContentChunk;
import org.wso2.carbon.messaging.HTTPContentChunk;
import org.wso2.carbon.messaging.Pipe;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;


/**
 * A type converter which is used to convert to and from CarbonMessage to other types
 * Specailly in case of content aware mediation
 */

public class CarbonMessageTypeConverter extends TypeConverterSupport {
    private static final Logger log = LoggerFactory.getLogger(CarbonMessageTypeConverter.class);

    @SuppressWarnings("unchecked")

    public <T> T convertTo(Class<T> type, Exchange exchange, Object value) {
        if (value instanceof CarbonMessage) {
            //Retrieving the Pipe from the carbon message
            Pipe pipe = ((CarbonMessage) value).getPipe();
            //Input stream used for building the desired message
            ByteBufInputStream byteBufInputStream = null;
            //Create a composite buffer from content chunks in the pipe
            CompositeByteBuf contentBuf = aggregateChunks(pipe);
            //Check whether we have any content to be processed
            if (contentBuf.capacity() != 0) {
                try {
                    if (type.isAssignableFrom(Document.class)) {
                        //Convert the input stream into xml dom element
                        return (T) toDocument(contentBuf, exchange);
                    } else if (type.isAssignableFrom(DOMSource.class)) {
                        return (T) toDOMSource(contentBuf, exchange);
                    } else if (type.isAssignableFrom(SAXSource.class)) {
                        return (T) toSAXSource(contentBuf, exchange);
                    } else if (type.isAssignableFrom(StAXSource.class)) {
                        return (T) toStAXSource(contentBuf, exchange);
                    } else if (type.isAssignableFrom(StreamSource.class)) {
                        return (T) toStreamSource(contentBuf, exchange);
                    } else if (type.isAssignableFrom(InputStream.class)) {
                        return (T) toInputStream(contentBuf, exchange);
                    } else if (type.isAssignableFrom(String.class)) {
                        return (T) toString(contentBuf, exchange);
                    }
                } catch (UnsupportedEncodingException e) {
                    log.error("Error occurred during type conversion", e);
                } finally {
                    //Release the buffer if all the content has been consumed
                    if (contentBuf.readerIndex() == contentBuf.writerIndex()) {
                        contentBuf.release();
                    }
                }
            }

        }
        return null;
    }

    private CompositeByteBuf aggregateChunks(Pipe pipe) {
        ByteBufInputStream byteBufInputStream = null;
        //Create an instance of composite byte buffer to hold the content chunks
        CompositeByteBuf content = new UnpooledByteBufAllocator(true).compositeBuffer();
        try {
            //Check whether the pipe is filled with HTTP content chunks up to last chunk
            while (pipe.isEmpty() || !pipe.isLastChunkAdded()) {
                Thread.sleep(10);
            }
            //Get a clone of content chunk queue from the pipe
            BlockingQueue<ContentChunk> clonedContent = pipe.getClonedContentQueue();
            //Traverse through the http content chunks and create the composite buffer
            while (true) {
                if (!clonedContent.isEmpty()) {
                    //Retrieve the HTTP content chunk from cloned queue
                    HttpContent chunk = ((HTTPContentChunk) clonedContent.take()).getHttpContent();
                    // Append the content of the chunk to the composite buffer
                    if (chunk.content().isReadable()) {
                        chunk.retain();
                        content.addComponent(chunk.content());
                        content.writerIndex(content.writerIndex() + chunk.content().readableBytes());
                    }

                } else {
                    //When all the content chunks are read, break from the loop
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error occurred during conversion from CarbonMessage", e);
        }
        //Return the composite buffer
        return content;
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


    private byte[] toByteArray(ByteBuf buffer, Exchange exchange) {
        byte[] bytes = new byte[buffer.readableBytes()];
        int readerIndex = buffer.readerIndex();
        buffer.getBytes(readerIndex, bytes);
        return bytes;
    }


    private String toString(ByteBuf buffer, Exchange exchange) throws UnsupportedEncodingException {
        byte[] bytes = toByteArray(buffer, exchange);
        // use type converter as it can handle encoding set on the Exchange
        if (exchange != null) {
            return exchange.getContext().getTypeConverter().convertTo(String.class, exchange, bytes);
        }
        return new String(bytes, "UTF-8");
    }


    private InputStream toInputStream(ByteBuf buffer, Exchange exchange) {
        return new ByteBufInputStream(buffer);
    }

    private Document toDocument(ByteBuf buffer, Exchange exchange) {
        InputStream is = toInputStream(buffer, exchange);
        return exchange.getContext().getTypeConverter().convertTo(Document.class, exchange, is);
    }


    private DOMSource toDOMSource(ByteBuf buffer, Exchange exchange) {
        InputStream is = toInputStream(buffer, exchange);
        return exchange.getContext().getTypeConverter().convertTo(DOMSource.class, exchange, is);
    }


    private SAXSource toSAXSource(ByteBuf buffer, Exchange exchange) {
        InputStream is = toInputStream(buffer, exchange);
        return exchange.getContext().getTypeConverter().convertTo(SAXSource.class, exchange, is);
    }


    private StreamSource toStreamSource(ByteBuf buffer, Exchange exchange) {
        InputStream is = toInputStream(buffer, exchange);
        return exchange.getContext().getTypeConverter().convertTo(StreamSource.class, exchange, is);
    }


    private StAXSource toStAXSource(ByteBuf buffer, Exchange exchange) {
        InputStream is = toInputStream(buffer, exchange);
        return exchange.getContext().getTypeConverter().convertTo(StAXSource.class, exchange, is);
    }


}


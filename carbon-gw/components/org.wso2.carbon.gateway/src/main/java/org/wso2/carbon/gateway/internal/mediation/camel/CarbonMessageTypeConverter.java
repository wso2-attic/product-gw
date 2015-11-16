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

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.HttpContent;

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
import java.util.concurrent.BlockingQueue;
import javax.xml.parsers.ParserConfigurationException;


/**
 * A type converter which is used to convert to and from CarbonMessage to other types
 * Specailly in case of content aware mediation
 */

public class CarbonMessageTypeConverter extends TypeConverterSupport {
    private static final Logger log = Logger.getLogger(CarbonMessageTypeConverter.class);

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
                //Increase the reference count by 1
                contentBuf.retain();
                //Create the input stream from the composite buffer
                byteBufInputStream = new ByteBufInputStream(contentBuf);
                try {
                    if (type.isAssignableFrom(org.w3c.dom.Document.class)) {
                        //Convert the input stream into xml dom element
                        XmlConverter xmlConverter = new XmlConverter();
                        return (T) xmlConverter.toDOMDocument(byteBufInputStream, exchange);
                    } else if (type.isAssignableFrom(java.io.InputStream.class)) {
                        return (T) byteBufInputStream;
                    }
                } catch (IOException e) {
                    log.error("IO Error occurred during conversion to XML", e);
                } catch (SAXException e) {
                    log.error("SAX Parser Error occurred during conversion to XML", e);
                } catch (ParserConfigurationException e) {
                    log.error("Parser Configuration Error occurred during conversion to XML", e);
                } finally {
                    try {
                        // Release the buffer and input stream
                        contentBuf.release();
                        byteBufInputStream.close();
                    } catch (IOException e) {
                        log.error("IOException when closing the input stream", e);
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


}


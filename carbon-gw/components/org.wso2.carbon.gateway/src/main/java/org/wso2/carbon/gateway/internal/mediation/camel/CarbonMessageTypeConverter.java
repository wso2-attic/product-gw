/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.gateway.internal.mediation.camel;


import org.apache.camel.Exchange;
import org.apache.camel.support.TypeConverterSupport;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.wso2.carbon.gateway.internal.common.ByteBufferBackedInputStream;
import org.wso2.carbon.messaging.CarbonMessage;


import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
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
    private static final Logger log = Logger.getLogger(CarbonMessageTypeConverter.class);

    @SuppressWarnings("unchecked")

    public <T> T convertTo(Class<T> type, Exchange exchange, Object value) {
        if (value instanceof CarbonMessage) {
            CarbonMessage msg = (CarbonMessage) value;
            //Take a clone of the message content
            BlockingQueue<ByteBuffer> contentBuf = aggregateContent(msg);
            InputStream inputStream = new ByteBufferBackedInputStream(contentBuf);
                try {
                    if (type.isAssignableFrom(Document.class)) {
                        //Convert the input stream into xml dom element
                        return (T) toDocument(inputStream, exchange);
                    } else if (type.isAssignableFrom(DOMSource.class)) {
                        return (T) toDOMSource(inputStream, exchange);
                    } else if (type.isAssignableFrom(SAXSource.class)) {
                        return (T) toSAXSource(inputStream, exchange);
                    } else if (type.isAssignableFrom(StAXSource.class)) {
                        return (T) toStAXSource(inputStream, exchange);
                    } else if (type.isAssignableFrom(StreamSource.class)) {
                        return (T) toStreamSource(inputStream, exchange);
                    } else if (type.isAssignableFrom(InputStream.class)) {
                        return (T) inputStream;
                    } else if (type.isAssignableFrom(String.class)) {
                        return (T) toString(inputStream, exchange);
                    }
                } catch (UnsupportedEncodingException e) {
                    log.error("Error occurred during type conversion", e);
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

    private String toString(InputStream is, Exchange exchange) throws UnsupportedEncodingException {
        //byte[] bytes = toByteArray(buffer, exchange);
        // use type converter as it can handle encoding set on the Exchange
        if (exchange != null) {
            return exchange.getContext().getTypeConverter().convertTo(String.class, exchange, is);
        }
        return null;
    }

    private Document toDocument(InputStream is, Exchange exchange) {
        //InputStream is = toInputStream(buffer, exchange);
        return exchange.getContext().getTypeConverter().convertTo(Document.class, exchange, is);
    }


    private DOMSource toDOMSource(InputStream is, Exchange exchange) {
        //InputStream is = toInputStream(buffer, exchange);
        return exchange.getContext().getTypeConverter().convertTo(DOMSource.class, exchange, is);
    }


    private SAXSource toSAXSource(InputStream is, Exchange exchange) {
        //InputStream is = toInputStream(buffer, exchange);
        return exchange.getContext().getTypeConverter().convertTo(SAXSource.class, exchange, is);
    }


    private StreamSource toStreamSource(InputStream is, Exchange exchange) {
        //InputStream is = toInputStream(buffer, exchange);
        return exchange.getContext().getTypeConverter().convertTo(StreamSource.class, exchange, is);
    }


    private StAXSource toStAXSource(InputStream is, Exchange exchange) {
        //InputStream is = toInputStream(buffer, exchange);
        return exchange.getContext().getTypeConverter().convertTo(StAXSource.class, exchange, is);
    }


    private BlockingQueue<ByteBuffer> aggregateContent(CarbonMessage msg) {

        try {
            //Check whether the message is fully read
            while (!msg.isEomAdded()) {
                Thread.sleep(10);
            }
            //Get a clone of content chunk queue from the pipe
            BlockingQueue<ByteBuffer> clonedContent = msg.getClonedMessageBody();
            return clonedContent;
        } catch (Exception e) {
            log.error("Error occurred during conversion from CarbonMessage", e);
        }
        return null;
    }


}



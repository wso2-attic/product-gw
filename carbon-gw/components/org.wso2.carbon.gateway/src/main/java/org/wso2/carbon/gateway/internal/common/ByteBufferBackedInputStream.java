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

package org.wso2.carbon.gateway.internal.common;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;


/**
 * A Gateway specific implementation to convert ByteBuffer queue to
 * an input stream
 */

public class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;
    BlockingQueue<ByteBuffer> buffersQueue;
    private static final Logger log = Logger.getLogger(ByteBufferBackedInputStream.class);

    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public ByteBufferBackedInputStream(BlockingQueue<ByteBuffer> buffersQueue) {
        this.buffersQueue = buffersQueue;
        if (!this.buffersQueue.isEmpty()) {
            try {
                this.buf = buffersQueue.take();
            } catch (InterruptedException e) {
                log.error("Error occurred during conversion from CarbonMessage", e);
            }
        }
    }

    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            if (!buffersQueue.isEmpty()) {
                getBuf();
            } else {
                return -1;
            }
        }
        return buf.get() & 0xFF;
    }

    public int read(byte[] bytes, int off, int len)
            throws IOException {
        if (!buf.hasRemaining()) {
            if (!buffersQueue.isEmpty()) {
                getBuf();
            } else {
                return -1;
            }
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }

    private void getBuf() {
        try {
            this.buf = this.buffersQueue.take();
        } catch (InterruptedException e) {
            log.error("Error occurred during conversion from CarbonMessage", e);
        }

    }
}

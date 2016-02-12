/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.gateway.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * InputStreamHandler
 */
public class InputStreamHandler implements Runnable {
    private String streamType;
    private InputStream inputStream;
    private StringBuilder stringBuilder;
    private volatile boolean running = true;
    private static final Logger log = LoggerFactory.getLogger(InputStreamHandler.class);

    public InputStreamHandler(String name, InputStream is) {
        this.streamType = name;
        this.inputStream = is;
        this.stringBuilder = new StringBuilder();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(this.inputStream, Charset.defaultCharset());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            while (running) {
                String bufferLine = bufferedReader.readLine();
                if (bufferLine == null) {
                    break;
                }
                if ("inputStream".equals(this.streamType)) {
                    this.stringBuilder.append(bufferLine).append("\n");
                    log.info(bufferLine);
                } else if ("errorStream".equals(this.streamType)) {
                    this.stringBuilder.append(bufferLine).append("\n");
                    log.error(bufferLine);
                }
            }
        } catch (Exception ex) {
            log.error("Problem reading the [" + this.streamType + "] due to: " + ex.getMessage(), ex);
        } finally {
            if (inputStreamReader != null) {
                try {
                    this.inputStream.close();
                } catch (IOException var11) {
                    log.error("Error occurred while closing the stream: " + var11.getMessage(), var11);
                }
            }
        }
    }

    public String getOutput() {
        return this.stringBuilder.toString();
    }

    public void stop() {
        running = false;
    }
}

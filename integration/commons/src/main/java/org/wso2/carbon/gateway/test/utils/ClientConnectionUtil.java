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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientConnectionUtil {

    private static final Logger log = LoggerFactory.getLogger(ClientConnectionUtil.class);

    private ClientConnectionUtil() {
    }

    public static void waitForPort(int port, long timeout, boolean verbose) {
        long startTime = System.currentTimeMillis();
        boolean isPortOpen = false;

        while (true) {
            if (!isPortOpen && System.currentTimeMillis() - startTime < timeout) {
                Socket socket = null;

                try {
                    InetAddress e = InetAddress.getByName("localhost");
                    socket = new Socket(e, port);
                    isPortOpen = socket.isConnected();
                    if (!isPortOpen) {
                        continue;
                    }

                    if (verbose) {
                        log.info("Successfully connected to the server on port " + port);
                    }
                } catch (IOException var21) {
                    if (verbose) {
                        log.info("Waiting until server starts on port " + port);
                    }

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException var20) {

                    }
                    continue;
                } finally {
                    try {
                        if (socket != null && socket.isConnected()) {
                            socket.close();
                        }
                    } catch (IOException var19) {
                        log.error("Can not close the socket with is used to check the server status ", var19);
                    }
                }
                return;
            }
            throw new RuntimeException("Port " + port + " is not open");
        }
    }

    public static void waitForPort(int port) {
        waitForPort(port, 60000L, true);
    }

    public static boolean isPortOpen(int port, boolean verbose) {
        Socket socket = null;
        boolean isPortOpen = false;

        try {
            InetAddress e = InetAddress.getByName("localhost");
            socket = new Socket(e, port);
            isPortOpen = socket.isConnected();
            if (isPortOpen && verbose) {
                log.info("Successfully connected to the server on port " + port);
            }
        } catch (IOException var12) {
            if (verbose) {
                log.info("Waiting until server starts on port " + port);
            }
            isPortOpen = false;
        } finally {
            try {
                if (socket != null && socket.isConnected()) {
                    socket.close();
                }
            } catch (IOException var11) {
                if (verbose) {
                    log.error("Can not close the socket with is used to check the server status ", var11);
                }
            }

        }
        return isPortOpen;
    }
}

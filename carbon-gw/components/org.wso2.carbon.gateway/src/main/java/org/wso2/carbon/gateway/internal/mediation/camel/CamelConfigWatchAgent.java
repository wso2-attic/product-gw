/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.internal.mediation.camel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.internal.GatewayActivator;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Camel configuration file watching agent implementation
 */
public class CamelConfigWatchAgent {
    private static final Logger log = LoggerFactory.getLogger(CamelConfigWatchAgent.class);

    private ExecutorService pool = Executors.newFixedThreadPool(10);
    private int eventCount = 0;

    /**
     * Start watching the specified directory for file modifications, adding new files, etc
     *
     * @param customRouteManger customRouteManger
     * @throws Exception
     */
    public void startWatchingForModifications(CamelCustomRouteManager customRouteManger) throws Exception {
        Path directoryPath = Paths.get(GatewayActivator.CAMEL_CONFIGS_DIRECTORY);
        log.info("Start watching directory [Path: " + directoryPath.toString() + "]");
        try {
            Future<Integer> future = pool.submit(() -> {
                try {
                    Boolean isFolder = (Boolean) Files.getAttribute(directoryPath, "basic:isDirectory", NOFOLLOW_LINKS);
                    if (!isFolder) {
                        throw new IllegalArgumentException("Path: " + directoryPath + " is not a folder");
                    }
                } catch (IOException ioe) {
                    throw ioe;
                }

                FileSystem fs = directoryPath.getFileSystem();
                try (WatchService service = fs.newWatchService()) {
                    directoryPath.register(service, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE, OVERFLOW);
                    log.info("Watch service running ...");
                    WatchKey key;
                    while (true) {
                        //todo debug log
                        log.debug("Watch service running ...");
                        key = service.take();
                        WatchEvent.Kind<?> kind;

                        log.debug("FileWatcher event detected!");
                        List<WatchEvent<?>> watchEvents = key.pollEvents();
                        WatchEvent createdEvent = null;

                        for (WatchEvent<?> watchEvent : watchEvents) {

                            String fileName = watchEvent.context().toString();

                            kind = watchEvent.kind();
                            if (OVERFLOW == kind || fileName.startsWith(".") || fileName.endsWith("~")) {
                                continue;
                            } else if (ENTRY_CREATE == kind) {

                                log.debug("New File creation Event Fired!");
                                createdEvent = watchEvent;
                                customRouteManger.addRoutesFromCustomConfigs(fileName);

                            } else if (ENTRY_DELETE == kind) {
                                log.debug("File deleted Event Fired!");
                                customRouteManger.removeRoutesFromCustomConfigs(fileName);
                            } else if (ENTRY_MODIFY == kind) {
                                if (createdEvent != null) {
                                    // new file creation or copying is non-atomic in some operating systems.
                                    // Both created and modified events will be triggered so ignore the modified event
                                    if (!fileName.equals(createdEvent.context().toString())) {
                                        log.info("File modification Event Fired!");
                                        customRouteManger.modifyRoutesFromCustomConfigs(fileName);
                                        createdEvent = null;
                                    }
                                } else {
                                    log.info("File modification Event Fired!");
                                    customRouteManger.modifyRoutesFromCustomConfigs(fileName);
                                }
                            }

                        }
                        key.reset();
                    }

                } catch (Exception e) {
                    throw e;
                }
            });
            if (future == null) {
                log.error("Camel config watcher has failed!");
            }
        } catch (Exception e) {
            throw e;
        }
    }
}

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

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.internal.GatewayActivator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mange the custom routes
 */
public class CamelCustomRouteManager {

    private static final Logger log = LoggerFactory.getLogger(CamelCustomRouteManager.class);
    Map<String, List<RouteDefinition>> routesMap = new HashMap<>();
    private CamelContext camelContext;

    public CamelCustomRouteManager(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    /**
     * Adding the routes from custom configuration files
     *
     * @param fileName custom configuration file name
     */
    public void addRoutesFromCustomConfigs(String fileName) throws FileNotFoundException {
        File createdFile = new File(GatewayActivator.CAMEL_CONFIGS_DIRECTORY + File.separator + fileName);
        addRoutesFromFile(createdFile);
    }

    private void addRoutesFromFile(File newFile) throws FileNotFoundException {
        String fileName = newFile.getName();
        if (fileName.equals(GatewayActivator.CAMEL_CONTEXT_NAME)) {
            log.debug("Skipping camel Context file [" + fileName + "]");
            return;
        }
        InputStream inputStream = new FileInputStream(newFile.getAbsolutePath());
        try {
            log.info("Adding custom routes from [" + fileName + "]");
            RoutesDefinition routesDefinition = camelContext.loadRoutesDefinition(inputStream);
            List<RouteDefinition> routes = routesDefinition.getRoutes();
            routesMap.put(fileName, routes);
            camelContext.addRouteDefinitions(routes);
        } catch (Exception e) {
            String msg = "Error while adding the routes from file [" + fileName + "] ";
            log.error(msg + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                String msg = "Error while Closing the input stream";
                log.error(msg + e);
            }
        }
    }

    public void modifyRoutesFromCustomConfigs(String fileName) throws FileNotFoundException {
        File modifiedFile = new File(GatewayActivator.CAMEL_CONFIGS_DIRECTORY + File.separator + fileName);
        InputStream inputStream = new FileInputStream(modifiedFile.getAbsoluteFile());
        try {
            RoutesDefinition routesDefinition = camelContext.loadRoutesDefinition(inputStream);
            List<RouteDefinition> modifiedRoutes = routesDefinition.getRoutes();

            log.info("Updating custom routes from [" + fileName + "]");
            camelContext.removeRouteDefinitions(routesMap.remove(fileName));
            camelContext.addRouteDefinitions(modifiedRoutes);
            routesMap.put(fileName, modifiedRoutes);

        } catch (Exception e) {
            String msg = "Error while updating the routes from file [" + fileName + "] ";
            log.error(msg + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                String msg = "Error while Closing the input stream";
                log.error(msg + e);
            }
        }
    }

    public void removeRoutesFromCustomConfigs(String fileName) throws FileNotFoundException {

        if (fileName.equals(GatewayActivator.CAMEL_CONTEXT_NAME)) {
            log.warn("Default camel Context file [" + fileName + "] removed");
            return;
        }
        try {
            log.info("Removing custom routes from [" + fileName + "]");
            List<RouteDefinition> routes = routesMap.remove(fileName);
            camelContext.removeRouteDefinitions(routes);
        } catch (Exception e) {
            String msg = "Error while removing the routes from file [" + fileName + "] ";
            log.error(msg + e);
        }
    }

    /**
     * Adding the routes from the files listed under the directory given by the path
     *
     * @param path location of the particular directory
     */
    public void addRoutesToContext(String path) throws FileNotFoundException {
        File directory = new File(path);
        if (!directory.isDirectory()) {
            log.warn("Specified path is not a Directory");
            return;
        }

        File[] files = directory.listFiles();
        // Iterate over the files in the specified directory and load routes from them
        if (files != null) {
            for (File file : files) {
                addRoutesFromFile(file);
            }
        }
    }
}

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

package org.wso2.carbon.gateway.internal;

import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.spring.SpringCamelContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.wso2.carbon.gateway.internal.mediation.camel.CamelMediationComponent;
import org.wso2.carbon.gateway.internal.mediation.camel.CamelMediationEngine;
import org.wso2.carbon.messaging.CarbonMessageProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * OSGi Bundle Activator of the gateway Carbon component.
 */
public class GatewayActivator implements BundleActivator {

    private static final Logger log = LoggerFactory.getLogger(GatewayActivator.class);
    private SpringCamelContext camelContext;
    private ApplicationContext applicationContext;

    public static final String CAMEL_CONTEXT_CONFIG_FILE =
            "conf" + File.separator + "camel" + File.separator + "camel-context.xml";

    public static final String CAMEL_CONFIGS_DIRECTORY = "conf" + File.separator + "camel";

    public void start(BundleContext bundleContext) throws Exception {
        try {
            SpringCamelContext.setNoStart(true);
            applicationContext = new ClassPathXmlApplicationContext(new String[] { CAMEL_CONTEXT_CONFIG_FILE });
            camelContext = (SpringCamelContext) applicationContext.getBean("wso2-cc");
            camelContext.start();
            CamelMediationComponent component = (CamelMediationComponent) camelContext.getComponent("wso2-gw");

            CamelMediationEngine engine = component.getEngine();

            bundleContext.registerService(CarbonMessageProcessor.class, engine, null);

            // Add the routes from the custom routes config files in the repository/conf/camel directory
            log.info("Adding routes from custom routes configuration files ...");
            addRoutesToContext(CAMEL_CONFIGS_DIRECTORY);

            // Start the watch service for the custom route file modification in the repository/conf/camel directory
            new CamelConfigWatchAgent().startWatchingForModifications(Paths.get(CAMEL_CONFIGS_DIRECTORY), this);

        } catch (Exception exception) {
            String msg = "Error while loading " + CAMEL_CONTEXT_CONFIG_FILE + " configuration file";
            log.error(msg + exception);
            throw new RuntimeException(msg, exception);
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }

    /**
     * Adding the routes from custom configuration files
     *
     * @param routeConfig custom configuration file
     */
    public void addRoutesFromCustomConfigs(File routeConfig) throws FileNotFoundException {
        log.info("Adding Custom Routes from [" + routeConfig.getName() + "]");
        if (routeConfig.getPath().equals(CAMEL_CONTEXT_CONFIG_FILE)) {
            log.warn("Skipping camel Context file [" + routeConfig.getName() + "]");
            return;
        }

        InputStream inputStream = new FileInputStream(routeConfig.getAbsolutePath());
        try {
            RoutesDefinition routesDefinition = camelContext.loadRoutesDefinition(inputStream);
            camelContext.addRouteDefinitions(routesDefinition.getRoutes());
        } catch (Exception e) {
            String msg = "Error while adding the routes from file [" + routeConfig.getName() + "] ";
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

    /**
     * Adding the routes from the files listed under the directory given by the path
     *
     * @param path location of the particular directory
     */
    private void addRoutesToContext(String path) throws FileNotFoundException {
        File directory = new File(path);
        if (!directory.isDirectory()) {
            log.warn("Specified path is not a Directory");
            return;
        }

        File[] files = directory.listFiles();
        // Iterate over the files in the specified directory and load routes from them
        if (files != null) {
            for (File file : files) {
                addRoutesFromCustomConfigs(file);
            }
        }
    }
}

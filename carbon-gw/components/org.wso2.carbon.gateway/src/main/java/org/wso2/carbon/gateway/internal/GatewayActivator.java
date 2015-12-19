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

import org.apache.camel.spring.SpringCamelContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.wso2.carbon.gateway.internal.mediation.camel.CamelMediationComponent;
import org.wso2.carbon.gateway.internal.mediation.camel.CamelMediationEngine;
import org.wso2.carbon.gateway.internal.mediation.camel.CarbonMessageReverseTypeConverter;
import org.wso2.carbon.gateway.internal.mediation.camel.CarbonMessageTypeConverter;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;

import java.io.File;
import java.io.InputStream;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;



/**
 * OSGi Bundle Activator of the gateway Carbon component.
 */
public class GatewayActivator implements BundleActivator {

    private static final Logger log = LoggerFactory.getLogger(GatewayActivator.class);

    public static final String CAMEL_CONTEXT_CONFIG_FILE =
            "conf" + File.separator + "camel" + File.separator + "camel-context.xml";

    public void start(BundleContext bundleContext) throws Exception {
        try {
            SpringCamelContext.setNoStart(true);
            ApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext(new String[] { CAMEL_CONTEXT_CONFIG_FILE });

            SpringCamelContext camelContext = (SpringCamelContext) applicationContext.getBean("wso2-cc");
            camelContext.start();
            CamelMediationComponent component = (CamelMediationComponent) camelContext.getComponent("wso2-gw");
            addTypeConverters(camelContext);
            CamelMediationEngine engine = component.getEngine();

            bundleContext.registerService(CarbonMessageProcessor.class, engine, null);

        } catch (Exception exception) {
            String msg = "Error while loading " + CAMEL_CONTEXT_CONFIG_FILE + " configuration file";
            log.error(msg + exception);
            throw new RuntimeException(msg, exception);
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }

    private void addTypeConverters(SpringCamelContext camelContext) {
        camelContext.getTypeConverterRegistry().addTypeConverter(Document.class, CarbonMessage.class,
                new CarbonMessageTypeConverter());
        camelContext.getTypeConverterRegistry().addTypeConverter(InputStream.class, CarbonMessage.class,
                new CarbonMessageTypeConverter());
        camelContext.getTypeConverterRegistry().addTypeConverter(DOMSource.class, CarbonMessage.class,
                new CarbonMessageTypeConverter());
        camelContext.getTypeConverterRegistry().addTypeConverter(SAXSource.class, CarbonMessage.class,
                new CarbonMessageTypeConverter());
        camelContext.getTypeConverterRegistry().addTypeConverter(StAXSource.class, CarbonMessage.class,
                new CarbonMessageTypeConverter());
        camelContext.getTypeConverterRegistry().addTypeConverter(StreamSource.class, CarbonMessage.class,
                new CarbonMessageTypeConverter());
        camelContext.getTypeConverterRegistry().addTypeConverter(String.class, CarbonMessage.class,
                new CarbonMessageTypeConverter());
        camelContext.getTypeConverterRegistry().addTypeConverter(CarbonMessage.class, String.class,
                new CarbonMessageReverseTypeConverter());

    }
}

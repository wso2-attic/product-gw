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

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.wso2.carbon.gateway.internal.common.CarbonMessage;

import java.util.Map;

/**
 * Represents the CamelMediationEndpoint endpoint.
 */
public class CamelMediationEndpoint extends DefaultEndpoint {

    private CamelMediationEngine engine;
    private CarbonCamelMessageUtil carbonCamelMessageUtil;

    public CamelMediationEndpoint(String uri, CamelMediationComponent component, CamelMediationEngine engine) {
        super(uri, component);
        this.engine = engine;
        carbonCamelMessageUtil = new CarbonCamelMessageUtil();
    }

    public Producer createProducer() throws Exception {
        return new CamelMediationProducer(this, engine);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new CamelMediationConsumer(this, processor, engine);
    }

    public boolean isSingleton() {
        return true;
    }

    public Exchange createExchange(Map<String, Object> headers, CarbonMessage cmsg) {
        Exchange exchange = createExchange();
        carbonCamelMessageUtil.setCamelHeadersToClientRequest(exchange, headers, cmsg);
        exchange.getIn().setBody(cmsg);
        return exchange;
    }

    public CarbonCamelMessageUtil getCarbonCamelMessageUtil() {
        return carbonCamelMessageUtil;
    }

}

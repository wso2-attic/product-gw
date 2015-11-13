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
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.log4j.Logger;
import org.wso2.carbon.gateway.internal.common.CarbonMessage;

import java.util.Map;

/**
 * Represents the CamelMediationEndpoint endpoint.
 */
public class CamelMediationEndpoint extends DefaultEndpoint {

    private CamelMediationEngine engine;
    private CarbonCamelMessageUtil carbonCamelMessageUtil;
    private String httpMethodRestrict;
    private static final Logger log = Logger.getLogger(CamelMediationEndpoint.class);

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

    public String getHttpMethodRestrict() {
        return httpMethodRestrict;
    }

    public void setHttpMethodRestrict(String httpMethodRestrict) {
        this.httpMethodRestrict = httpMethodRestrict;
    }


    public boolean isSingleton() {
        return true;
    }

    public Exchange createExchange(Map<String, Object> headers, CarbonMessage cmsg) {
        Exchange exchange = createExchange();
        try {
            Message msg = createCamelMessage(cmsg, exchange);
            exchange.setIn(msg);
            carbonCamelMessageUtil.setCamelHeadersToClientRequest(exchange, headers, cmsg);
        } catch (Exception e) {
            log.error("Error occurred during the camel message creation", e);
        }
        //carbonCamelMessageUtil.setCamelHeadersToClientRequest(exchange, headers, cmsg);
        //exchange.getIn().setBody(cmsg);
        return exchange;
    }

    public Message createCamelMessage(CarbonMessage carbonMessage, Exchange exchange) throws Exception {
        CamelHttp4Message answer = new CamelHttp4Message();
        answer.setCarbonMessage(carbonMessage);
        answer.setExchange(exchange);
        return answer;
    }

    public CarbonCamelMessageUtil getCarbonCamelMessageUtil() {
        return carbonCamelMessageUtil;
    }

}

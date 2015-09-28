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

import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.util.ObjectHelper;

/**
 * mediation starts from here : this is the camel mediation consumer
 * for each route a consumer is created and added to the engine consumers map
 */
public class CamelMediationConsumer extends DefaultConsumer {
    private final CamelMediationEngine engine;

    public CamelMediationConsumer(CamelMediationEndpoint endpoint, Processor processor, CamelMediationEngine engine) {
        super(endpoint, processor);
        this.engine = engine;
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        engine.removeConsumer(getEndpoint().getEndpointKey());
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        String endPointUrlOnly = ObjectHelper.after(getEndpoint().getEndpointKey(), "://");
        engine.addConsumer(endPointUrlOnly, this);
    }

    @Override
    public CamelMediationEndpoint getEndpoint() {
        return (CamelMediationEndpoint) super.getEndpoint();
    }

}

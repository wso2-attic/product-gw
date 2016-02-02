/*
 * *
 *  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.gw.emulator.http.server.contexts;

import org.wso2.gw.emulator.dsl.contexts.AbstractServerOperationBuilderContext;

public class HttpServerOperationBuilderContext extends AbstractServerOperationBuilderContext {

    private HttpServerInformationContext httpServerInformationContext;

    public HttpServerOperationBuilderContext(HttpServerInformationContext httpServerInformationContext) {
        this.httpServerInformationContext = httpServerInformationContext;
    }

    @Override
    public HttpServerOperationBuilderContext start() {

        boolean b = parameterValidation();

        if (!b){
            System.out.println("server parameters were not set");
            return null;
        }else{

            try {
                httpServerInformationContext.getHttpServerInitializer().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }
    }

    @Override
    public HttpServerOperationBuilderContext stop() {
        try {
            httpServerInformationContext.getHttpServerInitializer().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean parameterValidation(){
        String host = httpServerInformationContext.getServerConfigBuilderContext().getHost();
        Integer port = httpServerInformationContext.getServerConfigBuilderContext().getPort();

        if (host == null){
            if (port.toString() == null){
                return false;
            }
        }
        return true;
    }
}

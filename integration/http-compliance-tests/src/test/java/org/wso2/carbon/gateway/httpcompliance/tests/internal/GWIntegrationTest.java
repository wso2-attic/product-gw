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

package org.wso2.carbon.gateway.httpcompliance.tests.internal;

import io.netty.handler.codec.http.HttpMethod;
import org.wso2.gw.emulator.dsl.Emulator;

import java.io.File;

import static org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext.response;

public abstract class GWIntegrationTest {

    protected void gwCleanup() throws Exception {
        GWClientProvider.getInstance().getGwClient().cleanArtifacts();
    }

    protected void gwHotDeployArtifacts(String file, String fromUri) throws Exception {
        GWClientProvider.getInstance().getGwClient().hotDeployArtifact(file);

        String responseBody = "Message consumer not found.";
        int count = 1;
        while (responseBody.equalsIgnoreCase("Message consumer not found.") && count <= 3) {
            count++;
            Thread.sleep(1000);
            responseBody = Emulator.getHttpEmulator().client().given(configure().host("127.0.0.1").port(9090))
                    .when(request().withMethod(HttpMethod.GET).withPath(fromUri)).then(response().assertionIgnore())
                    .operation().send().getReceivedResponseContext().getResponseBody();
        }
        if (count > 3) {
            gwRestart();
        }
    }

    protected File gwDeployCamel(String file) throws Exception {
        File backup = GWClientProvider.getInstance().getGwClient().deployCamel(file);
        gwRestart();
        return backup;

    }

    protected File gwDeployTransports(String file) throws Exception {
        File backup = GWClientProvider.getInstance().getGwClient().deployTransports(file);
        gwRestart();
        return backup;

    }

    protected void gwRestart() throws Exception {
        GWClientProvider.getInstance().getGwClient().restartGateway();
    }

    protected void gwRestoreFile(File file) throws Exception {
        GWClientProvider.getInstance().getGwClient().restoreFile(file);
        gwRestart();
    }
}

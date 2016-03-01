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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.gateway.httpcompliance.tests.responses.informational;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClient;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClientImpl;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.http.server.contexts.HttpServerOperationBuilderContext;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

/**
 * Created by pubuduf on 2/10/16.
 */
public class HTTP100ComplianceTest {
    private GatewayAdminClient gwClient;
    private HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public void setup() throws Exception {
        gwClient = new GatewayAdminClientImpl();
        gwClient.startGateway();
        gwClient.hotDeployArtifact("artifacts/http-compliance-test-camel-context.xml");
        gwClient.restartGateway();
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(6065).context("/users"))

                .when(request().withMethod(HttpMethod.GET).withPath("/user1"))
                .then(response().withBody("User1").withStatusCode(HttpResponseStatus.OK))

                .when(request().withMethod(HttpMethod.GET).withPath("/user2"))
                .then(response().withBody("User2").withStatusCode(HttpResponseStatus.OK))

                .when(request().withMethod(HttpMethod.GET).withPath("/user3"))
                .then(response().withBody("User3").withStatusCode(HttpResponseStatus.OK)).operation().start();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwClient.stopGateway();
        emulator.stop();
        gwClient.cleanArtifacts();
    }

//    @Test
//    public void testGETRequest() throws Exception {
//        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
//                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
//                .when(HttpClientRequestBuilderContext.request().withPath("/wrong-route").withMethod(HttpMethod.GET)
//                        .withHeader("routeId", "r2"))
//                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();
//
//        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.NOT_FOUND,
//                "Expected response code not found");
//    }
}

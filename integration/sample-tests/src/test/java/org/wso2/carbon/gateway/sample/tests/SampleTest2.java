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
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.gateway.sample.tests;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClient;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClientImpl;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.gw.emulator.http.server.contexts.HttpServerOperationBuilderContext;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

public class SampleTest2 {
    private static GatewayAdminClient gwClient;
    private static HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public static void setup() throws Exception {
        gwClient = new GatewayAdminClientImpl();
        gwClient.startGateway();
        gwClient.deployArtifact("artifacts/camel-context.xml");
        gwClient.restartGateway();
        emulator = startHttpEmulator();
    }

    @Test
    public void test1() {
        Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                               .host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request()
                              .withPath("/default").withMethod(HttpMethod.GET).withHeader
                                ("routeId", "r1"))
                .then(HttpClientResponseBuilderContext.response().withBody("User1"))
                .operation().send();
    }

    @Test
    public void test2() throws Exception{
        Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                               .host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request()
                              .withPath("/default").withMethod(HttpMethod.GET).withHeader
                                ("routeId", "r1"))
                .then(HttpClientResponseBuilderContext.response().withBody("User1"))
                .operation().send();
    }

    @AfterClass(alwaysRun = true)
    public static void after() throws Exception {
        gwClient.stopGateway();
        emulator.stop();
        gwClient.cleanArtifacts();
    }

    private static HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator()
                .server()
                .given(configure()
                               .host("127.0.0.1").port(6065).context("/users")
                )
                .when(request()
                              .withMethod(HttpMethod.GET).withPath("user1/"))
                .then(response()
                              .withBody("User1")
                              .withStatusCode(HttpResponseStatus.OK))
                .when(request()
                              .withMethod(HttpMethod.GET).withPath("user2/"))
                .then(response()
                              .withBody("User2")
                              .withStatusCode(HttpResponseStatus.OK))
                .when(request()
                              .withMethod(HttpMethod.GET).withPath("user3/"))
                .then(response()
                              .withBody("User3")
                              .withStatusCode(HttpResponseStatus.OK))
                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulator1() {
        return Emulator.getHttpEmulator()
                .server()
                .given(configure()
                               .host("127.0.0.1").port(6065).context("/user").readingDelay(1000).writingDelay(1000)
                )
                .when(request()
                              .withMethod(HttpMethod.GET))
                .then(response()
                              .withBody("Test Response1")
                              .withStatusCode(HttpResponseStatus.OK))
                .when(request()
                              .withMethod(HttpMethod.POST).withBody("test"))
                .then(response()
                              .withBody("Test Response2")
                              .withStatusCode(HttpResponseStatus.OK))
                .operation().start();
    }

}

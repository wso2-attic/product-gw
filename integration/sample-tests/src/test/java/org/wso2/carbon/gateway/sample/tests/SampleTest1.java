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

import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClient;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClientImpl;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.dsl.Operation;
import org.wso2.gw.emulator.dsl.Protocol;
import org.wso2.gw.emulator.dsl.QueryParameterOperation;
import org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.gw.emulator.http.params.Header;
import org.wso2.gw.emulator.http.params.QueryParameter;
import org.wso2.gw.emulator.http.server.contexts.HttpServerOperationBuilderContext;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import io.netty.handler.codec.http.HttpMethod;

public class SampleTest1 {

    private static GatewayAdminClient gwClient;
    private static HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public static void setup() throws Exception {
        gwClient = new GatewayAdminClientImpl();
        gwClient.startGateway();
        emulator = startHttpEmulator();
    }

    @Test
    public void test() {
        System.out.println("================ Test - 3 ======================");
        Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1")
                        .port(6065)
                        .readingDelay(1000)
                        .withProtocol(Protocol.HTTP)
                )
                .when(HttpClientRequestBuilderContext.request()
                                .withPath("/user/wso2")
                                .withMethod(HttpMethod.POST)
                                .withBody("TestRequest1")
                                .withHeaders(
                                        Operation.OR,
                                        new Header("Header1","value1"),
                                        new Header("Header2","value2")
                                ).withQueryParameters(
                                new QueryParameter("Query1","value1"),
                                new QueryParameter("Query2","value2")
                                )

                        //.withQueryParameter("q1","q1")
                )
                .then(HttpClientResponseBuilderContext.response()
                        .withBody("TestResponse1")
                        .withHeaders(
                                new Header("Header1","value1"),
                                new Header("Header2","value2"))
                )
                .operation().send();
    }

    @AfterClass
    public static void after() throws Exception {
        gwClient.stopGateway();
        emulator.stop();
    }

    private static HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator()
                .server()
                .given(configure()
                        .host("127.0.0.1")
                        .port(6065)
                        .context("/user")
                        .withProtocol(Protocol.HTTP)
                        //.withFastBackend(1,5000)   //(number of queues, delay)
                        .randomConnectionClose(false)
                        .logicDelay(1000)
                        .withCustomProcessor(true))
                ////////////////////////////////////test producer1
                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest")
                        .withHeader("Header1","value1")
                        .withPath("/wso2")
                )
                .then(response()
                        .withBody("TestResponse")
                        .withHeader("Header1","value1")
                        .withStatusCode(HttpResponseStatus.OK)
                )
                ////////////////////////////////test producer2
                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest1")
                        .withHeader("Header1","value1")
                        .withPath("*")
                )
                .then(response()
                        .withBody("TestResponse1")
                        .withHeader("Header1","value1")
                        .withStatusCode(HttpResponseStatus.OK)
                )/////////////////////////////////////////////////////////////////test producer3
                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest1")
                        .withPath("/wso2")
                        .withHeaders(
                                Operation.OR,
                                new Header("Header1","value1"),
                                new Header("Header2","value2"))
                        .withQueryParameters(
                                QueryParameterOperation.OR,
                                new QueryParameter("Query1","value1"),
                                new QueryParameter("Query2","value2")

                        )
                )
                .then(response()
                        .withBody("TestResponse1")
                        .withHeaders(
                                new Header("Header1","value1"),
                                new Header("Header2","value2"))
                        .withStatusCode(HttpResponseStatus.OK))

                ///////////////////////////////
                .when(request()
                        .withMethod(HttpMethod.POST).withBody("test")
                )
                .then(response()
                        .withBody("Test Response2").withStatusCode(HttpResponseStatus.OK))
                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest1")
                        .withHeaders(
                                Operation.AND,
                                new Header("Header1","value1"),
                                new Header("Header2","value2"))
                        .withPath("")

                )
                .then(response()
                        .withBody("TestResponse1")
                        .withHeaders(
                                new Header("Header1","value1"),
                                new Header("Header2","value2"))
                        .withStatusCode(HttpResponseStatus.OK))

                ////////////////////////////////
                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest111")
                        .withHeaders(
                                Operation.AND,
                                new Header("header1","value1"),
                                new Header("header2","value2"))
                        .withQueryParameter("query1","vlaue1")
                )
                .then(response()
                        .withBody("This is response for @{body} with @{header.name2} @{header.name2}")
                        .withHeaders(
                                new Header("header1","value1"),
                                new Header("header2","value2"))
                        .withStatusCode(HttpResponseStatus.OK))
                /////////////////////////////////////
                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest11")
                        .withHeader("Header1","value1")
                        .withQueryParameter("Query1","value1")
                )
                .then(response()
                        .withBody("TestResponse1")
                        .withHeader("Header1","value1")
                        .withStatusCode(HttpResponseStatus.OK)
                )

                .operation().start();
    }
}

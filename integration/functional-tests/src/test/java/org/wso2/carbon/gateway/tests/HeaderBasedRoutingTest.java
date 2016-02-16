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

package org.wso2.carbon.gateway.tests;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.gateway.tests.internal.GWIntegrationTest;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseProcessorContext;
import org.wso2.gw.emulator.http.server.contexts.HttpServerOperationBuilderContext;

import java.io.File;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

public class HeaderBasedRoutingTest extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public void setup() throws Exception {
        gwDeployArtifacts("artifacts" + File.separator + "header-based-routing.xml", "/http_headerbased");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    @Test
    public void headerTest1() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/http_headerbased").withMethod(HttpMethod.GET)
                        .withHeader("routeId", "ep1"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response header test 1",
                "Expected response not found");
    }

    @Test
    public void headerTest2() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/http_headerbased").withMethod(HttpMethod.GET)
                        .withHeader("routeId", "ep2"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response header test 2",
                "Expected response not found");
    }

    @Test
    public void headerTest3() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/http_headerbased")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response header test 3",
                "Expected response not found");
    }

    @Test
    public void headerTest4() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/http_headerbased").withMethod(HttpMethod.GET)
                        .withHeader("routeId", "ep3"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response header test 4",
                "Expected response not found");
    }

    @Test
    public void headerTest5() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/http_headerbased").withMethod(HttpMethod.GET)
                        .withHeader("routeId", "ep46"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response header test 5",
                "Expected response not found");
    }

    @Test
    public void headerTest6() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/http_headerbased").withMethod(HttpMethod.GET)
                        .withHeader("routeId", "ep45"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response header test 3",
                "Expected response not found");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwCleanup();
        emulator.stop();
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(9773).context("/services"))
                //Header
                .when(request().
                        withMethod(HttpMethod.GET).withPath("/headerservice1").withHeader("routeId", "ep1"))
                .then(response().withBody("Response header test 1").withStatusCode(HttpResponseStatus.OK))

                .when(request().withMethod(HttpMethod.GET).withPath("/headerservice2").withHeader("routeId", "ep2"))
                .then(response().withBody("Response header test 2").withStatusCode(HttpResponseStatus.OK))

                .when(request().withMethod(HttpMethod.GET).withPath("/headerservice3"))
                .then(response().withBody("Response header test 3").withStatusCode(HttpResponseStatus.OK))

                .when(request().withMethod(HttpMethod.GET).withPath("/headerservice4").withHeader("routeId", "ep3"))
                .then(response().withBody("Response header test 4").withStatusCode(HttpResponseStatus.OK))

                .when(request().withMethod(HttpMethod.GET).withPath("/headerservice5").withHeader("routeId", "ep46"))
                .then(response().withBody("Response header test 5").withStatusCode(HttpResponseStatus.OK))

                .operation().start();
    }
}

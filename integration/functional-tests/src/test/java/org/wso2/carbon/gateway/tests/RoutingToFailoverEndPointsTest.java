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

public class RoutingToFailoverEndPointsTest extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public void setup() throws Exception {
        gwHotDeployArtifacts("artifacts" + File.separator + "failover-endpoint.xml", "/failover_without_lb");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    @Test
    public void failOverTestWithLoadBalance() throws Exception {
        HttpServerOperationBuilderContext emulator4 = httpEmulator4();
        Thread.sleep(1000);
        HttpServerOperationBuilderContext emulator5 = httpEmulator5();
        Thread.sleep(1000);

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/failover_with_lb")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response failOver test 1",
                "Expected response not found");

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/failover_with_lb")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response failOver test 2",
                "Expected response not found");

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/failover_with_lb")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals("Response failOver test 3", response.getReceivedResponseContext().getResponseBody(),
                "Expected response not found");

        emulator4.stop();

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/failover_with_lb")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response failOver test 1",
                "Expected response not found");

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/failover_with_lb")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response failOver test 3",
                "Expected response not found");

        emulator5.stop();

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/failover_with_lb")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response failOver test 1",
                "Expected response not found");
    }

    @Test
    public void failOverTestWithoutLoadBalance() throws Exception {
        HttpServerOperationBuilderContext emulator6 = httpEmulator6();
        Thread.sleep(1000);
        HttpServerOperationBuilderContext emulator7 = httpEmulator7();
        Thread.sleep(1000);

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/failover_without_lb")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response failOver test 1",
                "Expected response not found");

        emulator6.stop();

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/failover_without_lb")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response failOver test 1",
                "Expected response not found");

        emulator7.stop();

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/failover_without_lb")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response failOver test 1",
                "Expected response not found");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwCleanup();
        emulator.stop();
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(9773).context("/services"))
                //failOver
                .when(request().withMethod(HttpMethod.GET).withPath("/failover1"))
                .then(response().withBody("Response failOver test 1").withStatusCode(HttpResponseStatus.OK)).operation()
                .start();
    }

    private HttpServerOperationBuilderContext httpEmulator4() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(9873).context("/services"))
                .when(request().withMethod(HttpMethod.GET).withPath("/failover2"))
                .then(response().withBody("Response failOver test 2").withStatusCode(HttpResponseStatus.OK)).operation()
                .start();
    }

    private HttpServerOperationBuilderContext httpEmulator5() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(9973).context("/services"))
                //failOver
                .when(request().withMethod(HttpMethod.GET).withPath("/failover3"))
                .then(response().withBody("Response failOver test 3").withStatusCode(HttpResponseStatus.OK)).operation()
                .start();
    }

    private HttpServerOperationBuilderContext httpEmulator6() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(8773).context("/services"))
                .when(request().withMethod(HttpMethod.GET).withPath("/failover2"))
                .then(response().withBody("Response failOver test 1").withStatusCode(HttpResponseStatus.OK)).operation()
                .start();
    }

    private HttpServerOperationBuilderContext httpEmulator7() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(7773).context("/services"))
                //failOver
                .when(request().withMethod(HttpMethod.GET).withPath("/failover3"))
                .then(response().withBody("Response failOver test 1").withStatusCode(HttpResponseStatus.OK)).operation()
                .start();
    }
}

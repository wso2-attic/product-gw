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

package org.wso2.carbon.gateway.httpcompliance.tests.responses.clienterror;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.gateway.httpcompliance.tests.internal.GWIntegrationTest;
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

public class HTTP413ComplianceTest extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator;
    private String host = "127.0.0.1";
    private int port = 9090;
    private String serverResponse = "413 - Request Entity Too Large";

    @BeforeClass
    public void setup() throws Exception {
        gwHotDeployArtifacts("artifacts" + File.separator + "http-compliance-test-camel-context.xml",
                "/new-route");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(6065).context("/users"))

                .when(request()
                        .withMethod(HttpMethod.GET)
                        .withPath("/user1")
                        .withBody("Payload"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE)
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user3")
                        .withBody("name=WSO2&location=Colombo10"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE)
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user1")
                        .withBody("name=WSO2&location=Colombo10"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE)
                        .withHeader("Retry-After", "60")
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.PUT)
                        .withPath("/user2")
                        .withBody("name=WSO2&location=Colombo10"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE)
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.PUT)
                        .withPath("/user3")
                        .withBody("name=WSO2&location=Colombo10"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE)
                        .withHeader("Retry-After", "60")
                        .withBody(serverResponse))

                .operation().start();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwCleanup();
        emulator.stop();
    }

    @Test
    public void test413GETRequest() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.GET)
                        .withPath("/new-route")
                        .withHeader("routeId", "r1")
                        .withBody("Payload"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse);
    }

    @Test
    public void test413POSTRequestWithPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r3")
                        .withPath("/new-route")
                        .withBody("name=WSO2&location=Colombo10"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse,
                "Response body does not match the expected response body");
    }

    @Test
    public void test413POSTRequestWithPayload2() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r1")
                        .withPath("/new-route")
                        .withBody("name=WSO2&location=Colombo10"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get("Retry-After").get(0),
                "60");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse,
                "Response body does not match the expected response body");
    }

    @Test
    public void test413PUTRequest() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.PUT)
                        .withHeader("routeId", "r2")
                        .withPath("/new-route")
                        .withBody("name=WSO2&location=Colombo10"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse,
                "Response body does not match the expected response body");
    }

    @Test
    public void test413PUTRequest2() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.PUT)
                        .withHeader("routeId", "r3")
                        .withPath("/new-route")
                        .withBody("name=WSO2&location=Colombo10"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get("Retry-After").get(0),
                "60");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse,
                "Response body does not match the expected response body");
    }
}

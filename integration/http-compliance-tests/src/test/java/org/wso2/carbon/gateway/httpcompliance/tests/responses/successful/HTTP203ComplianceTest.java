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

package org.wso2.carbon.gateway.httpcompliance.tests.responses.successful;

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

public class HTTP203ComplianceTest extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator;
    private static final String HOST = "127.0.0.1";
    private int port = 9090;
    private String requestBody = "Sample Request Body";
    private String serverResponse = "203 - Non Authorative Information";

    @BeforeClass
    public void setup() throws Exception {
        gwHotDeployArtifacts("artifacts" + File.separator + "http-compliance-test-camel-context.xml",
                "/new-route");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host(HOST).port(6065).context("/users"))

                .when(request()
                        .withMethod(HttpMethod.GET)
                        .withPath("/user1"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION)
                        .withHeader("Sample-Header", "3rd party information included")
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.HEAD)
                        .withPath("/user1"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION)
                        .withHeader("Sample-Header", "3rd party information included")
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user2")
                        .withBody(requestBody))
                .then(response()
                        .withStatusCode(HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION)
                        .withHeader("Sample-Header", "3rd party information included")
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user3"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION)
                        .withHeader("Sample-Header", "3rd party information included")
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user1")
                        .withHeader("Content-Type", "application/json"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION)
                        .withHeader("Sample-Header", "3rd party information included")
                        .withBody(serverResponse))

                .operation().start();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwCleanup();
        emulator.stop();
    }

    @Test
    public void test203GETRequest() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(9090))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.GET)
                        .withPath("/new-route")
                        .withHeader("routeId", "r1"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(),
                HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION, "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get("Sample-Header").get(0),
                "3rd party information included");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse);
    }

    @Test
    public void test203HEADRequest() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(9090))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.HEAD)
                        .withPath("/new-route")
                        .withHeader("routeId", "r1"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(),
                HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION, "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get("Sample-Header").get(0),
                "3rd party information included");

        Assert.assertNull(response.getReceivedResponseContext().getResponseBody());
    }

    @Test
    public void test203POSTRequestWithPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/new-route")
                        .withHeader("routeId", "r2")
                        .withBody(requestBody))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(),
                HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION, "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get("Sample-Header").get(0),
                "3rd party information included");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse);
    }

    @Test
    public void test203POSTRequestWithoutPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/new-route")
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r3"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(),
                HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION, "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get("Sample-Header").get(0),
                "3rd party information included");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse,
                "Response body does not match the expected response body");
    }

    @Test
    public void test203POSTRequestWithoutPayloadWithContentType() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/new-route")
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r1")
                        .withHeader("Content-Type", "application/json"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(),
                HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION, "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get("Sample-Header").get(0),
                "3rd party information included");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse,
                "Response body does not match the expected response body");
    }
}
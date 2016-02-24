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

package org.wso2.carbon.gateway.httpcompliance.tests.responses.servererror;

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

public class HTTP502ComplianceTest extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator;
    private String host = "127.0.0.1";
    private int port = 9090;
    private String servererror = "502 Bad Gateway";

    @BeforeClass
    public void setup() throws Exception {
        gwHotDeployArtifacts("artifacts" + File.separator + "http-compliance-test-camel-context.xml",
                "/new-route");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(6065).context("/users"))

                // This particular scenario is problematic on part of GW since it just returns the response it gets
                // from the emulator directly to the client, without first validating whether it is a valid response
                .when(request()
                        .withMethod(HttpMethod.GET)
                        .withPath("/user1"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.valueOf(98674))
                        .withBody(servererror))

                .when(request()
                        .withMethod(HttpMethod.GET)
                        .withPath("/user2")
                        .withBody("Body included"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.BAD_GATEWAY)
                        .withBody(servererror))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user1"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.BAD_GATEWAY)
                        .withBody(servererror))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user1")
                        .withBody("Body included"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.BAD_GATEWAY)
                        .withBody(servererror))

                .operation().start();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwCleanup();
        emulator.stop();
    }

    @Test
    public void test502GETRequest() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.GET)
                        .withPath("/new-route")
                        .withHeader("routeId", "r1"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.BAD_GATEWAY,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), servererror);
    }

    @Test
    public void test502GETRequestWithPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.GET)
                        .withPath("/new-route")
                        .withHeader("routeId", "r2")
                        .withBody("Body included"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.BAD_GATEWAY,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), servererror);
    }

    @Test
    public void test502POSTRequestWithPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/new-route")
                        .withBody("Body included")
                        .withHeader("routeId", "r1"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.BAD_GATEWAY,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), servererror);
    }

    @Test
    public void test502POSTRequestWithoutPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/new-route")
                        .withHeader("routeId", "r1"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.BAD_GATEWAY,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), servererror,
                "Response body does not match the expected response body");
    }
}

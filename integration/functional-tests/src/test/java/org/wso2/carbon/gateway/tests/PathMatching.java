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

package org.wso2.carbon.gateway.tests;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.gateway.tests.internal.GWIntegrationTest;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseProcessorContext;
import org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext;
import org.wso2.gw.emulator.http.server.contexts.HttpServerOperationBuilderContext;
import org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext;
import org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext;

import java.io.File;

import static org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext.response;

public class PathMatching extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public void setup() throws Exception {
        gwDeployArtifacts("artifacts" + File.separator + "path-matching.xml", "/baz/qux");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwCleanup();
        emulator.stop();
    }

    @Test
    public void testFooService() {

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(configure().host("localhost").port(9090))
                .when(request().withMethod(HttpMethod.GET).withPath("/foo")).then(response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK);
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "This is foo service");

    }

    @Test
    public void testPOSTFooService() {

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(configure().host("localhost").port(9090))
                .when(request().withMethod(HttpMethod.POST).withPath("/foo")).then(response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.NOT_FOUND);
        Assert.assertNull(response.getReceivedResponseContext().getResponseBody(), "Emulator returns empty body");

    }

    @Test
    public void testBarService() {

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(configure().host("localhost").port(9090))
                .when(request().withMethod(HttpMethod.GET).withPath("/bar")).then(response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK);
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "This is bar service");

    }

    @Test
    public void testFoo2Service() {

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(configure().host("localhost").port(9090))
                .when(request().withMethod(HttpMethod.GET).withPath("/foo2")).then(response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK);
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "This is foo2 service");

    }

    @Test
    public void testFoobarService() {

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(configure().host("localhost").port(9090))
                .when(request().withMethod(HttpMethod.GET).withPath("/foo/bar")).then(response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.NOT_FOUND);
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Message consumer not found.");

    }

    @Test
    public void testBarfooService() {

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(configure().host("localhost").port(9090))
                .when(request().withMethod(HttpMethod.GET).withPath("/bar/foo")).then(response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.NOT_FOUND);
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Message consumer not found.");

    }

    @Test
    public void testBazQuxService() {

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(configure().host("localhost").port(9090))
                .when(request().withMethod(HttpMethod.GET).withPath("/baz/qux")).then(response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK);
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "This is Baz Qux service");

    }

    @Test
    public void testBazService() {

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(configure().host("localhost").port(9090))
                .when(request().withMethod(HttpMethod.GET).withPath("/baz")).then(response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.NOT_FOUND);
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Message consumer not found.");

    }

    @Test
    public void testQuxService() {

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(configure().host("localhost").port(9090))
                .when(request().withMethod(HttpMethod.GET).withPath("/qux")).then(response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.NOT_FOUND);
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Message consumer not found.");

    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server()
                .given(HttpServerConfigBuilderContext.configure().host("localhost").port(9773).context("/services"))
                .when(HttpServerRequestBuilderContext.request().withMethod(HttpMethod.GET).withPath("/foo"))
                .then(HttpServerResponseBuilderContext.response().withBody("This is foo service")
                        .withStatusCode(HttpResponseStatus.OK))
                .when(HttpServerRequestBuilderContext.request().withMethod(HttpMethod.GET).withPath("/bar"))
                .then(HttpServerResponseBuilderContext.response().withBody("This is bar service")
                        .withStatusCode(HttpResponseStatus.OK))
                .when(HttpServerRequestBuilderContext.request().withMethod(HttpMethod.GET).withPath("/foo2"))
                .then(HttpServerResponseBuilderContext.response().withBody("This is foo2 service")
                        .withStatusCode(HttpResponseStatus.OK))
                .when(HttpServerRequestBuilderContext.request().withMethod(HttpMethod.GET).withPath("/baz/qux"))
                .then(HttpServerResponseBuilderContext.response().withBody("This is Baz Qux service")).operation()
                .start();
    }

}

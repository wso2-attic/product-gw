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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClient;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClientImpl;
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

/**
 * SampleTest1
 */
public class MessageRouting {
    private GatewayAdminClient gwClient;
    private HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public void setup() throws Exception {
        gwClient = new GatewayAdminClientImpl();
        gwClient.startGateway();
        gwClient.deployArtifact("artifacts" + File.separator + "message-routing.xml");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    @Test
    public void simplePassthrough() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/simple_passthrough")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response simple passthrough",
                "Expected response not found");
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

    @Test
    public void loadBalanceRoundRobinTest() throws Exception {
        HttpServerOperationBuilderContext emulator2 = httpEmulator2();
        Thread.sleep(1000);
        HttpServerOperationBuilderContext emulator3 = httpEmulator3();
        Thread.sleep(1000);

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/loadbalanced").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response loadBalance test 1",
                "Expected response not found");

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/loadbalanced").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response loadBalance test 2",
                "Expected response not found");

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/loadbalanced").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response loadBalance test 3",
                "Expected response not found");

        emulator2.stop();

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/loadbalanced").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response loadBalance test 1",
                "Expected response not found");

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/loadbalanced").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.BAD_GATEWAY,
                "Expected response code not found");

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/loadbalanced").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response loadBalance test 3",
                "Expected response not found");

        emulator3.stop();

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/loadbalanced").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Response loadBalance test 1",
                "Expected response not found");

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/loadbalanced").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.BAD_GATEWAY,
                "Expected response code not found");

        response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/loadbalanced").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.BAD_GATEWAY,
                "Expected response code not found");
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

    @Test
    public void passHeader() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/headers_in_message")
                        .withMethod(HttpMethod.GET).withHeader("Accept", "application/json"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "{data: \"Hello\"}",
                "Expected response not found");
    }

    @Test
    public void serviceChainingSoap() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/servicechaining_soap")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(),
                "Response servicechaining soap test result", "Expected response not found");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwClient.stopGateway();
        emulator.stop();
        gwClient.cleanArtifacts();
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(9773).context("/services"))
                //Simplepassthrough
                .when(request().withMethod(HttpMethod.GET).withPath("/HelloService"))
                .then(response().withBody("Response simple passthrough").withStatusCode(HttpResponseStatus.OK))
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

                //loadbalanced
                .when(request().withMethod(HttpMethod.GET).withPath("/loadbalanced1"))
                .then(response().withBody("Response loadBalance test 1").withStatusCode(HttpResponseStatus.OK))

                //failOver
                .when(request().withMethod(HttpMethod.GET).withPath("/failover1"))
                .then(response().withBody("Response failOver test 1").withStatusCode(HttpResponseStatus.OK))

                //pass header
                .when(request().withMethod(HttpMethod.GET).withPath("/customers/customerservice/customers")
                        .withHeader("Accept", "application/json"))
                .then(response().withBody("{data: \"Hello\"}").withStatusCode(HttpResponseStatus.OK))

                //soap service chaining
                .when(request().
                        withMethod(HttpMethod.GET).withPath("/"))
                .then(response().withBody("Response servicechaining soap test").withStatusCode(HttpResponseStatus.OK))

                .when(request().
                        withMethod(HttpMethod.POST).withPath("/Axis2Service").withHeader("SOAPAction", "urn:echoInt")
                        .withHeader("Content-Type", "text/xml").withBody("Response servicechaining soap test"))
                .then(response().withBody("Response servicechaining soap test result")
                        .withStatusCode(HttpResponseStatus.OK)).operation().start();
    }

    //additional emulators
    private HttpServerOperationBuilderContext httpEmulator2() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(9783).context("/services"))
                //loadbalanced
                .when(request().withMethod(HttpMethod.GET).withPath("/loadbalanced2"))
                .then(response().withBody("Response loadBalance test 2").withStatusCode(HttpResponseStatus.OK))
                .operation().start();
    }

    private HttpServerOperationBuilderContext httpEmulator3() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(9793).context("/services"))
                //loadbalanced
                .when(request().withMethod(HttpMethod.GET).withPath("/loadbalanced3"))
                .then(response().withBody("Response loadBalance test 3").withStatusCode(HttpResponseStatus.OK))
                .operation().start();
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

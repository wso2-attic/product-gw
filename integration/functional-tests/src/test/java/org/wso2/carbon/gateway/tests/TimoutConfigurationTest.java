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

public class TimoutConfigurationTest extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator, emulator2;
    private File backup = null;

    @BeforeClass
    public void setup() throws Exception {
        gwHotDeployArtifacts("artifacts" + File.separator + "simple-passthrough.xml", "/simple_passthrough");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
        emulator2 = httpEmulator();
        Thread.sleep(1000);
    }

    @Test
    public void defaultTimeOut() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/simple_passthrough")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.GATEWAY_TIMEOUT,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(),
                "<errorMessage>ReadTimeoutException occurred for endpoint localhost-9773</errorMessage>",
                "Expected response not found");
    }

    @Test
    public void manualTimeOut() throws Exception {
        backup = gwDeployTransports("artifacts" + File.separator + "netty-transports.yml");
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))
                .when(HttpClientRequestBuilderContext.request().withPath("/simple_passthrough2")
                        .withMethod(HttpMethod.GET)).then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.GATEWAY_TIMEOUT,
                "Expected response code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(),
                "<errorMessage>ReadTimeoutException occurred for endpoint localhost-9873</errorMessage>",
                "Expected response not found");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwCleanup();
        emulator.stop();
        emulator2.stop();
        if (backup != null) {
            gwRestoreFile(backup);
        }
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server()
                .given(configure().host("127.0.0.1").port(9773).context("/services").withWritingDelay(40000))
                //Simplepassthrough
                .when(request().withMethod(HttpMethod.GET).withPath("/HelloService"))
                .then(response().withBody("Response simple passthrough").withStatusCode(HttpResponseStatus.OK))
                .operation().start();
    }

    private HttpServerOperationBuilderContext httpEmulator() {
        return Emulator.getHttpEmulator().server()
                .given(configure().host("127.0.0.1").port(9873).context("/services").withWritingDelay(30000))
                //Simplepassthrough
                .when(request().withMethod(HttpMethod.GET).withPath("/HelloService"))
                .then(response().withBody("Response simple passthrough").withStatusCode(HttpResponseStatus.OK))
                .operation().start();
    }
}

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

public class HTTP205ComplianceTest extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator;
    private static final String HOST = "127.0.0.1";
    private int port = 9090;
    private static final String REQUEST_BODY = "Body included";
    private static final String RESPONSE_BODY = "";

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
                        .withMethod(HttpMethod.POST)
                        .withPath("/user1"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.RESET_CONTENT)
                        .withBody(RESPONSE_BODY))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user2")
                        .withBody(REQUEST_BODY))
                .then(response()
                        .withStatusCode(HttpResponseStatus.RESET_CONTENT)
                        .withBody(RESPONSE_BODY))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user3")
                        .withHeader("Content-Type", "application/json"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.RESET_CONTENT)
                        .withBody(RESPONSE_BODY))

                .operation().start();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwCleanup();
        emulator.stop();
    }

    @Test
    public void test205POSTRequestWithoutBody() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/new-route")
                        .withHeader("routeId", "r1"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.RESET_CONTENT,
                "Expected response code not found");

        Assert.assertNull(response.getReceivedResponseContext().getResponseBody());
    }

    @Test
    public void test205POSTRequestWithBody() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/new-route")
                        .withHeader("routeId", "r2")
                        .withBody(REQUEST_BODY))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.RESET_CONTENT,
                "Expected response code not found");

        Assert.assertNull(response.getReceivedResponseContext().getResponseBody());
    }

    @Test
    public void test205POSTRequestWithoutBodyWithContentType() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/new-route")
                        .withHeader("routeId", "r3")
                        .withHeader("Content-Type", "application/json"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.RESET_CONTENT,
                "Expected response code not found");

        Assert.assertNull(response.getReceivedResponseContext().getResponseBody());
    }
}

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

public class HTTP202ComplianceTest extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator;
    private static final String HOST = "127.0.0.1";
    private int port = 9090;
    private static final String RESPONSE_BODY = "Request accepted and will be processed in due time";
    private File clientRequestPayload;

    @BeforeClass
    public void setup() throws Exception {
        clientRequestPayload = new File(getClass().getClassLoader()
                .getResource("test-payloads" + File.separator + "client-request-payload.txt").toURI());
        gwHotDeployArtifacts("artifacts" + File.separator + "http-compliance-test-camel-context.xml",
                "/new-route");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host(HOST).port(6065).context("/users"))

                .when(request()
                        .withPath("/user1")
                        .withMethod(HttpMethod.PUT)
                        .withBody(clientRequestPayload))
                .then(response()
                        .withStatusCode(HttpResponseStatus.ACCEPTED)
                        .withBody(RESPONSE_BODY))

                .when(request()
                        .withMethod(HttpMethod.PUT)
                        .withPath("/user2")
                        .withHeader("Content-Type", "application/json"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.ACCEPTED)
                        .withBody(RESPONSE_BODY))

                .when(request()
                        .withPath("/user2")
                        .withMethod(HttpMethod.POST)
                        .withBody(clientRequestPayload))
                .then(response()
                        .withStatusCode(HttpResponseStatus.ACCEPTED)
                        .withBody(RESPONSE_BODY))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user3")
                        .withHeader("Content-Type", "application/json"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.ACCEPTED)
                        .withBody(RESPONSE_BODY))

                .operation().start();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwCleanup();
        emulator.stop();
    }

    @Test
    public void test202PUTRequest() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.PUT)
                        .withHeader("routeId", "r1")
                        .withPath("/new-route")
                        .withBody(clientRequestPayload))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.ACCEPTED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), RESPONSE_BODY);
    }

    @Test
    public void test202PUTRequestWithoutPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.PUT)
                        .withHeader("routeId", "r2")
                        .withHeader("Content-Type", "application/json")
                        .withPath("/new-route"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.ACCEPTED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), RESPONSE_BODY);
    }

    @Test
    public void test202POSTRequestWithBody() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r2")
                        .withPath("/new-route")
                        .withBody(clientRequestPayload))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.ACCEPTED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), RESPONSE_BODY);
    }

    @Test
    public void test202POSTRequestWithoutBody() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r3")
                        .withHeader("Content-Type", "application/json")
                        .withPath("/new-route"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.ACCEPTED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), RESPONSE_BODY);
    }
}

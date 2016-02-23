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
import java.util.Date;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

public class HTTP206ComplianceTest extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator;
    private static final String HOST = "127.0.0.1";
    private int port = 9090;
    private static final String RESPONSE_BODY = "";

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
                        .withPath("/user1")
                        .withHeader("Range", "bytes=0-5"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.PARTIAL_CONTENT)
                        .withHeader("Content-Range", "bytes 0-5")
                        .withHeader("Date", new Date(System.currentTimeMillis()).toString())
                        .withHeader("Cache-Control", "public")
                        .withHeader("Expires", new Date(System.currentTimeMillis() + Integer.MAX_VALUE).toString())
                        .withHeader("Etag", "34256638")
                        .withBody("ABCDE"))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user1"))
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
    public void test206GETRequest() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/new-route")
                        .withMethod(HttpMethod.GET)
                        .withHeader("routeId", "r1")
                        .withHeader("Range", "bytes=0-5"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.PARTIAL_CONTENT,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "ABCDE");
    }
}

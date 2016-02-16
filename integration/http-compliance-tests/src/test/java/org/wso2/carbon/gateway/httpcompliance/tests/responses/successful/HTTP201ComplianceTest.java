package org.wso2.carbon.gateway.httpcompliance.tests.responses.successful;

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

public class HTTP201ComplianceTest {
    private GatewayAdminClient gwClient;
    private HttpServerOperationBuilderContext emulator;
    private static final String HOST = "127.0.0.1";
    private int port = 9090;
    private static final String RESPONSE_BODY = "Created entity WSO2, located in Colombo 10";
    private static final String REQUEST_BODY = "name=WSO2&location=Colombo10";

    @BeforeClass
    public void setup() throws Exception {
        gwClient = new GatewayAdminClientImpl();
        gwClient.startGateway();
        gwClient.deployArtifact("artifacts" + File.separator + "http-compliance-test-camel-context.xml");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(6065).context("/users"))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user1")
                        .withBody("ABC"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.CREATED)
                        .withBody("XYZ"))

                .operation().start();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwClient.stopGateway();
        emulator.stop();
        gwClient.cleanArtifacts();
    }

    @Test
    public void test201POSTRequestWithPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/new-route")
                        .withHeader("routeId", "r1")
                        .withBody("ABC"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.CREATED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "XYZ");
    }

//    @Test
//    public void test201POSTRequestWithoutPayload() throws Exception {
//        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
//                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(port))
//                .when(HttpClientRequestBuilderContext.request().withPath("/new-route").withMethod(HttpMethod.POST)
//                        .withHeader("routeId", "r3"))
//                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();
//
//        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.CREATED,
//                "Expected response code not found");
//        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), RESPONSE_BODY,
//                "Response body does not match the expected response body");
//    }
}

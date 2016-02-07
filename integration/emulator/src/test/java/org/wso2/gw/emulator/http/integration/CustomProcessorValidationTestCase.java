package org.wso2.gw.emulator.http.integration;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseProcessorContext;
import org.wso2.gw.emulator.http.params.Header;
import org.wso2.gw.emulator.http.server.contexts.HttpServerOperationBuilderContext;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

/**
 * Created by dilshank on 2/6/16.
 */
public class CustomProcessorValidationTestCase {

    private HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public void setEnvironment() {
        this.emulator = startHttpEmulator();
    }

    @Test
    public void testServerRequestCustomProcessor(){
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                              .withPath("/users/user1").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User2",
                "Expected response content not found");
    }

    @Test
    public void testServerResponseCustomProcessor(){
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                              .withPath("/users/user2").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get
                        ("Header4").get(0), "value4",
                "Expected response header not found");
    }

    @Test
    public void testServerRequestResponseCustomProcessors(){
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                              .withPath("/users/user1").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User2",
                "Expected response content not found");
        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get
                        ("Header4").get(0), "value4",
                "Expected response header not found");
    }

    @Test
    public void testServerRequestResponseCustomProcessorsErrorScenario(){
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))

                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users/user5").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
    }


    @AfterClass
    public void cleanup() {
        this.emulator.stop();
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator()
                .server()
                .given(configure()
                        .host("127.0.0.1").port(6065).context("/users").withCustomRequestProcessor(new CustomRequestProcessor()))

                .when(request()
                        .withMethod(HttpMethod.GET).withPath("/user1"))
                .then(response()
                        .withBody("User1")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeader("Header1", "value1"))

                .when(request()
                        .withMethod(HttpMethod.GET).withPath("/user2"))
                .then(response()
                        .withBody("User2")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeader("Header2", "value2"))

                .when(request()
                        .withMethod(HttpMethod.GET).withPath("user4"))
                .then(response()
                              .withBody("User4")
                              .withStatusCode(HttpResponseStatus.OK)
                              .withHeaders(new Header("Header4", "value4")))
                .operation().start();
    }
}

package org.wso2.gw.emulator.http.integration.ContextPathValidation;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.dsl.Operation;
import org.wso2.gw.emulator.dsl.QueryParameterOperation;
import org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseProcessorContext;
import org.wso2.gw.emulator.http.params.Header;
import org.wso2.gw.emulator.http.params.QueryParameter;
import org.wso2.gw.emulator.http.server.contexts.HttpServerOperationBuilderContext;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

/**
 * Created by dilshank on 2/6/16.
 */
public class EmulatorWithPathValidationTestCase {

    private HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public void setEnvironment() throws InterruptedException {
        this.emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    @Test
    public void testWithClientPathFieldGETMethod() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))

                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/user1").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User1",
                "Expected response content not found");
    }

    @Test
    public void testWithClientPathFieldGETMethod2() {
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
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User1",
                "Expected response content not found");
    }

    @Test
    public void testWithoutClientPathFieldGETMethod() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User1",
                "Expected response content not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
    }

    @Test
    public void testClientPathFieldWithStarValueGETMethod() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.GET).withPath("*"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User1",
                "Expected response content not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
    }


    @Test
    public void testClientPathFieldWithoutValueGETMethod() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User1",
                "Expected response content not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
    }


    @Test
    public void testWithoutClientPathWithHeadersANDOperationWithPOST() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withBody("User4")
                        .withHeaders(
                            new Header("Header2","value2"),
                            new Header("Header3","value3"),
                            new Header("Header4","value4")))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User4",
                "Expected response content not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
    }

    @Test
    public void testWithoutClientPathWithHeadersQueryANDOperationwithPOST() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withBody("User5")
                        .withHeaders(
                                new Header("Header5","value5"),
                                new Header("Header6","value6"),
                                new Header("Header7","value7"))
                        .withQueryParameters(
                                new QueryParameter("Query1","value1"),
                                new QueryParameter("Query2","value2")))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User5",
                "Expected response content not found");

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
                        .host("127.0.0.1").port(6065))

                .when(request()
                        .withMethod(HttpMethod.GET))
                .then(response()
                        .withBody("User1")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeader("Header1", "value1"))

                .when(request()
                        .withMethod(HttpMethod.POST).withBody("User4")
                        .withHeaders(Operation.AND,
                                new Header("Header2","value2"),
                                new Header("Header3","value3"),
                                new Header("Header4","value4")))
                .then(response()
                        .withBody("User4")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeader("Header2", "value2"))

                .when(request()
                        .withMethod(HttpMethod.POST).withBody("User5")
                        .withHeaders(Operation.AND,
                                new Header("Header5","value5"),
                                new Header("Header6","value6"),
                                new Header("Header7","value7"))
                        .withQueryParameters(QueryParameterOperation.AND,
                                new QueryParameter("Query1","value1"),
                                new QueryParameter("Query2","value2")))
                .then(response()
                        .withBody("User5")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeader("Header6", "value6"))

                .operation().start();
    }
}

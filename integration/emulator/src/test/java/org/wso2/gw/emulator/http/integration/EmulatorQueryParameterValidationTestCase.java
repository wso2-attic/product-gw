package org.wso2.gw.emulator.http.integration;

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
public class EmulatorQueryParameterValidationTestCase {

    private HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public void setEnvironment() {
        this.emulator = startHttpEmulator();
    }

    @Test
    public void testSingleRequestQueryParameterFields() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users/user4").withMethod(HttpMethod.GET).withQueryParameter("Query-req-1","value-req1")
                                .withHeader("Header-req-1",
                                "value-req1"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User4",
                "Expected response content not found");
        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().size(), 2,
                "Expected response headers count is wrong");
        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get
                        ("Header5").get(0), "value5",
                "Expected response header not found");
    }

    @Test
    public void testMultipleRequestQueryFieldsWithANDOperation() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users/user5").withMethod(HttpMethod.GET).withQueryParameters(new QueryParameter
                                ("Query-req2", "value-req2"), new QueryParameter("Query-req3", "value-req3"), new QueryParameter
                                ("Query-req4", "value-req4")))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();
        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User5",
                "Expected response content not found");
        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().size(), 2,
                "Expected response headers count is wrong");
        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get
                        ("Header6").get(0), "value6",
                "Expected response header not found");
    }

    @Test
    public void testMultipleRequestQueryFieldsWithOROperation() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users/user6").withMethod(HttpMethod.GET).withQueryParameters(new QueryParameter("Query-req5", "value-req5"), new QueryParameter
                                ("Query-req6", "value-req6")))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();
        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User7",
                "Expected response content not found");
        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().size(), 2,
                "Expected response headers count is wrong");
        Assert.assertEquals(response.getReceivedResponseContext().getHeaderParameters().get
                        ("Header7").get(0), "value7",
                "Expected response header not found");
    }
    /////////////////////////
    @Test
    public void testMultipleRequestQueryFieldsWithOROperationErrorScenario() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users/user6").withMethod(HttpMethod.GET)
                        .withQueryParameter("Header-req15", "value-req15"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();
        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.NOT_FOUND,
                "Expected response status code not found");
    }

    @Test
    public void testMultipleRequestQueryFieldsWithANDOperationErrorScenario() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users/user5").withMethod(HttpMethod.GET).withQueryParameters(new QueryParameter
                                ("Query-req2", "value-req2"), new QueryParameter("Query-req3", "value-req3"), new QueryParameter
                                ("Query-req14", "value-req14"), new QueryParameter
                                ("Query-req5", "value-req5")))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();
        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.NOT_FOUND,
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
                        .host("127.0.0.1").port(6065).context("/users"))


                .when(request()
                        .withMethod(HttpMethod.GET).withPath("user1/"))
                .then(response()
                        .withBody("User1")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeader("Header1", "value1"))


                .when(request()
                        .withMethod(HttpMethod.GET).withPath("user2/"))
                .then(response()
                        .withBody("User2")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeaders(new Header("Header2", "value2")))


                .when(request()
                        .withMethod(HttpMethod.GET).withPath("user3/"))
                .then(response()
                        .withBody("User3")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeaders(new Header("Header3", "value3"), new Header("Header4", "value4")))


                .when(request()
                        .withMethod(HttpMethod.GET).withPath("user4/").withQueryParameter("Query-req-1", "value-req1"))
                .then(response()
                        .withBody("User4")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeader("Header5", "value5"))


                .when(request()
                        .withMethod(HttpMethod.GET)
                        .withPath("user5/")
                        .withQueryParameters(
                                QueryParameterOperation.AND,
                                new QueryParameter("Query-req2", "value-req2"),
                                new QueryParameter("Query-req3", "value-req3"),
                                new QueryParameter("Query-req4", "value-req4")))
                .then(response()
                        .withBody("User5")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeader("Header6", "value6"))


                .when(request()
                        .withMethod(HttpMethod.GET).withPath("user6/").withQueryParameters(QueryParameterOperation.OR, new QueryParameter
                                ("Query-req3", "value-req3"), new QueryParameter("Query-req5", "value-req5"), new QueryParameter
                                ("Query-req6", "value-req6")))
                .then(response()
                        .withBody("User7")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeader("Header7", "value7"))


                .operation().start();
    }
}


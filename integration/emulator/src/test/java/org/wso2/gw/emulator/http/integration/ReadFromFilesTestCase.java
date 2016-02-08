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

import java.io.File;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

/**
 * Created by dilshank on 2/7/16.
 */
public class ReadFromFilesTestCase {

    private HttpServerOperationBuilderContext emulator;

    @Test
    public void testClientANDServerRequestFromTestFile(){

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users/user1").withMethod(HttpMethod.POST)
                        .withBody("User1")
                        .withBody(new File("/home/dilshank/product/02-01/product-gw/integration/emulator/file/test1clientRequest.txt")))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User1",
                "Expected response content not found");
    }

    @Test
    public void testServerResponseFromFile(){

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users/user2").withMethod(HttpMethod.POST).withBody("User2"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User2",
                "Expected response content not found");
    }

    @Test
    public void testServerRequestResponseFromTestFile(){

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users/user3").withMethod(HttpMethod.POST)
                        .withBody("User3"))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User3",
                "Expected response content not found");
    }

    @Test
    public void testServerRequestResponseANDClientRequestFromTestFile(){

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users/user4").withMethod(HttpMethod.POST)
                        .withBody(new File("/home/dilshank/product/02-01/product-gw/integration/emulator/file/test4clientRequest.txt")))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User4",
                "Expected response content not found");

    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator()
                .server()
                .given(configure()
                        .host("127.0.0.1").port(6065).context("/users"))

                .when(request()
                        .withMethod(HttpMethod.POST).withPath("/user1")
                        .withBody("User1")
                        .withBody(new File("/home/dilshank/product/02-01/product-gw/integration/emulator/file/test1serverRequest.txt")))
                .then(response()
                        .withBody("User1")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeader("Header1", "value1"))


                .when(request()
                        .withMethod(HttpMethod.POST).withPath("/user2").withBody("User2"))
                .then(response()
                        .withBody(new File("/home/dilshank/product/02-01/product-gw/integration/emulator/file/test2serverResponse.txt"))
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeaders(new Header("Header2", "value2")))


                .when(request()
                        .withMethod(HttpMethod.POST).withPath("/user3")
                        .withBody(new File("/home/dilshank/product/02-01/product-gw/integration/emulator/file/test3serverRequest.txt")))
                .then(response()
                        .withBody("User3")
                        .withBody(new File("/home/dilshank/product/02-01/product-gw/integration/emulator/file/test3serverResponse.txt"))
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeaders(new Header("Header3", "value3")))


                .when(request()
                        .withMethod(HttpMethod.POST).withPath("/user4")
                        .withBody(new File("/home/dilshank/product/02-01/product-gw/integration/emulator/file/test4serverRequest.txt")))
                .then(response()
                        .withBody("User4")
                        .withBody(new File("/home/dilshank/product/02-01/product-gw/integration/emulator/file/test4serverResponse.txt"))
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeaders(new Header("Header4", "value4")))

                .operation().start();
    }

    @BeforeClass
    public void setEnvironment() {
        this.emulator = startHttpEmulator();
    }

    @AfterClass
    public void cleanup() {
        this.emulator.stop();
    }
}

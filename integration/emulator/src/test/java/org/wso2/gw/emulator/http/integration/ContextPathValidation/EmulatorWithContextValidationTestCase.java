
package org.wso2.gw.emulator.http.integration.ContextPathValidation;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.dsl.Operation;
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
 * Created by dilshank on 2/5/16.
 */

public class EmulatorWithContextValidationTestCase {

    private HttpServerOperationBuilderContext emulator;

    @BeforeClass
    public void setEnvironment() {
        this.emulator = startHttpEmulator();
    }

    @Test
    public void testWithPathField() {
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
    public void testWithContextValue() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))

                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.OK,
                "Expected response status code not found");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User2",
                "Expected response content not found");
    }


@Test
    public void testWithoutPathField() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.NOT_FOUND,
                "Expected response status code not found");
    }


    @Test
    public void testPathFieldWithoutValue() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                .operation().send();

        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(), HttpResponseStatus.NOT_FOUND,
                "Expected response status code not found");
    }

    @Test
    public void testPathWithErrorScenario() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/users1/user56").withMethod(HttpMethod.GET))
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
                        .withMethod(HttpMethod.GET))
                .then(response()
                        .withBody("User2")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeaders(new Header("Header2", "value2")))

                .when(request()
                        .withMethod(HttpMethod.GET).withPath(""))
                .then(response()
                        .withBody("User3")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeaders(new Header("Header3", "value3")))

                .when(request()
                        .withMethod(HttpMethod.GET).withPath("user4"))
                .then(response()
                        .withBody("User4")
                        .withStatusCode(HttpResponseStatus.OK)
                        .withHeaders(new Header("Header4", "value4")))



                .operation().start();
    }
}


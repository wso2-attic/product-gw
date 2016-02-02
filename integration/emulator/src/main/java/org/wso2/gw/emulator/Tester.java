/*
 * *
 *  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.gw.emulator;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
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

import java.io.FileNotFoundException;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;


public class Tester {

    public static void main(String[] args) throws Exception {
        HttpServerOperationBuilderContext serverOperationBuilderContext = startHttpEmulator();
        Thread.sleep(1000);
        //testProducer1();
        //testProducer2();
        testProducer3();
        //testProducer4();
        //testProducer5();
        serverOperationBuilderContext.stop();

    }

    private static HttpServerOperationBuilderContext startHttpEmulator() throws FileNotFoundException {
        return Emulator.getHttpEmulator()
                .server()
                .given(configure()
                                .host("127.0.0.1")
                                .port(6065)
                                .context("/user")
                                .withReadingDelay(1000)
                                .withWritingDelay(1000)
                                .randomConnectionClose(false)
                        //.withLogicDelay(1000)
                        //.withCustomProcessor(true)
                )
                //test producer1
                /*.when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest")
                        .withHeader("Header1", "value1")
                        .withPath("/wso2")
                )
                .then(response()
                        .withBody("TestResponse")
                        .withHeader("Header1", "value1")
                        .withStatusCode(HttpResponseStatus.OK)
                )
               ////////////////////////////////test producer2
                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest1")
                        .withHeader("Header1","value1")
                        .withPath("/wso2")
                )
                .then(response()
                        .withBody("TestResponse1")
                        .withHeader("Header1", "value1")
                        .withStatusCode(HttpResponseStatus.OK)
                )*/
                //test producer3
                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest1")
                        .withPath("/wso2")
                        .withHeaders(
                                Operation.OR,
                                new Header("Header1", "value1"),
                                new Header("Header2", "value2"))
                        .withQueryParameters(
                                QueryParameterOperation.OR,
                                new QueryParameter("Query1", "value1"),
                                new QueryParameter("Query2", "value2")

                        )
                )
                .then(response()
                        .withBody("This is response for @{body} with @{header.Header1} @{query.Query1}")
                        .withHeaders(
                                new Header("Header1", "value1"),
                                new Header("Header2", "value2"))
                        .withStatusCode(HttpResponseStatus.OK))

                ///////////////////////////////
                .when(request()
                        .withMethod(HttpMethod.POST).withBody("test")
                )
                .then(response()
                        .withBody("Test Response2").withStatusCode(HttpResponseStatus.OK))
               /* .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest1")
                        .withHeaders(
                                Operation.AND,
                                new Header("Header1", "value1"),
                                new Header("Header2", "value2"))
                        .withPath("")

                )
                .then(response()
                        .withBody("TestResponse1")
                        .withHeaders(
                                new Header("Header1", "value1"),
                                new Header("Header2", "value2"))
                        .withStatusCode(HttpResponseStatus.OK))

                ////////////////////////////////
                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest111")
                        .withHeaders(
                                Operation.AND,
                                new Header("header1", "value1"),
                                new Header("header2", "value2"))
                        .withQueryParameter("query1", "value1")
                )
                .then(response()
                        .withBody("This is response for @{body} with @{header.name2} @{header.name2}")
                        .withHeaders(
                                new Header("header1", "value1"),
                                new Header("header2", "value2"))
                        .withStatusCode(HttpResponseStatus.OK))
                /////////////////////////////////////
                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest1")
                        .withHeader("Header1", "value1")
                        .withQueryParameter("Query1", "value1")
                )
                .then(response()
                        .withBody("TestResponse1")
                        .withHeader("Header1","value1")
                        .withStatusCode(HttpResponseStatus.OK)
                )
               */ //test producer 5
                /*.when(request()
                        .withMethod(HttpMethod.GET))
                .then(response().withBody("Test Response1").withStatusCode(HttpResponseStatus.OK))
*/
                /*.when(request()
                        //.withPath("/wso2")
                        .withMethod(HttpMethod.POST)
                        .withBody("test"))
                .then(response()
                        .withBody("Test Response2")
                        .withStatusCode(HttpResponseStatus.OK))

                */.operation().start();
    }

    private static HttpClientResponseProcessorContext testProducer3() throws FileNotFoundException {
        return Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1")
                        .port(6065)
                        .WithReadingDelay(1000)
                )
                .when(HttpClientRequestBuilderContext.request()
                                .withPath("/user/wso2")
                                .withMethod(HttpMethod.POST)
                                .withBody("TestRequest1")
                                .withHeaders(
                                        Operation.OR,
                                        new Header("Header1", "value1"),
                                        new Header("Header2", "value2")
                                ).withQueryParameters(
                                new QueryParameter("Query1", "value1"),
                                new QueryParameter("Query2", "value2")
                                )

                        //.withQueryParameter("q1","q1")
                )
                .then(HttpClientResponseBuilderContext.response()
                        .withBody("This is response for TestRequest1 with value1 value1")
                        .withHeaders(
                                new Header("Header1", "value1"),
                                new Header("Header2", "value3"))
                )
                .operation().send();
    }

    /*private static HttpClientResponseProcessorContext testProducer() {
        return Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                                .host("127.0.0.1").port(6065))
                .when(HttpClientRequestBuilderContext.request()
                              .withPath("/user").withMethod(HttpMethod.GET))
                .then(HttpClientResponseBuilderContext.response().withBody("Test Response1"))
                .when(HttpClientRequestBuilderContext.request()
                              .withPath("/user").withMethod(HttpMethod.POST).withBody("test"))
                .then(HttpClientResponseBuilderContext.response().withBody("Test Response2").assertionIgnore())
                .operation().send();
    }*/

    private static HttpClientResponseProcessorContext testProducer1() throws FileNotFoundException {
        return Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1")
                        .port(6065)
                        .WithReadingDelay(1000)
                )
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/user/wso2")
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest")
                        .withHeader("Header1", "value1")

                )
                .then(HttpClientResponseBuilderContext.response()
                        .withBody("TestResponse")
                        .withHeader("Header1", "value1")

                )

                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/user/wso2")
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest1")
                        .withHeader("Header1", "value1")

                )
                .then(HttpClientResponseBuilderContext.response()
                        .withBody("TestResponse1")
                        .withHeader("Header1", "value1")
                        .assertionIgnore()

                )
                .operation().send();
    }



    private static HttpClientResponseProcessorContext testProducer2() throws FileNotFoundException {
        return Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1")
                        .port(6065)
                        .WithReadingDelay(1000)
                )
                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/user/wso2")
                        .withMethod(HttpMethod.POST)
                        .withBody("TestRequest1")
                        .withHeader("Header1", "value1")
                )
                .then(HttpClientResponseBuilderContext.response()
                        .withBody("TestResponse1")
                        .withHeader("Header1", "value1")
                )
                .operation().send();
    }



    private static HttpClientResponseProcessorContext testProducer4() throws FileNotFoundException {
        return Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1")
                        .port(6065)
                        .WithReadingDelay(1000)
                )
                .when(HttpClientRequestBuilderContext.request()
                                .withPath("*")
                                .withMethod(HttpMethod.POST)
                                .withBody("TestRequest1")
                                .withHeader("Heder1","value1")
                                .withQueryParameter("q1","q1")
                )
                .then(HttpClientResponseBuilderContext.response()
                        .withBody("TestResponse1")
                        .withHeader("Header1", "value1")
                )
                .operation().send();
    }

    private static HttpClientResponseProcessorContext testProducer5() throws FileNotFoundException {
        return Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                                .host("127.0.0.1")
                                .port(6065)
                        //.withReadingDelay(1000)
                )
               /* .when(HttpClientRequestBuilderContext.request()
                        .withPath("/user")
                        .withMethod(HttpMethod.GET)
                )
                .then(HttpClientResponseBuilderContext.response()
                        .withBody("Test Response1")
                )*/
                .when(HttpClientRequestBuilderContext.request().withPath("")
                        //.withPath("/user/wso2")
                        .withMethod(HttpMethod.POST)
                        .withBody("test")
                )
                .then(HttpClientResponseBuilderContext.response()
                        .withBody("Test Response2")
                )
                .operation().send();
    }
}

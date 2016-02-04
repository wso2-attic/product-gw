package test.http.contxtPath;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpResponseContext;
import org.wso2.gw.emulator.http.params.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

/**
 * Created by dilshank on 2/3/16.
 */
public class WithContextTest {

    @BeforeClass
    public void setEnvironment(){
        Emulator.getHttpEmulator()
                .server()
                .given(configure()
                        .host("127.0.0.1")
                        .port(6065)
                        .context("/user")
                )
                .when(request()
                        .withBody("Test Request1")
                        .withMethod(HttpMethod.POST))
                .then(response()
                        .withBody("Test Response1")
                        .withHeader("Header1","Value1")
                        .withStatusCode(HttpResponseStatus.OK))
        .operation().start();
    }

    @Test(description = "test without path")
    public void withoutPath(){
        HttpResponseContext response = Emulator.getHttpEmulator()
                .client()
                .given(HttpClientConfigBuilderContext.configure()
                        .host("127.0.0.1")
                        .port(6065)
                )
                .when(HttpClientRequestBuilderContext.request()
                        .withBody("Test Request1")
                        .withPath("/user")
                        .withMethod(HttpMethod.POST)
                )
                .then(HttpClientResponseBuilderContext.response()
                        .withBody("Test Response1")
                        .assertionIgnore()
                )
                .operation().send().getReceivedResponseContext();

        Map<String, List<String>> headerMap = new HashMap<>();
        List<String> valueSet = new ArrayList<>();

        valueSet.add("Value1");
        headerMap.put("Header1", valueSet);

        // headerList.add(new Header("Header2","Value2"));

        //Assert.assertEquals(response.getResponseBody(),"Test Response1");
        Assert.assertEquals(response.getHeaderParameters(), headerMap);
    }

}

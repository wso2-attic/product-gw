package org.wso2.gw.emulator.http.server.contexts;

import io.netty.handler.codec.http.HttpMethod;
import org.wso2.gw.emulator.dsl.CookieOperation;
import org.wso2.gw.emulator.dsl.Operation;
import org.wso2.gw.emulator.dsl.QueryParameterOperation;
import org.wso2.gw.emulator.dsl.contexts.AbstractRequestBuilderContext;
import org.wso2.gw.emulator.util.FileRead;
import org.wso2.gw.emulator.http.params.Cookie;
import org.wso2.gw.emulator.http.params.Header;
import org.wso2.gw.emulator.http.params.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpServerRequestBuilderContext extends AbstractRequestBuilderContext {

    private static HttpServerRequestBuilderContext serverRequest;
    private HttpMethod method;
    private String path;
    private String body;
    private String context;
    private Header header;
    private Pattern pathRegex;
    private QueryParameter queryParameter;
    private Cookie cookie;
    private List<Header> headers;
    private List<QueryParameter> queryParameters;
    private List<Cookie> cookies;
    private Operation operation;
    private CookieOperation cookieOperation;
    private QueryParameterOperation queryOperation;



    private static HttpServerRequestBuilderContext getInstance() {
        serverRequest = new HttpServerRequestBuilderContext();
        return serverRequest;
    }

    public static HttpServerRequestBuilderContext request() {
        return getInstance();
    }

    public HttpServerRequestBuilderContext withMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpServerRequestBuilderContext withPath(String path) {
        this.path = path;
        return this;
    }

    public HttpServerRequestBuilderContext withBody(String body) {
        this.body = body;
        return this;
    }

    public HttpServerRequestBuilderContext withBody(File filePath) {
        try {
            this.body = FileRead.getFileBody(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //this.body = body;
        return this;
    }

    public HttpServerRequestBuilderContext withHeader(String name, String value) {
        this.header = new Header(name, value);
        if (headers == null) {
            headers = new ArrayList<Header>();
        }
        headers.add(header);
        return this;
    }

    public Operation getOperation() {
        return operation;
    }

    public CookieOperation getCookieOperation() {
        return cookieOperation;
    }

    public HttpServerRequestBuilderContext withHeaders(Operation operation, Header... headers) {
        this.operation = operation;
        this.headers = Arrays.asList(headers);
        return this;
    }


    public HttpServerRequestBuilderContext withQueryParameter(String name, String value) {
        this.queryParameter = new QueryParameter(name, value);
        if (queryParameters == null) {
            queryParameters = new ArrayList<QueryParameter>();
        }
        queryParameters.add(queryParameter);
        return this;
    }


    public QueryParameterOperation getQueryOperation() {
        return queryOperation;
    }

    public HttpServerRequestBuilderContext withQueryParameters(QueryParameterOperation queryOperation, QueryParameter... queryParameters) {
        this.queryOperation = queryOperation;
        this.queryParameters = Arrays.asList(queryParameters);
        return this;
    }

    public HttpServerRequestBuilderContext withCookie(String name, String value) {
        if (cookie == null) {
            this.cookies = new ArrayList<Cookie>();
        }
        this.cookies.add(new Cookie(name, value));
        return this;
    }

    public HttpServerRequestBuilderContext withCookies(CookieOperation cookieOperation, Cookie... cookies) {
        this.cookieOperation = cookieOperation;
        this.cookies = Arrays.asList(cookies);
        return this;
    }

    public HttpServerRequestBuilderContext withCustomProcessor(String CustomRequestProcessor) {
        return this;
    }

    public boolean isMatch(HttpRequestContext requestContext) {
        if (isContextMatch(requestContext) && isHttpMethodMatch(requestContext) && isQueryParameterMatch(requestContext) && isRequestContentMatch(requestContext) &&
                isHeadersMatch(requestContext)) {
            return true;
        }
        return false;
    }

    public void buildPathRegex(String context) {
        this.context = context;
        String regex = buildRegex(context, path);
        this.pathRegex = Pattern.compile(regex);
    }

    private boolean isContextMatch(HttpRequestContext requestContext) {
        this.context = extractContext(requestContext.getUri());
        return pathRegex.matcher(context).find();
    }

    private boolean isHttpMethodMatch(HttpRequestContext requestContext) {
        if (method == null) {
            return true;
        }

        if (method.equals(requestContext.getHttpMethod())) {
            return true;
        }
        return false;
    }

    private boolean isRequestContentMatch(HttpRequestContext requestContext) {
        if (body == null || body.isEmpty()) {
            return true;
        }

        if (body.equalsIgnoreCase(requestContext.getRequestBody())) {
            return true;
        }
        return false;
    }

    private boolean isHeadersMatch(HttpRequestContext requestContext) {

        if (headers == null) {
            return true;
        }
        Operation operation = getOperation();
        Map<String, List<String>> headerParameters = requestContext.getHeaderParameters();

        if (operation == Operation.OR) {
            if((headerParameters == null || headerParameters.isEmpty()) && (headers != null || !headers.isEmpty())) {
                return false;
            }
            for (Header header : headers) {
                List<String> headerValues = headerParameters.get(header.getName());
                String value = header.getValue();
                if (headerValues != null && headerValues.contains(value)) {
                    return true;
                }
            }
        }else {
            for (Header header : headers) {
                if(headerParameters.get(header.getName()) == null) {
                    return false;
                }

                if (!headerParameters.get(header.getName()).contains(header.getValue())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isQueryParameterMatch(HttpRequestContext requestContext) {
        if (queryParameters == null) {
            return true;
        }

        Map<String, List<String>> queryParametersMap = requestContext.getQueryParameters();
        boolean x = false;
        if (queryOperation == QueryParameterOperation.OR) {
            for (QueryParameter query : queryParameters) {
                List<String> queryParameterValues = queryParametersMap.get(query.getName());
                String value = query.getValue();
                if (queryParameterValues == null) {
                    //continue;
                }
                else if (queryParameterValues.contains(value)) {
                    x = true;
                    break;
                }
            }
            if (x == true){
                return true;
            }else{
                return false;
            }
        }else {
            List<String> queryParameterValues = null;
            String value = null;

            for (QueryParameter query : queryParameters) {

                if (queryParametersMap.get(query.getName()) != null){
                    queryParameterValues = queryParametersMap.get(query.getName());
                    value = query.getValue();
                }else {
                    return false;
                }

                if (!queryParameterValues.contains(value)) {
                    return false;
                }
            }
            return true;
        }

        /*List<String> queryValues = queryParameters.get(queryParameter.getName());

        if (queryParameters == null || queryValues == null || queryValues.isEmpty()) {
            return false;
        }

        for (String value : queryValues) {
            if (value.equalsIgnoreCase(queryParameter.getValue())) {
                return true;
            }
        }*/

    }

    private String buildRegex(String context, String path) {
        String fullPath = "";

        if ((context == null || context.isEmpty()) && (path == null || path.isEmpty())) {
            return ".*";
        }

        if ((context == "*") && (path == "*")) {
            return ".*";
        }

        if (context != null && !context.isEmpty() && path == "*") {
            fullPath = context;

            if (!fullPath.startsWith("/")) {
                fullPath = "/" + fullPath;
            }

            /*if (!fullPath.endsWith("/")) {
                fullPath = fullPath + "/";
            }*/

            fullPath = fullPath + ".*";
            return fullPath;
        }

        if (context != null && !context.isEmpty()) {
            fullPath = context;

            if (!fullPath.startsWith("/")) {
                fullPath = "/" + fullPath;
            }

            if (!fullPath.endsWith("/")) {
                fullPath = fullPath + "/";
            }
        } else {
            fullPath = ".*";
        }

        if (path != null && !path.isEmpty()) {
            if (fullPath.endsWith("/") && path.startsWith("/")) {
                fullPath = fullPath + path.substring(1);

            } else if (fullPath.endsWith("/") && !path.startsWith("/")) {
                fullPath = fullPath + path;

            } else if (!fullPath.endsWith("/") && path.startsWith("/")) {
                fullPath = fullPath + path;

            } else {
                fullPath = fullPath + "/" + path;
            }
        } else {
            fullPath = fullPath + ".*";
        }

        if (fullPath.endsWith("/")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
        }
        return "^" + fullPath + "$";
    }

    private String extractContext(String uri) {


        if (uri == null || uri.isEmpty()) {
            return null;
        }
        if (!uri.contains("?")) {
            /*if (!uri.endsWith("/")) {
                uri = uri + "/";
            }*/

            if (path == null || path.isEmpty()){
                uri = uri + "/";
            }
            return uri;
        }
        uri = uri.split("\\?")[0];
        /*if (!uri.endsWith("/")) {
            uri = uri + "/";
        }*/
        if (path == null || path.isEmpty()){
            uri = uri + "/";
        }
        return uri;
    }
}

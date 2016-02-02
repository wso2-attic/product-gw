package org.wso2.gw.emulator.dsl.processors;


import org.wso2.gw.emulator.http.server.contexts.HttpRequestContext;

public abstract class AbstractCustomProcessor {

    public abstract HttpRequestContext process(HttpRequestContext requestContext);
}

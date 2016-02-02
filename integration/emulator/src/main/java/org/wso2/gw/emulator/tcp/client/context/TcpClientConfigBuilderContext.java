package org.wso2.gw.emulator.tcp.client.context;

import org.wso2.gw.emulator.dsl.contexts.AbstractConfigurationBuilderContext;

/**
 * Created by dilshank on 1/4/16.
 */
public class TcpClientConfigBuilderContext extends AbstractConfigurationBuilderContext {

    private static TcpClientConfigBuilderContext clientConfigBuilderContext;
    private String host;
    private int port;
    private int readingDelay;


    private static TcpClientConfigBuilderContext getInstance() {
        clientConfigBuilderContext = new TcpClientConfigBuilderContext();
        return clientConfigBuilderContext;
    }

    public static TcpClientConfigBuilderContext configure() {
        return getInstance();
    }

    @Override
    public TcpClientConfigBuilderContext host(String host) {
        this.host=host;
        return this;
    }

    @Override
    public TcpClientConfigBuilderContext port(int port) {
        this.port=port;
        return this;
    }

    public TcpClientConfigBuilderContext readingDelay(int readingDelay){
        this.readingDelay = readingDelay;
        return this;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getReadingDelay() {
        return readingDelay;
    }


}

package test.http.reverseproxy;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ReverseProxyInitializer extends ChannelInitializer<SocketChannel> {
    private final String remoteHost;
    private final int remotePort;

    public ReverseProxyInitializer(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new SourceHandler(remoteHost, remotePort));
    }
}

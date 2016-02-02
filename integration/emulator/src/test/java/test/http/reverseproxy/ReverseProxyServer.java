package test.http.reverseproxy;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


public final class ReverseProxyServer {

    static final int LOCAL_PORT = 6060;
    static final String REMOTE_HOST = "http://127.0.0.1";
    static final int REMOTE_PORT = 9000;

    public static void main(String[] args) throws Exception {


        /* Multithreaded event loop that handles I/O operation */
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);    /* BossGroup - Accepts the incoming connections */
        EventLoopGroup workerGroup = new NioEventLoopGroup();   /* WorkerGroup - Handles the IO traffic of the accepted connections */

        try {
            ServerBootstrap b = new ServerBootstrap();          /* Sets up the server */
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)      /* NIO selector based implementation to accept new connections*/
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ReverseProxyInitializer(REMOTE_HOST, REMOTE_PORT))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(LOCAL_PORT).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }



    }
}


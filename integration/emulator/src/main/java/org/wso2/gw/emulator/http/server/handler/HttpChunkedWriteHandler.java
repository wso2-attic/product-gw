package org.wso2.gw.emulator.http.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.log4j.Logger;
import org.wso2.gw.emulator.http.server.contexts.HttpServerInformationContext;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * HttpChunkedWriteHandler
 */
public class HttpChunkedWriteHandler extends ChunkedWriteHandler {
    private static final Logger log = Logger.getLogger(HttpChunkedWriteHandler.class);
    private final HttpServerInformationContext serverInformationContext;
    private final ScheduledExecutorService scheduledWritingExecutorService;
    private final int corePoolSize = 10;

    public HttpChunkedWriteHandler(HttpServerInformationContext serverInformationContext) {
        this.serverInformationContext = serverInformationContext;
        scheduledWritingExecutorService = Executors.newScheduledThreadPool(corePoolSize);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        waitingDelay(serverInformationContext.getServerConfigBuilderContext().getWritingDelay());
    }

    private void waitingDelay(int delay) {
        if (delay != 0) {

            ScheduledFuture scheduledWaitingFuture = scheduledWritingExecutorService.schedule(callable, delay,
                    TimeUnit.MILLISECONDS);
            try {
                scheduledWaitingFuture.get();
            } catch (InterruptedException e) {
                log.error(e);
            } catch (ExecutionException e) {
                log.error(e);
            }
            //scheduledWritingExecutorService.shutdown();
        }
    }

    static Callable callable = new Callable() {
        public Object call() throws Exception {
            return "Writing";
        }
    };
}

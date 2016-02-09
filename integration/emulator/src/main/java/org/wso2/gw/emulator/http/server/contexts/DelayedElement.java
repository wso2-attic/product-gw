package org.wso2.gw.emulator.http.server.contexts;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Delay element of the fast backend
 * */
public class DelayedElement implements Delayed {
    private long DELAY;
    private ChannelHandlerContext ctx;
    private HttpServerProcessorContext context;
    protected long timestamp;

    public DelayedElement(ChannelHandlerContext ctx, HttpServerProcessorContext context, long receivedTime, int delay) {
        DELAY = delay;
        this.ctx = ctx;
        this.context = context;
        this.timestamp = receivedTime;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return DELAY - (System.currentTimeMillis() - this.timestamp);
    }

    @Override
    public int compareTo(Delayed other) {
        long comparison = ((DelayedElement) other).timestamp - this.timestamp;
        if (comparison > 0) {
            return -1;
        } else if (comparison < 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public ChannelHandlerContext getContext() {
        return ctx;
    }

    public HttpServerProcessorContext getProcessorContext() {
        return this.context;
    }
}

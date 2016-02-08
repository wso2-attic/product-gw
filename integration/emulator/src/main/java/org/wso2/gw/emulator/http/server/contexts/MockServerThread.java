package org.wso2.gw.emulator.http.server.contexts;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.concurrent.DelayQueue;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by dilshank on 1/6/16.
 */
public class MockServerThread extends Thread {

    private final DelayQueue<DelayedElement> queue = new DelayQueue<DelayedElement>();

    public void run() {
        DelayedElement elem = null;

        while(true) {
            try {
                elem = (DelayedElement) queue.take();
                beginResponse(elem.getContext(), elem.getProcessorContext());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void delayEvent(ChannelHandlerContext ctx, HttpServerProcessorContext processorContext, int delay, int id) {
        DelayedElement delayedElement = new DelayedElement(ctx, processorContext, System.currentTimeMillis(), delay);
        queue.add(delayedElement);
    }

    private void beginResponse(ChannelHandlerContext ctx, HttpServerProcessorContext context) {
        writeResponse(ctx, context);
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ChannelFuture future = ctx.channel().write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    private void writeResponse( ChannelHandlerContext ctx, HttpServerProcessorContext context) {
        // Decide whether to close the connection or not.
        HttpRequest request = context.getHttpRequest();

        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = context.getFinalResponse();

        if (keepAlive) {
            ctx.write(response);
        }else{
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }
        ctx.channel().flush();
    }

}

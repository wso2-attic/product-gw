/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package test.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostStandardRequestDecoder;

//import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class SOAP11EmulatorHandler extends ChannelHandlerAdapter {


    public static final String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "   <soapenv:Body>\n" +
            "      <ns:getQuoteResponse xmlns:ns=\"http://services.samples\">\n" +
            "         <ns:return xsi:type=\"ax21:GetQuoteResponse\" xmlns:ax21=\"http://services.samples/xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "            <ax21:change>4.2412907989646635</ax21:change>\n" +
            "            <ax21:earnings>13.042325817705265</ax21:earnings>\n" +
            "            <ax21:high>69.39043856850702</ax21:high>\n" +
            "            <ax21:last>67.19669368876177</ax21:last>\n" +
            "            <ax21:lastTradeTimestamp>Wed Jul 01 12:18:12 IST 2015</ax21:lastTradeTimestamp>\n" +
            "            <ax21:low>-65.75132906450288</ax21:low>\n" +
            "            <ax21:marketCap>1.1862627545001213E7</ax21:marketCap>\n" +
            "            <ax21:name>WSO2 Company</ax21:name>\n" +
            "            <ax21:open>-65.87526914139053</ax21:open>\n" +
            "            <ax21:peRatio>23.54148767939302</ax21:peRatio>\n" +
            "            <ax21:percentageChange>-6.3259733826534355</ax21:percentageChange>\n" +
            "            <ax21:prevClose>-67.04566305313239</ax21:prevClose>\n" +
            "            <ax21:symbol>WSO2</ax21:symbol>\n" +
            "            <ax21:volume>9670</ax21:volume>\n" +
            "         </ns:return>\n" +
            "      </ns:getQuoteResponse>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";


    private static final byte[] CONTENT = payload.getBytes();




   // @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

   // @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

       /* if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            System.out.println("REQ : " + req.toString() );
            if (HttpHeaderUtil.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }

            boolean keepAlive = HttpHeaderUtil.isKeepAlive(req);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
            response.headers().set(CONTENT_TYPE, "text/xml");
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.write(response);
            }
        } else if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            System.out.println("Body : " +
                    httpContent.content().toString());
        }*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

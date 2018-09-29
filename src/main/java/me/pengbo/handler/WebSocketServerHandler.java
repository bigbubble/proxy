package me.pengbo.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * <p>
 *
 * </p >
 *
 * @author pengb
 * @since 2018-09-29 11:20
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame o) throws Exception {
        String msg = o.text();
        if("_ping".equals(msg)){
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("_pong"));
        }else{
            System.out.println(msg);
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("received:" + msg));
        }
    }
}

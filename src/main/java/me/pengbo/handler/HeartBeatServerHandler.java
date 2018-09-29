package me.pengbo.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * <p>
 *
 * </p >
 *
 * @author pengb
 * @since 2018-09-29 14:00
 */
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state()== IdleState.READER_IDLE){
                System.out.println("关闭这个不活跃通道！");
                ctx.channel().close();
            }
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }
}

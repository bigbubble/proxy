package me.pengbo.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import me.pengbo.Global;

import java.util.Map;

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
                ctx.channel().close();
                for(Map.Entry<String, Channel> entry : Global.channelContextMap.entrySet()) {
                    String key = entry.getKey();
                    Channel context = entry.getValue();
                    if(context.equals(ctx)){
                        System.out.println("关闭这个不活跃通道：" + key);
                        Global.channelContextMap.remove(key);
                        break;
                    }
                }
            }
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }
}

package me.pengbo.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.pengbo.Global;
import me.pengbo.model.Message;

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
                Channel channel = ctx.channel();
                channel.close();
                String removeKey = null;
                for(Map.Entry<String, Channel> entry : Global.channelContextMap.entrySet()) {
                    Channel existChannel = entry.getValue();
                    if(existChannel.equals(channel)){
                        System.out.println("关闭这个不活跃通道：" + entry.getKey());
                        removeKey = entry.getKey();
                    }else{
                        Message systemMessage = new Message();
                        systemMessage.setTalkFrom("SYSTEM");
                        systemMessage.setType(3);
                        systemMessage.setTalkTo(entry.getKey());
                        systemMessage.setMessage(JSON.toJSONString(Global.channelContextMap.keySet()));
                        String msgJSON = JSON.toJSONString(systemMessage);
                        existChannel.writeAndFlush(new TextWebSocketFrame(msgJSON));
                    }
                }
                if(removeKey != null) {
                    Global.channelContextMap.remove(removeKey);
                }
            }
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }
}

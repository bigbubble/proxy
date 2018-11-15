package me.pengbo.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import me.pengbo.Global;
import me.pengbo.model.Message;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

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
        String frameText = o.text();
        Message message = JSON.parseObject(frameText, Message.class);
        message.setCreateTime(new Date());
        String newTalkFrom = message.getTalkFrom();
        boolean exist = false;
        while(Global.channelContextMap.keySet().contains(newTalkFrom)) {
            newTalkFrom = message.getTalkFrom() + UUID.randomUUID();
            exist = true;
        }

        if(exist == true) {
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(2);
            systemMessage.setMessage("用户名已经有人在使用，系统自动分配用户名：" + newTalkFrom);
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame());

            message.setTalkFrom(newTalkFrom);
        }

        Global.channelContextMap.put(message.getTalkFrom(), channelHandlerContext.channel());

        if(1 == message.getType() && "_ping".equals(message.getMessage())){ // _ping
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(1);
            systemMessage.setMessage("_pong");
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(systemMessage)));
        } else if(2 == message.getType()){
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(2);
            systemMessage.setMessage(JSON.toJSONString(Global.channelContextMap.keySet()));
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame());
        }else{
            String talkTo = message.getTalkTo();
            Channel receiveChannel = Global.channelContextMap.get(talkTo);
            // 对方在线
            if(receiveChannel != null ) {
                Message sendMsg = new Message();
                sendMsg.setMessage(message.getMessage());
                sendMsg.setTalkFrom(talkTo);
                String textMsg = JSON.toJSONString(sendMsg);
                receiveChannel.writeAndFlush(new TextWebSocketFrame(textMsg));
            } else {
                Message systemMessage = new Message();
                systemMessage.setType(2);
                systemMessage.setTalkFrom("SYSTEM");
                systemMessage.setMessage("对方不在线");
                channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame());
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        for(Map.Entry<String, Channel> entry : Global.channelContextMap.entrySet()) {
            String key = entry.getKey();
            Channel context = entry.getValue();
            if(context.equals(ctx)){
                System.out.println("移除通道：" + key);
                Global.channelContextMap.remove(key);
                break;
            }
        }
    }
}

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
        while(Global.channelContextMap.keySet().contains(newTalkFrom) && !Global.channelContextMap.values().contains(channelHandlerContext.channel())) {
            newTalkFrom = message.getTalkFrom() + UUID.randomUUID();
            exist = true;
        }

        if(exist == true) {
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(2);
            systemMessage.setMessage("用户名已经有人在使用，系统自动分配用户名：" + newTalkFrom);
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(systemMessage)));

            message.setTalkFrom(newTalkFrom);
        }
        if(!Global.channelContextMap.keySet().contains(message.getTalkFrom())){
            Global.channelContextMap.put(message.getTalkFrom(), channelHandlerContext.channel());
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(3);
            systemMessage.setTalkTo(message.getTalkFrom());
            systemMessage.setMessage(JSON.toJSONString(Global.channelContextMap.keySet()));
            String msgJSON = JSON.toJSONString(systemMessage);
            for(Channel c : Global.channelContextMap.values()){
                c.writeAndFlush(new TextWebSocketFrame(msgJSON));
            }
        }

        if(1 == message.getType() && "_ping".equals(message.getMessage())){ // _ping
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(1);
            systemMessage.setTalkTo(message.getTalkFrom());
            systemMessage.setMessage("_pong");
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(systemMessage)));
        } else if(2 == message.getType()){
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(2);
            systemMessage.setCreateTime(new Date());
            systemMessage.setTalkTo(message.getTalkTo());
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(systemMessage)));
        } else if(3 == message.getType()) {
            Message sendMsg = new Message();
            sendMsg.setMessage(JSON.toJSONString(Global.channelContextMap.keySet()));
            sendMsg.setTalkFrom("SYSTEM");
            sendMsg.setType(3);
            String textMsg = JSON.toJSONString(sendMsg);
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(textMsg));
        } else {
            String talkTo = message.getTalkTo();
            Channel receiveChannel = Global.channelContextMap.get(talkTo);
            // 对方在线
            if(receiveChannel != null ) {
                Message sendMsg = new Message();
                sendMsg.setMessage(message.getMessage());
                sendMsg.setTalkFrom(message.getTalkFrom());
                sendMsg.setCreateTime(new Date());
                String textMsg = JSON.toJSONString(sendMsg);
                receiveChannel.writeAndFlush(new TextWebSocketFrame(textMsg));
            } else {
                Message systemMessage = new Message();
                systemMessage.setType(2);
                systemMessage.setCreateTime(new Date());
                systemMessage.setTalkFrom("SYSTEM");
                systemMessage.setMessage("对方不在线");
                channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame());
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        String removeKey = null;
        for(Map.Entry<String, Channel> entry : Global.channelContextMap.entrySet()) {
            Channel existChannel = entry.getValue();
            if(existChannel.equals(ctx.channel())){
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
}

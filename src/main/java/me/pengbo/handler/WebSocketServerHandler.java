package me.pengbo.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import me.pengbo.Global;
import me.pengbo.model.Message;

import java.util.Date;
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
        //处理重复用户名
        String newTalkFrom = message.getTalkFrom();
        boolean exist = false;
        while(Global.channelContextMap.keySet().contains(newTalkFrom) && !Global.channelContextMap.values().contains(channelHandlerContext.channel())) {
            newTalkFrom = message.getTalkFrom() + UUID.randomUUID();
            exist = true;
        }
        // 通知用户名字已经在使用，重新分配新名字
        if(exist == true) {
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(2);
            systemMessage.setMessage("用户名已经有人在使用，系统自动分配用户名：" + newTalkFrom);
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(systemMessage.toJSONString()));

            message.setTalkFrom(newTalkFrom);
        }
        //新用户加入，通知在线用户
        if(!Global.channelContextMap.keySet().contains(message.getTalkFrom())){
            //检查是否超过系统最擦载荷
            if(Global.channelContextMap.size() > 10000) {
                Message systemMessage = new Message();
                systemMessage.setTalkFrom("SYSTEM");
                systemMessage.setType(2);
                systemMessage.setTalkTo(message.getTalkFrom());
                systemMessage.setMessage("已超过系统最大载荷，请稍后重试");
                TextWebSocketFrame frame = new TextWebSocketFrame(systemMessage.toJSONString());
                channelHandlerContext.channel().writeAndFlush(frame);
                channelHandlerContext.channel().close();
                return;
            }
            channelHandlerContext.channel().attr(Global.CHANNEL_USER_KEY).set(message.getTalkFrom());
            Global.channelContextMap.put(message.getTalkFrom(), channelHandlerContext.channel());
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(3);
            systemMessage.setTalkTo(message.getTalkFrom());
            systemMessage.setMessage(JSON.toJSONString(Global.channelContextMap.keySet()));
            TextWebSocketFrame frame = new TextWebSocketFrame(systemMessage.toJSONString());
            for(Channel c : Global.channelContextMap.values()){
                c.writeAndFlush(frame);
            }
        }
        //心跳包
        if(1 == message.getType() && "_ping".equals(message.getMessage())){ // _ping
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(1);
            systemMessage.setTalkTo(message.getTalkFrom());
            systemMessage.setMessage("_pong");
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(systemMessage.toJSONString()));
        } else if(2 == message.getType()){
            //系统消息
            Message systemMessage = new Message();
            systemMessage.setTalkFrom("SYSTEM");
            systemMessage.setType(2);
            systemMessage.setCreateTime(new Date());
            systemMessage.setTalkTo(message.getTalkTo());
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(message.toJSONString()));
        } else if(3 == message.getType()) {
            //系统交互，获取在线用户列表
            Message sendMsg = new Message();
            sendMsg.setMessage(JSON.toJSONString(Global.channelContextMap.keySet()));
            sendMsg.setTalkFrom("SYSTEM");
            sendMsg.setType(3);
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(sendMsg.toJSONString()));
        } else {
            //和其他用户聊天
            String talkTo = message.getTalkTo();
            Channel receiveChannel = Global.channelContextMap.get(talkTo);
            // 对方在线
            if(receiveChannel != null ) {
                Message sendMsg = new Message();
                sendMsg.setMessage(message.getMessage());
                sendMsg.setTalkFrom(message.getTalkFrom());
                sendMsg.setCreateTime(new Date());
                receiveChannel.writeAndFlush(new TextWebSocketFrame(sendMsg.toJSONString()));
            } else {
                //对方不在线
                Message systemMessage = new Message();
                systemMessage.setType(2);
                systemMessage.setCreateTime(new Date());
                systemMessage.setTalkFrom("SYSTEM");
                systemMessage.setMessage("对方不在线,重新选择对话");
                channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(systemMessage.toJSONString()));
            }
        }
    }

    /**
     * 用户下线，从map中移除
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        System.out.println("用户下线");
        Global.remove(ctx.channel());
    }
}

package me.pengbo;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import me.pengbo.model.Message;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * </p >
 *
 * @author pengb
 * @since 2018-11-15 10:25
 */
public class Global{

    public final static Map<String, Channel> channelContextMap = new ConcurrentHashMap<String, Channel>();

    public static void remove(Channel channel) {
        Iterator<Map.Entry<String, Channel>> it = channelContextMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Channel> entry = it.next();
            Channel existChannel = entry.getValue();
            if(existChannel.equals(channel)){
                Global.channelContextMap.remove(entry.getKey());
                break;
            }
        }
        //所有在线
        String JSONText = JSON.toJSONString(channelContextMap.keySet());
        Message message = new Message();
        message.setType(3);
        message.setCreateTime(new Date());
        message.setTalkFrom("SYSTEM");
        message.setMessage(JSONText);
        TextWebSocketFrame frame = new TextWebSocketFrame(JSON.toJSONString(message));
        for(Map.Entry<String, Channel> entry : channelContextMap.entrySet()) {
            entry.getValue().writeAndFlush(frame);
        }
    }
}

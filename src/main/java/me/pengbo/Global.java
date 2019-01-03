package me.pengbo;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import me.pengbo.model.Message;

import java.util.Date;
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

    public final static AttributeKey<String> CHANNEL_USER_KEY = AttributeKey.newInstance("USER_IDENTIFY");

    public final static Map<String, Channel> channelContextMap = new ConcurrentHashMap<String, Channel>();

    /**
     * 从在线用户map中删除删除
     * @param channel
     */
    public static void remove(Channel channel) {
        String key = channel.attr(CHANNEL_USER_KEY).get();
        channelContextMap.remove(key);
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

package me.pengbo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

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
}

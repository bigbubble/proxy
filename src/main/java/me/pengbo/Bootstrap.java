package me.pengbo;

import io.netty.channel.ChannelHandlerContext;
import me.pengbo.server.WebSocketServer;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p >
 *
 * @author pengb
 * @since 2018-09-29 13:42
 */
public class Bootstrap {
    public static void main(String[] args) {
        try {
            new WebSocketServer().run(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

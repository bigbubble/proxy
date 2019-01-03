package me.pengbo;

import me.pengbo.server.WebSocketServer;

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

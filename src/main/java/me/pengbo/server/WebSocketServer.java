package me.pengbo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import me.pengbo.handler.HeartBeatServerHandler;
import me.pengbo.handler.WebSocketServerHandler;

/**
 * <p>
 *
 * </p >
 *
 * @author pengb
 * @since 2018-09-29 10:59
 */
public class WebSocketServer {
    public void run(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap b  = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("http-codec",
                                    new HttpServerCodec());
                            pipeline.addLast("aggregator",
                                    new HttpObjectAggregator(65536));
                            pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                            //超时关闭连接
                            pipeline.addLast(new IdleStateHandler(300, 0, 0));
                            pipeline.addLast(new HeartBeatServerHandler());

                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65535 ));
                            pipeline.addLast(new WebSocketServerHandler());
                        }
                    });
            Channel ch = b.bind(port).sync().channel();
            ch.closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

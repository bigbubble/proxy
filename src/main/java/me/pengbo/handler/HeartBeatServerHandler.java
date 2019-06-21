package me.pengbo.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import me.pengbo.Global;
import me.pengbo.model.Message;

import java.util.Date;

/**
 * <p>
 *
 * </p >
 *
 * @author pengb
 * @since 2018-09-29 14:00
 */
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 移除不常用用户
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                System.out.println("关闭不活跃用户");
                Global.remove(ctx.channel());
                Message msg = new Message();
                msg.setMessage("您已下线");
                msg.setTalkFrom("SYSTEM");
                msg.setType(2);
                msg.setCreateTime(new Date());
                channel.writeAndFlush(new TextWebSocketFrame(msg.toJSONString()));
                ctx.close();
                channel.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}

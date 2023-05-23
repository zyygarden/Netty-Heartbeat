package cn.yuyang.handler;

import cn.yuyang.message.PingMessage;
import cn.yuyang.message.PongMessage;
import cn.yuyang.session.SessionFactory;
import cn.yuyang.util.MapUnRecPingTimes;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component(value = "pingRequestMessageHandler")
public class PingRequestMessageHandler extends SimpleChannelInboundHandler<PingMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PingMessage msg) throws Exception {
        System.out.println("服务器收到来自客户端"+ SessionFactory.getSession().getUsername(ctx.channel())+"发来的ping消息, 同时发送了pong消息");
        ctx.writeAndFlush(new PongMessage("pong"));
        MapUnRecPingTimes.map.put(ctx.channel(), 0);
    }
}
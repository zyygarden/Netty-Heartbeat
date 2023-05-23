package cn.yuyang.handler;

import cn.yuyang.message.ChatRequestMessage;
import cn.yuyang.message.ChatResponseMessage;
import cn.yuyang.session.SessionFactory;
import cn.yuyang.util.MapUnRecPingTimes;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component(value = "chatRequestMessageHandler")
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        String to = msg.getTo();
        Channel channel = SessionFactory.getSession().getChannel(to);
        MapUnRecPingTimes.map.put(ctx.channel(), 0);
        if (channel != null) {//在线
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), msg.getContent()));
        } else {//不在线
            ctx.writeAndFlush(new ChatResponseMessage(false,"对方用户不存在或者不在线"));
        }
    }
}

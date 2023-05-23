package cn.yuyang.handler;

import cn.yuyang.message.GroupChatRequestMessage;
import cn.yuyang.message.GroupChatResponseMessage;
import cn.yuyang.session.GroupSessionFactory;
import cn.yuyang.util.MapUnRecPingTimes;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;
import java.util.List;

@ChannelHandler.Sharable
@Component(value = "groupChatRequestMessageHandler")
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {
        MapUnRecPingTimes.map.put(ctx.channel(), 0);
        List<Channel> channels = GroupSessionFactory.getGroupSession().getMembersChannel(msg.getGroupName());
        for (Channel channel : channels) {
            channel.writeAndFlush(new GroupChatResponseMessage(msg.getFrom(), msg.getContent()));
        }
    }
}

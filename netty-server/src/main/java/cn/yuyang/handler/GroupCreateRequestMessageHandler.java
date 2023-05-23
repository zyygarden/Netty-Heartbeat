package cn.yuyang.handler;

import cn.yuyang.message.GroupCreateRequestMessage;
import cn.yuyang.message.GroupCreateResponseMessage;
import cn.yuyang.session.Group;
import cn.yuyang.session.GroupSession;
import cn.yuyang.session.GroupSessionFactory;
import cn.yuyang.util.MapUnRecPingTimes;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;

@Component(value = "groupCreateRequestMessageHandler")
@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        MapUnRecPingTimes.map.put(ctx.channel(), 0);
        String groupName = msg.getGroupName();
        Set<String> members = msg.getMembers();
        //群管理器
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.createGroup(groupName, members);
        if (group == null) {
            //发送成功消息
            ctx.writeAndFlush(new GroupCreateResponseMessage(true, groupName + "创建成功"));
            //发送拉群消息
            List<Channel> channels = groupSession.getMembersChannel(groupName);
            for (Channel channel : channels) {
                channel.writeAndFlush(new GroupCreateResponseMessage(true, "您已被拉入" + groupName));
            }
        } else {
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, groupName + "已经存在"));
        }
    }
}

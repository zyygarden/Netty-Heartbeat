package cn.yuyang.handler;

import cn.yuyang.util.MapUnRecPingTimes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ConnectHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        MapUnRecPingTimes.map.put(ctx.channel(),0);
    }
}

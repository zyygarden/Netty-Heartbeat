package cn.yuyang.handler;

import cn.yuyang.message.PingMessage;
import cn.yuyang.session.ClientSessionFactory;
import cn.yuyang.util.MapCurrentTimeGap;
import cn.yuyang.util.MapUnRecPongTimes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartHandler extends ChannelInboundHandlerAdapter {

    //总共发送的PING次数
    private int heartbeatCount = 0;

    // 定义客户端允许没有收到服务端的pong消息的最大次数
    private static final int MAX_UN_REC_PONG_TIMES = 3;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        String clientName = ClientSessionFactory.getSession().getUsername(ctx.channel());
        if (evt instanceof IdleStateEvent) {
            if (MapUnRecPongTimes.map.get(ctx.channel()) < MAX_UN_REC_PONG_TIMES) {
                MapUnRecPongTimes.map.put(ctx.channel(), MapUnRecPongTimes.map.get(ctx.channel()) + 1);
                System.out.println("客户端" + clientName + "第" + MapUnRecPongTimes.map.get(ctx.channel()) + "次向服务端发送了ping消息");
                heartbeatCount++;
                System.out.println("客户端" + clientName + "总计第" + heartbeatCount + "次向服务端发送了ping消息");
                //设置客户端发送ping消息前时间
                MapCurrentTimeGap.map.put(ctx.channel(), System.currentTimeMillis());
                //发送ping消息
                ctx.writeAndFlush(new PingMessage(clientName + "发送了Ping数据包"));
            } else {
                System.out.println("服务端超过2次未响应客户端的ping消息...");
                ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
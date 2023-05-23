package cn.yuyang.handler;

import cn.yuyang.message.TimeoutResponseMessage;
import cn.yuyang.session.SessionFactory;
import cn.yuyang.util.MapUnRecPingTimes;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.stereotype.Component;

/**
 * 实现心跳的hander  支持超时断开客户端避免浪费资源
 */
@Component(value = "serverHeartHandler")
@ChannelHandler.Sharable
public class ServerHeartHandler extends ChannelInboundHandlerAdapter {

    //定义客户端没有收到服务端的pong消息的最大次数
    private static final int MAX_UN_REC_PING_TIMES = 3;

    //等待触发(专门用来接收客户端数据,若10s内没接收到则触发该事件)
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            String type = "";
            if (event.state() == IdleState.READER_IDLE) {
                type = "read idle";
            } else if (event.state() == IdleState.WRITER_IDLE) {
                type = "write idle";
            } else if (event.state() == IdleState.ALL_IDLE) {
                type = "all idle";
            }
            if (MapUnRecPingTimes.map.get(ctx.channel()) >= MAX_UN_REC_PING_TIMES) {
                System.out.println("已关闭和客户端" + SessionFactory.getSession().getUsername(ctx.channel()) + "的连接");
                // 服务端发送失败消息
                ctx.writeAndFlush(new TimeoutResponseMessage("连接超时"));
                // 睡眠1s
                Thread.sleep(1000);
                // 连续超过N次未收到client的ping消息，那么关闭该通道，等待client重连
                ctx.channel().close();
            } else {
                // 失败计数器加1
                MapUnRecPingTimes.map.put(ctx.channel(), MapUnRecPingTimes.map.get(ctx.channel()) + 1);
                System.out.println("未读到客户端" + SessionFactory.getSession().getUsername(ctx.channel()) + "的请求, " + "触发" + type + "事件第" + MapUnRecPingTimes.map.get(ctx.channel()) + "次");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
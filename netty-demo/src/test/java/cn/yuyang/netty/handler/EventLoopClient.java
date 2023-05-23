package cn.yuyang.netty.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import java.net.InetSocketAddress;
import java.util.Scanner;

@Slf4j
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));//比如1s后
        Channel channel = channelFuture.sync().channel();
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String line = sc.nextLine();
                if ("q".equals(line)) {
                    channel.close();//在nio线程处理
                    //log.debug("处理关闭之后的操作");//在input线程处理,这种会有问题
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();

        //获取CloseFuture,(1)同步处理关闭;(2)异步处理关闭
        ChannelFuture closeFuture = channel.closeFuture();

        //方法1【在该线程里执行】
        /*System.out.println("wait close...");
        closeFuture.sync();
        log.debug("处理关闭之后的操作");*/

        //方法2【Nio线程关闭】
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("处理关闭之后的操作");
                group.shutdownGracefully();
            }
        });
    }
}
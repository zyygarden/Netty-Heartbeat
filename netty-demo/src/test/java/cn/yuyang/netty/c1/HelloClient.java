package cn.yuyang.netty.c1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import java.net.InetSocketAddress;

public class HelloClient {
    public static void main(String[] args) throws InterruptedException {
        //1.启动类
        new Bootstrap()
                //2.添加EventLoop
                .group(new NioEventLoopGroup())
                //3.选择客户端channel实现
                .channel(NioSocketChannel.class)
                //4.添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override//连接建立后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());//添加编码器,将发送的数据转化成字节发送

                    }
                })
                //5.连接到服务器
                .connect(new InetSocketAddress("localhost", 18080))
                .sync()//阻塞方法,直到连接建立
                .channel()
                //向服务器发送数据
                .writeAndFlush("hello, world");
    }
}
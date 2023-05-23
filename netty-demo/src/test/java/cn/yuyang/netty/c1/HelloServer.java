package cn.yuyang.netty.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class HelloServer {
    public static void main(String[] args) {
        //1.服务器端的启动器,负责组装netty组件
        new ServerBootstrap()
                //2.boss和worker(selector,thread)
                .group(new NioEventLoopGroup())
                //3.选择服务器的ServerSocketChannel实现
                .channel(NioServerSocketChannel.class)
                //4.boss负责处理连接,work(child)负责处理读写,决定了worker(child)能执行哪些操作(handler)
                .childHandler(
                        //5.channel代表和客户端进行数据读写的通道,负责添加别的handler
                        new ChannelInitializer<NioSocketChannel>() {
                    @Override//连接建立后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringDecoder());//将ByteBuffer转化成字符串
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {//自定义handker
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(msg);
                            }
                        });
                    }
                })
                .bind(18080);
    }
}
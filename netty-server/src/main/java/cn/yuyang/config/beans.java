package cn.yuyang.config;

import cn.yuyang.channelInitializer.ServerChannelInitializer;
import cn.yuyang.util.SpringBeanFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import java.net.InetSocketAddress;

/**
 * @package com.netty.server.config
 * @Date Created in 2019/2/23 13:01
 * @Author myzf
 */

@Configuration
public class beans {

    /*根据名称装配，防止和客户端的ChannelInitializer冲突报错*/
    @Autowired
    @Qualifier("serverChannelInitializer")
    private ServerChannelInitializer serverChannelInitializer;


    @Value("${tcp.host}")
    private String host;

    @Value("${tcp.port}")
    private int tcpPort;

    @Value("${boss.thread.count}")
    private int bossCount;

    @Value("${worker.thread.count}")
    private int workerCount;

    /**
     * netty服务器启动帮助类
    */
    @Bean(name = "serverBootstrap")
    public ServerBootstrap bootstrap() {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup(), workerGroup())
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(serverChannelInitializer);
        return b;
    }

    /*用来监控tcp链接 指定线程数 默认是1 用默认即可*/
    @Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossCount);
    }

    /*处理io事件 一定要多线程效率高 源码中默认是cpu核数*2 */
    @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerCount);
    }

    /*定义使用tcp连接方式*/
    @Bean(name = "tcpSocketAddress")
    public InetSocketAddress tcpPort() {
        return new InetSocketAddress(host,tcpPort);
    }

    @Bean
    @Lazy(value = false)/*关闭懒加载，防止启动时候不实例化*/
    public SpringBeanFactory springBeanFactory(){
        return new SpringBeanFactory();
    }

}
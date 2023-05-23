package cn.yuyang;

import cn.yuyang.handler.HeartHandler;
import cn.yuyang.message.*;
import cn.yuyang.pojo.MonitorVo;
import cn.yuyang.protocol.MessageCodecSharable;
import cn.yuyang.protocol.ProcotolFrameDecoder;
import cn.yuyang.redis.JedisPoolUtil;
import cn.yuyang.rpc.Rpc;
import cn.yuyang.service.RpcService;
import cn.yuyang.session.ClientSessionFactory;
import cn.yuyang.util.MapCurrentTimeGap;
import cn.yuyang.util.MapUnRecPongTimes;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        CountDownLatch WAT_FOR_LOGIN = new CountDownLatch(1);//登录
        AtomicBoolean LOGIN = new AtomicBoolean(false);//登录成功或失败标志
        CountDownLatch TIMEOUT = new CountDownLatch(1);//超时
        AtomicBoolean TIMEOUT_RESULT = new AtomicBoolean(true);//超时标志
        //日期格式
        DateFormat sf = new SimpleDateFormat("HH:mm:ss");
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new MessageCodecSharable());
                    //10s内没有向服务器写数据,会触发一个IdleState.WRITER_IDLE
                    ch.pipeline().addLast(new IdleStateHandler(0, 10, 0));
                    ch.pipeline().addLast(new HeartHandler());
                    //监听服务器断开连接消息|登录消息
                    ch.pipeline().addLast("client handler", new ChannelInboundHandlerAdapter() {
                        //接收响应消息
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg: {}", msg);
                            if (msg instanceof LoginResponseMessage) {
                                LoginResponseMessage response = (LoginResponseMessage) msg;
                                if (response.isSuccess()) {
                                    //如果登录成功
                                    LOGIN.set(true);
                                }
                                //唤醒system in线程
                                WAT_FOR_LOGIN.countDown();
                            } else if (msg instanceof TimeoutResponseMessage) {
                                TIMEOUT_RESULT.set(false);
                                TIMEOUT.countDown();
                            } else if (msg instanceof PongMessage) {//接收服务端的pong消息
                                //注入jedis
                                Jedis jedis = JedisPoolUtil.getJedis();
                                String clientName = ClientSessionFactory.getSession().getUsername(ctx.channel());
                                System.out.println(clientName);
                                //接收到server发送的pong指令
                                MapUnRecPongTimes.map.put(ctx.channel(), 0);//重置
                                //计算发送一次ping-pong时延
                                long time = System.currentTimeMillis() - MapCurrentTimeGap.map.get(ctx.channel());
                                log.info("客户端和服务器的ping-pong通信的时延是" + time + "ms");
                                MonitorVo monitorVo = new MonitorVo();
                                monitorVo.setClientName(clientName);
                                monitorVo.setDateList(sf.format(new Date()));
                                monitorVo.setValueList(time);
                                jedis.lpush("heart:monitor:" + clientName, JSON.toJSONString(monitorVo));
                            }
                        }

                        //连接建立后触发active事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            //负责接收用户在控制台的输入,负责向服务器发送各种消息
                            new Thread(() -> {
                                //连接成功后
                                MapUnRecPongTimes.map.put(ctx.channel(), 0);//设置写操作初始Map
                                Scanner scanner = new Scanner(System.in);
                                log.info("请输入用户名:");
                                String username = scanner.nextLine();
                                log.info("请输入密码:");
                                String password = scanner.nextLine();
                                //构造消息对象
                                LoginRequestMessage message = new LoginRequestMessage(username, password);
                                //发送消息
                                ctx.writeAndFlush(message);
                                try {
                                    WAT_FOR_LOGIN.await();//同步等待
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                //如果登录失败
                                if (!LOGIN.get()) {
                                    ctx.channel().close();
                                    return;
                                }
                                ClientSessionFactory.getSession().bind(ctx.channel(), username);
                                while (true) {
                                    System.out.println("==================================");
                                    System.out.println("rpc");
                                    System.out.println("send [username] [content]");
                                    System.out.println("gsend [group name] [content]");
                                    System.out.println("gcreate [group name] [m1,m2,m3...]");
                                    System.out.println("gquit [group name]");
                                    System.out.println("quit");
                                    System.out.println("==================================");
                                    if (!TIMEOUT_RESULT.get()) {
                                        ctx.channel().close();
                                        return;
                                    }
                                    String command = scanner.nextLine();
                                    String[] s = command.split(" ");
                                    switch (s[0]) {
                                        case "rpc":
                                            Rpc rpc = new Rpc();
                                            RpcService service = rpc.RpcService(RpcService.class, ctx.channel());
                                            String say = service.say(username);
                                            System.out.println(say);
                                            break;
                                        case "send":
                                            ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                                            break;
                                        case "gsend":
                                            ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                                            break;
                                        case "gcreate":
                                            Set<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                                            set.add(username);//加入自己
                                            ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                                            break;
                                        case "gquit":
                                            ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                                            break;
                                        case "quit":
                                            ctx.channel().close();
                                            return;
                                    }
                                }
                            }, "system in").start();
                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 19999).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
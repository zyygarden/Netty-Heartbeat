package cn.yuyang.channelInitializer;

import cn.yuyang.handler.*;
import cn.yuyang.protocol.MessageCodecSharable;
import cn.yuyang.protocol.ProcotolFrameDecoder;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component(value = "serverChannelInitializer")
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Value("${server.READER_IDLE_TIME_SECONDS}")
    private int READER_IDLE_TIME_SECONDS;

    @Value("${server.WRITER_IDLE_TIME_SECONDS}")
    private int WRITER_IDLE_TIME_SECONDS;

    @Value("${server.ALL_IDLE_TIME_SECONDS}")
    private int ALL_IDLE_TIME_SECONDS;

    @Autowired
    @Qualifier("chatRequestMessageHandler")
    private ChatRequestMessageHandler chatRequestMessageHandler;

    @Autowired
    @Qualifier("groupChatRequestMessageHandler")
    private GroupChatRequestMessageHandler groupChatRequestMessageHandler;

    @Autowired
    @Qualifier("groupCreateRequestMessageHandler")
    private GroupCreateRequestMessageHandler groupCreateRequestMessageHandler;

    @Autowired
    @Qualifier("loginRequestMessageHandler")
    private LoginRequestMessageHandler loginRequestMessageHandler;

    @Autowired
    @Qualifier("pingRequestMessageHandler")
    private PingRequestMessageHandler pingRequestMessageHandler;

    @Autowired
    @Qualifier("quitHandler")
    private QuitHandler quitHandler;

    @Autowired
    @Qualifier("rpcRequestMessageHandler")
    private RpcRequestMessageHandler rpcRequestMessageHandler;

    @Autowired
    @Qualifier("serverHeartHandler")
    private ServerHeartHandler serverHeartHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //LTC长度字段解码器
        ch.pipeline().addLast(new ProcotolFrameDecoder());
        //日志handler
        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
        //自定义协议消息编解码协议
        ch.pipeline().addLast(new MessageCodecSharable());
        //心跳机制
        ch.pipeline().addLast(new IdleStateHandler(READER_IDLE_TIME_SECONDS, WRITER_IDLE_TIME_SECONDS, ALL_IDLE_TIME_SECONDS));
        ch.pipeline().addLast(serverHeartHandler);
        //连接建立后
        ch.pipeline().addLast(new ConnectHandler());
        //自定义处理器
        ch.pipeline().addLast(rpcRequestMessageHandler);//客户端远程调用
        ch.pipeline().addLast(pingRequestMessageHandler);//接收PING消息
        ch.pipeline().addLast(loginRequestMessageHandler);//接受登录请求
        ch.pipeline().addLast(chatRequestMessageHandler);
        ch.pipeline().addLast(groupCreateRequestMessageHandler);
        ch.pipeline().addLast(groupChatRequestMessageHandler);
        ch.pipeline().addLast(quitHandler);
    }
}
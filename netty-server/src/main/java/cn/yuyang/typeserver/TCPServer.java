package cn.yuyang.typeserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.net.InetSocketAddress;

@Component
public class TCPServer {

	@Autowired
	@Qualifier("serverBootstrap")
	private ServerBootstrap serverBootstrap;

	@Autowired
	@Qualifier("tcpSocketAddress")
	private InetSocketAddress tcpIpAndPort;

	private Channel serverChannel;

	//@PostConstruct（会导致其他东西不运行）
	public void start() throws Exception {
		serverChannel =  serverBootstrap.bind(tcpIpAndPort).sync().channel().closeFuture().sync().channel();
	}

}

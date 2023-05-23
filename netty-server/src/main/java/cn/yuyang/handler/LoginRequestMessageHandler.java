package cn.yuyang.handler;

import cn.yuyang.message.LoginRequestMessage;
import cn.yuyang.message.LoginResponseMessage;
import cn.yuyang.pojo.UserMapper;
import cn.yuyang.session.SessionFactory;
import cn.yuyang.util.MapUnRecPingTimes;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@ChannelHandler.Sharable
@Component(value = "loginRequestMessageHandler")
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {

    @Resource
    private UserMapper userMapper;

    private static LoginRequestMessageHandler loginRequestMessageHandler;

    @PostConstruct
    public void init() {
        loginRequestMessageHandler = this;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        System.out.println("来自" + ctx.channel().remoteAddress() + " 的客户端请求登录");
        String username = msg.getUsername();
        String password = msg.getPassword();
        Integer login = loginRequestMessageHandler.userMapper.login(username, password);
        LoginResponseMessage message;
        if (login > 0) {
            SessionFactory.getSession().bind(ctx.channel(), username);
            message = new LoginResponseMessage(true, "登陆成功");
            MapUnRecPingTimes.map.put(ctx.channel(), 0);
        } else {
            message = new LoginResponseMessage(false, "用户名或密码不正确");
        }
        ctx.writeAndFlush(message);
    }
}
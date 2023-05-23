package cn.yuyang.handler;

import cn.yuyang.message.RpcRequestMessage;
import cn.yuyang.message.RpcResponseMessage;
import cn.yuyang.service.RpcService;
import cn.yuyang.service.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
@Component(value = "rpcRequestMessageHandler")
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) throws Exception {
        System.out.println("要寄了");
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(message.getSequenceId());
        try {
            //使用ServicesFactory工厂类获取到远程服务的实例
            //通过接口名称参数，创建并返回该接口对应的实现类的实例
            RpcService service = (RpcService) ServicesFactory.getService(Class.forName(message.getInterfaceName()));
            //通过反射,从Service对象中获取需要调用的方法,通过传入方法名称和参数类型
            Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
            //调用该方法
            Object invoke = method.invoke(service, message.getParameterValue());
            response.setReturnValue(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getCause().getMessage();
            response.setExceptionValue(new Exception("远程调用出错:" + msg));
        }
        ctx.writeAndFlush(response);
    }

    public static void main(String[] args) throws Exception {
        RpcRequestMessage message = new RpcRequestMessage(
                1,
                "cn.yuyang.service.RpcService",
                "say",
                String.class,
                new Class[]{String.class},
                new Object[]{"zhang"}
        );
        RpcService service = (RpcService)
                ServicesFactory.getService(Class.forName(message.getInterfaceName()));
        Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
        Object invoke = method.invoke(service, message.getParameterValue());
        System.out.println(invoke);
    }
}

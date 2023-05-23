package cn.yuyang.rpc;

import cn.yuyang.message.RpcRequestMessage;
import cn.yuyang.protocol.SequenceIdGenerator;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;
import java.lang.reflect.Proxy;

public class Rpc {

    public <T> T RpcService(Class<T> serviceClass, Channel channel) {
        return getProxyService(serviceClass, channel);
    }

    // 创建代理类
    public <T> T getProxyService(Class<T> serviceClass, Channel channel) {
        ClassLoader loader = serviceClass.getClassLoader();//指定生成的代理类的类加载器
        Class<?>[] interfaces = new Class[]{serviceClass};//指定生成的代理类要实现的接口

        Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            // 1.将方法调用转换为消息对象
            int sequenceId = SequenceIdGenerator.nextId();
            RpcRequestMessage msg = new RpcRequestMessage(
                    sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
            );
            // 2.将消息发送出去
            channel.writeAndFlush(msg);
            return "1";
        });
        return (T) o;
    }
}

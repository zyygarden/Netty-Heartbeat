package cn.yuyang.protocol;

import cn.yuyang.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Slf4j
@ChannelHandler.Sharable
public class MessageCodec extends ByteToMessageCodec<Message> {

    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        //4个字节的魔数[判断数据包是否有效]
        out.writeBytes(new byte[]{1, 2, 3, 4});
        //1个字节的版本[支持协议的升级]
        out.writeByte(1);
        //1个字节的(反)序列化方式[jdk 0,json 1]
        out.writeByte(0);
        //1个字节的指令类型[登录、通信...]
        out.writeByte(msg.getMessageType());
        //4个字节的请求序号[异步发送接收(不是按顺序),为了找到响应是对应哪次请求]
        out.writeInt(msg.getSequenceId());
        //1个字节[无意义，对齐填充]//保证固定的字节数是二的整数倍
        out.writeByte(0xff);
        //获取内容的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();
        //4个字节的消息长度
        out.writeInt(bytes.length);
        //消息正文
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Message message = (Message) ois.readObject();
        log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);
        log.debug("{}", message);
        out.add(message);
    }
}
package cn.yuyang.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TestLengthFiledDecoder {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(1024,0,4,1,5),
                new LoggingHandler(LogLevel.DEBUG)
        );
        //4个字节长度,实际内容
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        send(buffer,"Hello, world");
        send(buffer,"Hi!");
        channel.writeInbound(buffer);
    }

    private static void send(ByteBuf buffer, String s) {
        byte[] bytes = s.getBytes();//实际内容
        int length = bytes.length;//实际内容长度
        buffer.writeInt(length);
        buffer.writeByte(1);//版本号,1个字节
        buffer.writeBytes(bytes);
    }

}

package cn.yuyang.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import static cn.yuyang.netty.bytebuf.TestByteBuf.log;


public class TestSlice {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'});
        log(buf);

        //在切片得过程中没有发生数据复制
        ByteBuf buf1 = buf.slice(0, 5);
        ByteBuf buf2 = buf.slice(5, 5);
        log(buf1);
        log(buf2);

        System.out.println("===============================");
        buf1.setByte(0,'b');
        log(buf1);
        log(buf);
    }


}

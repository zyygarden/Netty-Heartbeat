package cn.yuyang.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import static cn.yuyang.netty.bytebuf.TestByteBuf.log;

public class TestCompositeByteBuf {
    public static void main(String[] args) {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{1, 2, 3, 4, 5});
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{6, 7, 8, 9, 10});

//        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
//        buffer.writeBytes(buf1).writeBytes(buf2);
//        log(buffer);

        CompositeByteBuf buf = ByteBufAllocator.DEFAULT.compositeBuffer();
        buf.addComponents(true, buf1, buf2);
        log(buf);
    }
}
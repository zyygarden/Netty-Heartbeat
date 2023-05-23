package cn.yuyang.nio.c1;

import java.nio.ByteBuffer;

public class TestByteBufferRead {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a', 'b', 'c', 'd'});
        buffer.flip();//切换为读模式

        //从头开始读
//        buffer.get(new byte[4]);
//        debugAll(buffer);
//        buffer.rewind();//position置为0
//        System.out.println((char) buffer.get());

        //mark & reset
        //mark是做一个标记,记录position的位置;而reset是将position重置到mark的位置
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        buffer.mark();//索引为2
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        buffer.reset();//将position重置到2
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
    }
}
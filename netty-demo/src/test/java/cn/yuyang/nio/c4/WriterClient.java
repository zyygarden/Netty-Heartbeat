package cn.yuyang.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriterClient {
    public static void main(String[] args) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("localhost", 8080));
        //接收数据
        int count = 0;
        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            count += channel.read(buffer);//从channel里读数据写入到buffer
            System.out.println(count);
        }
    }
}

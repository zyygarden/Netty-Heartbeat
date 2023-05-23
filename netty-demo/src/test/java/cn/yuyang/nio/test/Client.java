package cn.yuyang.nio.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("localhost", 8080));
        channel.write(Charset.defaultCharset().encode("Hello"));
        System.in.read();
    }
}

package cn.yuyang.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

public class WriterServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        ssc.bind(new InetSocketAddress(8080));

        while (true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel channel = ssc.accept();
                    channel.configureBlocking(false);
                    SelectionKey sckey = channel.register(selector, SelectionKey.OP_READ, null);
                    //1.向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 5000000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());

                    //返回值代表实际写入的字节数
                    int write = channel.write(buffer);//从buffer里读数据写入channel
                    System.out.println(write);

                    //判断buffer里是否有剩余内容
                    if (buffer.hasRemaining()) {
                        //关注可写事件
                        sckey.interestOps(sckey.interestOps() + SelectionKey.OP_WRITE);
                        //把未写完的数据挂到sckey上
                        sckey.attach(buffer);
                    }
                } else if (key.isWritable()) {
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel channel = (SocketChannel) key.channel();
                    int write = channel.write(buffer);
                    System.out.println(write);
                    //6.清理操作
                    if (!buffer.hasRemaining()) {
                        key.attach(null);//清除buffer
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);//不需要关注可写事件
                    }
                }
            }

        }
    }
}

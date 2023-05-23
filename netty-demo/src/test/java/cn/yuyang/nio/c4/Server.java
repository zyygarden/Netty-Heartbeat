package cn.yuyang.nio.c4;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import static cn.yuyang.nio.c1.ByteBufferUtil.debugAll;

@Slf4j
public class Server {

    private static void split(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
        source.compact();//压缩向前冲(并切换为写模式)
    }

    public static void main(String[] args) throws IOException {
        //创建Selector,管理多个channel
        Selector selector = Selector.open();

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); //非阻塞模式,同时监听多个端口
        //建立Selector和channel之间的联系,SelectionKey就是将来事件发生后,通过它可以知道时间和哪个channel的事件
        SelectionKey sscKey = ssc.register(selector, 0, null);
        //只关注accept哪个事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register key:{}", sscKey);

        ssc.bind(new InetSocketAddress(8080));

        while (true) {
            //3.select方法
            //Select在事件未处理时,它不会阻塞,事件发生后要么处理,要么取消,不能置之不理
            selector.select();//没有事件发生,有事件就恢复运行

            //4.处理事件,selectedKeys内部包含了所有发生的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();//拿到所有事件集合[相当于每次重新遍历,只会加入不会删除]
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                //处理key时,要从selectedKeys集合中删除,否则下次处理就会有问题
                iterator.remove();
                log.debug("key:{}", key);
                //5.区分事件类型
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    //将bytebuffer作为附件关联到selectionKey上
                    ByteBuffer buffer = ByteBuffer.allocate(16); //attachment
                    SelectionKey scKey = sc.register(selector, 0, buffer);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", sc);
                } else if (key.isReadable()) {
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();//拿到触发事件的channel
                        //获取
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = channel.read(buffer);//通过读channel写入到buffer,如果是正常断开,read方法的返回值是-1
                        if (read == -1) {
                            key.cancel();
                        } else {
                            split(buffer);
                            if (buffer.position() == buffer.limit()) {
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                buffer.flip();//切换为读模式
                                newBuffer.put(buffer);
                                key.attach(newBuffer);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel(); //因为客户端断开了,因此需要将key取消【从selector的keys集合中真正删除】
                    }
                }

            }

        }

    }
}

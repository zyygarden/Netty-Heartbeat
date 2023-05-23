package cn.yuyang.nio.test;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import static cn.yuyang.nio.c1.ByteBufferUtil.debugAll;

@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT, null);//监听注册进的监听通道
        ssc.bind(new InetSocketAddress(8080));
        AtomicInteger index = new AtomicInteger();
        Worker[] workers = new Worker[2];
        for (int i = 0; i < 2; i++) {
            workers[i] = new Worker("thread-" + (i + 1));
        }
        while (true) {
            selector.select();//selector开始监听事件(EventLoop)
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();//遍历监听的每一个事件
                iter.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    workers[index.getAndIncrement() % workers.length].register(sc);//启动线程
                }
            }
        }
    }

    //必须注册进再开始监听,不然一直会阻塞

    static class Worker implements Runnable {

        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean start = false;//控制线程是否初始化
        private ConcurrentLinkedDeque<Runnable> queue = new ConcurrentLinkedDeque<>();

        public Worker(String name) {
            this.name = name;
        }

        //初始化线程和任务
        public void register(SocketChannel sc) throws IOException {
            if (!start) {
                selector = Selector.open();
                thread = new Thread(this, name);
                thread.start();
                start = true;
            }
            queue.add(() -> {
                try {
                    sc.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            selector.wakeup();//唤醒正在阻塞的监听,然后注册下次循环再监听
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select();
                    Runnable poll = queue.poll();
                    if (poll != null) {
                        poll.run();
                    }
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            log.debug("read...{}");
                            channel.read(buffer);
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
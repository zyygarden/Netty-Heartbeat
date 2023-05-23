package cn.yuyang.nio.multithreading;

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
public class MultiThreadServer_Workers {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("Boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey bossKey = ssc.register(boss, SelectionKey.OP_ACCEPT, null);
        ssc.bind(new InetSocketAddress(8080));
        //1.创建多个固定数量的worker
        Worker[] workers = new Worker[2];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }
        AtomicInteger index = new AtomicInteger();
        while (true) {
            boss.select();
            Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected...{}", sc.getRemoteAddress());
                    //2.关联selector
                    log.debug("before register...{}", sc.getRemoteAddress());
                    //round robin(轮询算法)
                    workers[index.getAndIncrement() % workers.length].register(sc);
                    log.debug("after register...{}", sc.getRemoteAddress());
                }
            }
        }
    }

    static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean start = false;//还未初始化
        private ConcurrentLinkedDeque<Runnable> queue = new ConcurrentLinkedDeque<>();//两个线程之间传递数据用队列

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
            //向队列添加了任务,但这个任务并没有立刻执行
            queue.add(() -> {
                try {
                    sc.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            selector.wakeup();//boss线程调用唤醒select方法
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select();//worker-0开始阻塞
                    Runnable task = queue.poll();
                    if (task != null) {
                        task.run();//在work0中注册
                    }
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();//从channel读数据写入buffer
                            log.debug("read...{}");
                            channel.read(buffer);
                            buffer.flip();//切换为读模式
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
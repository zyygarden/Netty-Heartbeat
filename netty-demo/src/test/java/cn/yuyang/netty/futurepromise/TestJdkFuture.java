package cn.yuyang.netty.futurepromise;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;

@Slf4j
public class TestJdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.线程池
        ExecutorService service = Executors.newFixedThreadPool(2);
        //2.提交任务
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 50;
            }
        });
        //3.主线程通过future获取结果
        log.debug("等待结果");
        log.debug("结果是{}",future.get());//阻塞等待,等待线程池让返回结果
    }
}
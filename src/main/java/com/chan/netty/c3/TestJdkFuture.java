package com.chan.netty.c3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 测试 JDK 中的 Future
 */
@Slf4j
public class TestJdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // JDK 中的 Future 一般是关联线程池一起使用的
        // 1. 线程池
        ExecutorService service = Executors.newFixedThreadPool(2);
        // 2. 提交任务   两种：一种是 callable 有返回结果， 一种是 runnable 没有返回结果
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 50;// 自动填入到 Future，我们不用手动去填充，直接 future 对象 get 就行
            }
        });

        // 主线程中怎么和线程池中的线程通信，获得它的结果呢？
        // 3. main 线程通过 Future 来获得结果
        log.debug("等待结果");
        Integer result = future.get();// 会同步等待线程池中的线程的任务运行结束得出结果，get 方法结束等待拿到结果
        log.debug("结果是 {}", result);
    }
}

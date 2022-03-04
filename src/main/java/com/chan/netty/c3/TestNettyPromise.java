package com.chan.netty.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * 前面的两个例子中，无论是jdk中的future还是netty的future，都是需要等任务提交之后返回future
 * future的创建权和future中值的设置权都不属于我们
 * 但是promise可以我们自己设置
 */
@Slf4j
public class TestNettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 准备 EventLoop 对象
        EventLoop eventLoop = new NioEventLoopGroup().next();

        // 2. 可以主动创建promise, 相当于一个结果容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(new Runnable() {
            // 3. 任何一个线程执行计算，计算完毕之后向 promise 填充结果
            @Override
            public void run() {
                // System.out.println("开始计算");
                log.debug("开始计算");
                try {
                    int i = 1 / 0;
                    Thread.sleep(1000);
                    promise.setSuccess(80);// 计算成功，设置结果
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    promise.setFailure(e);// 结果有问题
                }
            }
        }).start();

        // 4. 接收结果的线程
        log.debug("等待结果");
        log.debug("结果是 {}", promise.get());
    }
}

package com.chan.netty.c3;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j(topic = "TestEventLoop")
public class TestEventLoop {
    public static void main(String[] args) {
        //1、创建事件循环组
        EventLoopGroup nGroup = new NioEventLoopGroup(2);//io事件，普通任务，定时任务
        // EventLoopGroup dGroup = new DefaultEventLoopGroup();//普通任务，定时任务

        //2、获取下一个事件循环对象
        // 第一个事件循环对象负责1，3两个channel；第二个事件循环对象负责2，4两个channel
        System.out.println(nGroup.next());// io.netty.channel.nio.NioEventLoop@3aa9e816
        System.out.println(nGroup.next());// io.netty.channel.nio.NioEventLoop@17d99928
        System.out.println(nGroup.next());// io.netty.channel.nio.NioEventLoop@3aa9e816
        System.out.println(nGroup.next());// io.netty.channel.nio.NioEventLoop@17d99928

        //3、执行普通任务
        /*nGroup.next().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("ok");
            }
        });*/

        //4、执行定时任务
        nGroup.next().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.debug("ok");
            }
        }, 0, 1, TimeUnit.SECONDS);
        log.debug("main");
    }
}

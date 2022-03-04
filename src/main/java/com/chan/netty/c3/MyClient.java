package com.chan.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * 需求：在客户端控制台输入字符，发送到服务器，并且客户端输入 “q” 的时候退出客户端。
 */
@Slf4j
public class MyClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override// 在连接建立后被调用
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // netty 提供的 handler，日志相关的，相关的日志显示操作不用自定义 handler ，用它就可以。
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        nioSocketChannel.pipeline().addLast(new StringEncoder());// netty 提供的字符串转字节的 handler
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));
        Channel channel = channelFuture.sync().channel();
        log.debug("{}", channel);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.println("请输入：");
                    String line = scanner.nextLine();
                    if ("q".equals(line)){
                        channel.close();// close 和 connect 都是异步操作 可能 1s 之后才关闭  // nio 线程将 channel 关闭
                        break;
                    } else {
                        channel.writeAndFlush(line);// 发送给服务器
                    }
                }
            }
        }, "input").start();

        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            // ChannelFutureListener 不是 main 线程调用的，是个异步的操作，将来谁去关闭 channel 谁来调用 operationComplete 回调方法，
            // 也就是 nio 线程，关闭了 channel 之后找到回调对象从而执行 operationComplete 中的逻辑
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("处理关闭之后的操作");
                // channel 关闭后，客户端还没有停止，因为 nioEventLoopGroup 中还有部分线程，因此将它们关闭，
                // shutdownGracefully()：优雅的停下来，拒绝接受新的任务，等段时间把现有的任务完成，再停止线程而非立刻停止。
                group.shutdownGracefully();
            }
        });
    }
}

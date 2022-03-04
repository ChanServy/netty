package com.chan.netty.c6;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * 协议设计与解析-redis
 *
 * 以redis为例，体验什么是协议
 * redis 命令：
 * set key value，set name chanservy，看成一个数组，数组中有三个元素
 * 解析方式如下：
 * *3
 * $3
 * set
 * $4
 * name
 * $9
 * chanservy
 */
@Slf4j
public class TestRedis {
    public static void main(String[] args) {
        final byte[] LINE = {13, 10};// 换行
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast(new LoggingHandler());
                    nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        @Override
                        // 连接建立就发送命令到redis服务器
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buffer = ctx.alloc().buffer();
                            buffer.writeBytes("*3".getBytes());
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("$3".getBytes());
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("set".getBytes());
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("$4".getBytes());
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("name".getBytes());
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("$9".getBytes());
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("chanservy".getBytes());
                            buffer.writeBytes(LINE);
                            ctx.writeAndFlush(buffer);
                        }
                        // 接收写入redis之后，redis返回的信息
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ByteBuf buf = (ByteBuf) msg;
                            String result = buf.toString(Charset.defaultCharset());
                            System.out.println(result);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 6379).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }
}

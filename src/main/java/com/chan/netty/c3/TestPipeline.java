package com.chan.netty.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

@Slf4j
public class TestPipeline {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // nioSocketChannel.pipeline().addLast()
                        // 1.通过channel拿到pipeline
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        // 2.添加处理器 head -> h1 -> h2 -> h3 -> h4 -> h5 -> h6 -> tail
                        // （head和tail是netty自动加的） 这里所谓的addLast，是加在tail前的 底层是个双向链表
                        // 1,2,3 为入站处理器，读客户端发来的数据
                        pipeline.addLast("h1", new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {// 这里的msg实际上是一个ByteBuf。客户端发来的
                                log.debug("1");
                                ByteBuf buf = (ByteBuf) msg;
                                String name = buf.toString(Charset.defaultCharset());
                                super.channelRead(ctx, name);//ctx.fireChannelRead(msg); 要将执行权交给下一个入站handler h2，并且会将 h1 自己的处理结果传递下去(给h2)
                            }
                        });
                        pipeline.addLast("h2", new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {// 这里的msg实际上是一个String。也就是h1传递来的name
                                log.debug("2");
                                Stu stu = new Stu(msg.toString());
                                super.channelRead(ctx, stu);//ctx.fireChannelRead(msg); 要将执行权交给下一个入站handler h3，并且会将 h2 自己的处理结果传递下去(给h3)
                            }
                        });
                        pipeline.addLast("h3", new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {// 这里的msg实际上是一个Stu。也就是h2传递来的stu
                                log.debug("3, 结果: {}, class: {}", msg, msg.getClass());
                                // super.channelRead(ctx, msg); 不再需要唤醒下一个入站处理器了，因为下面没有入站处理器了，是出站处理器，下面就要开始写出了
                                // 分配一个ByteBuf对象 ctx.alloc().buffer()，向其中写入一些字节 writeBytes("server...".getBytes()
                                // 注意：nioSocketChannel是NioSocketChannel类型，ctx是ChannelHandlerContext类型，都能向channel中写入数据,唤醒出站处理器，区别如下：
                                // 当前程序中的顺序为head -> h1 -> h2 -> h3 -> h4 -> h5 -> h6 -> tail，
                                // 通过nioSocketChannel向channel中写入数据是从tail开始向前依次唤醒出站处理器，∴日志输出为123654
                                // 通过ctx向channel中写入数据是从当前handler开始向前依次唤醒出站处理器，∴日志输出为123，∵3前面没有出站处理器，∴一个出站处理器也没唤醒
                                nioSocketChannel.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes()));
                                // ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes()));
                            }
                        });
                        // 4,5,6 为出站处理器 只有你向channel中写入数据时才会触发出站处理器，没写数据就形同虚设
                        pipeline.addLast("h4", new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("4");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h5", new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("5");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h6", new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("6");
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                })
                .bind(new InetSocketAddress(8080));
    }
    @Data
    @AllArgsConstructor
    static class Stu{
        private String name;
    }
}

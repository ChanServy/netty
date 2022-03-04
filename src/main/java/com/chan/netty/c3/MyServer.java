package com.chan.netty.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

@Slf4j
public class MyServer {
    public static void main(String[] args) {
        DefaultEventLoopGroup dGroup = new DefaultEventLoopGroup();
        new ServerBootstrap()
                .group(new NioEventLoopGroup()/* ACCEPT */, new NioEventLoopGroup(2)/* READ/WRITE */)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast("handler1", new ChannelInboundHandlerAdapter(){
                            @Override
                            // 因为没有使用 netty 提供的字符串转换 handler，因此此时的 msg 的实际类型是 ByteBuf，客户端发来的数据
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                String s = buf.toString(Charset.defaultCharset());
                                log.debug("{}", s);
                                // 本案例是读取客户端发来的 msg 的值，其实不耗时，可以直接在这个 nio 的线程中完成
                                // 把这个 msg 的值传递到额外的 group(非io线程的)，只是为了演示耗时操作的处理方案
                                // 因为把耗时操作放在 NioEventLoop 这种 nio 线程处理的话会降低 io 的处理效率！
                                ctx.fireChannelRead(msg);// 让消息传递给下一个 handler
                            }
                        }).addLast(dGroup, "handler2", new ChannelInboundHandlerAdapter(){// 把一个handler的执行权交给额外的group
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                // buf转字符串，使用带字符集参数的toString，指定字符集
                                log.debug(buf.toString(Charset.defaultCharset()));
                                System.out.println(buf.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                })
                .bind(new InetSocketAddress(8080));
    }
}

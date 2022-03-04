package com.chan.netty.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        // 进一步的分工，除了分出boss和worker分别处理不同的事件类型之外，
        // 如果某个handler内部的代码执行的时间较长，可以再独立出来一个group，
        // 让这个group中的线程来单独处理这个handler中的代码，这样就不会影响io线程。
        // 细分2：创建一个独立的EventLoopGroup
        EventLoopGroup group = new DefaultEventLoopGroup();//只能处理普通任务和定时任务
        new ServerBootstrap()
                // boss 和 worker
                // 细分1：
                //     boss 只负责 ServerSocketChannel 上的 accept 事件
                //                              worker 只负责 SocketChannel 上的读写事件
                .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast("handler1", new ChannelInboundHandlerAdapter(){
                            @Override
                            // 因为没有使用 netty 提供的字符串转换 handler，因此此时的 msg 的实际类型是 ByteBuf
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                //buf转字符串，使用带字符集参数的toString，指定字符集
                                log.debug(buf.toString(Charset.defaultCharset()));
                                ctx.fireChannelRead(msg);// 让消息传递给下一个 handler
                            }
                        }).addLast(group, "handler2", new ChannelInboundHandlerAdapter(){//把一个handler的执行权交给额外的group
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                //buf转字符串，使用带字符集参数的toString，指定字符集
                                log.debug(buf.toString(Charset.defaultCharset()));
                                System.out.println(buf.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                })
                .bind(8080);
    }
}

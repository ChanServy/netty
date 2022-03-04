package com.chan.netty.c5;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class Client {
    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        // 会在连接 channel 建立成功后，也就是连上服务器就会触发 active 事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buffer = ctx.alloc().buffer();
                            preSend(buffer, "Hello, world");
                            preSend(buffer, "Hi!");
                            ctx.writeAndFlush(buffer);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress("localhost", 8080)).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }

    /**
     * 发送之前，准备数据和数据长度
     * @param buffer ByteBuf
     * @param content String
     */
    private static void preSend(ByteBuf buffer, String content) {
        byte[] bytes = content.getBytes();// 实际内容
        int length = bytes.length;// 实际内容长度
        // writeInt，int 长度为4 所以为 0x00000000。把实际长度存入后：0x0000000C 和 0x00000003
        // （本案例中：Hello, world 长度为12，也就是C；Hi! 长度为3，就是3）
        buffer.writeInt(length);
        buffer.writeByte(1);// 比如是版本号
        buffer.writeBytes(bytes);
    }
}

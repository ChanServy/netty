package com.chan.netty.c6;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * netty进阶-协议设计与解析-http
 *
 * 以HTTP为例，体验什么是协议
 * Http的协议比较繁琐，所以使用Netty提供好的处理器
 */
@Slf4j
public class TestHttp {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    // netty提供的http请求解码器HttpServerCodec既是入站处理器（解码）也是出站处理器（编码成ByteBuf）
                    socketChannel.pipeline().addLast(new HttpServerCodec());// 编解码结合 从命名角度codec既包括解码也包括编码
                    // SimpleChannelInboundHandler 可以根据消息的类型加以区分，来进行选择处理
                    socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                        @Override
                        // 经过入站处理器HttpServerCodec解码之后传到msg
                        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                            // 1、获取请求
                            log.debug("请求行：{}", msg.uri());// 获取请求行
                            // log.debug(String.valueOf(msg.headers()));// 获取请求头 不关注
                            // 2、返回响应
                            // netty提供的响应对象DefaultFullHttpResponse，写入channel之后，会经过上一个出站处理器HttpServerCodec 编码成ByteBuf 返回给浏览器
                            DefaultFullHttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
                            // 浏览器一直转圈，是因为我们没有告诉浏览器我们返回响应的长度是多少，所以在响应头里面加一个ContentLength响应体长度
                            byte[] bytes = "<h1>Hello, world!</h1>".getBytes();
                            response.headers().setInt(CONTENT_LENGTH, bytes.length);
                            ByteBuf content = response.content();
                            content.writeBytes(bytes);
                            ctx.writeAndFlush(response);
                        }
                    });
                    /*
                    socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        @Override
                        // 因为是浏览器发给我的信息，所以要channelRead读。channelRead方法的msg参数会传过来上一步解码后的结果
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            // 由此可见netty提供的http请求解码器将我们的请求解码成了两个部分
                            // io.netty.handler.codec.http.DefaultHttpRequest 第一部分HttpRequest包含请求行和请求头
                            // io.netty.handler.codec.http.LastHttpContent 第二部分HttpContent包含请求体
                            log.debug("{}", msg.getClass());// 因为不知道解码之后是什么类型，这里测试一下
                            // if逻辑判断太麻烦？使用上面的SimpleChannelInboundHandler
                            if (msg instanceof HttpRequest){// 请求行和请求头

                            } else if (msg instanceof HttpContent) {// 请求体

                            }
                        }
                    });
                    */
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(8080)).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

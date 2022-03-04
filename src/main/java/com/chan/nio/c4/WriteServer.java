package com.chan.nio.c4;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

@Slf4j(topic = "WriteServer")
public class WriteServer {
    @SneakyThrows
    public static void main(String[] args) {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT, null);
        // sscKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    /*
                     和上面的ssc其实是一个，因为对于服务器而言，ServerSocketChannel只有一个，所以这个位置也可以ssc.accept()。
                     但是SocketChannel不行，因为有多个，需要根据触发事件的那个key去找到对应的SocketChannel。
                     ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                     SocketChannel socketChannel = serverSocketChannel.accept();
                    */
                    SocketChannel socketChannel = ssc.accept();
                    socketChannel.configureBlocking(false);
                    SelectionKey scKey = socketChannel.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    //向客户端发送大量数据
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < 5000000; i++) {
                        stringBuilder.append("a");
                    }
                    //将数据放入buffer并且自动打开buffer的读模式
                    ByteBuffer buffer = Charset.defaultCharset().encode(stringBuilder.toString());
                    //返回值代表实际写入的字节数，不一定能一次性写完
                    int write = socketChannel.write(buffer);
                    System.out.println(write);
                    //判断是否有剩余内容
                    if (buffer.hasRemaining()) {
                        //关注可写事件        1                       4
                        scKey.interestOps(scKey.interestOps() + SelectionKey.OP_WRITE);//5 表示关注读事件和写事件或下面写法
                        // scKey.interestOps(scKey.interestOps() | SelectionKey.OP_WRITE);
                        //把未写完的数据挂到scKey上
                        scKey.attach(buffer);
                    }
                } else if (key.isWritable()) {
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel channel = (SocketChannel) key.channel();
                    int write = channel.write(buffer);
                    System.out.println(write);
                    //清理操作
                    if (!buffer.hasRemaining()) {//如果内容都写完了的话
                        key.attach(null);//清除buffer
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);//不再关注可写事件
                    }
                }
            }
        }
    }
}

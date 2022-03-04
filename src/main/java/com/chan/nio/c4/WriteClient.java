package com.chan.nio.c4;

import lombok.SneakyThrows;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteClient {
    @SneakyThrows
    public static void main(String[] args) {
        SocketChannel socketChannel = SocketChannel.open();
        //客户端这边connect一连上，服务端那边selector.select(),就会往下进行，就会发现一个事件类型为ACCEPT的key
        socketChannel.connect(new InetSocketAddress("localhost", 8080));
        //接收数据
        int count = 0;
        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            count = count + socketChannel.read(buffer);
            System.out.println(count);
            buffer.clear();
        }
    }
}

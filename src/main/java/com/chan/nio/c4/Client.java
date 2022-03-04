package com.chan.nio.c4;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
@Slf4j(topic = "Client")
public class Client {
    @SneakyThrows
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("localhost", 8080));
        log.debug("waiting...");
        buffer.put("hello...".getBytes());//写入
        buffer.flip();
        channel.write(buffer);//通过channel发送
        buffer.clear();
    }
}

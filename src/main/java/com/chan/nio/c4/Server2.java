package com.chan.nio.c4;

import com.chan.nio.c2.ByteBufferUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * 单线程配合选择器完成对多个socketChannel事件的处理
 */
@Slf4j(topic = "Server2")
public class Server2 {
    @SneakyThrows
    public static void main(String[] args) {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //更改为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        Selector selector = Selector.open();
        //将channel注册到selector，并指定事件类型为ACCEPT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);
        while (true) {
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            if (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题
                iterator.remove();
                //事件类型为ACCEPT的key
                if (key.isAcceptable()) {
                    //因为Selector中事件类型为Accept的channel只有一个，因此这个ssc和上面的serverSocketChannel是同一个
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = ssc.accept();
                    socketChannel.configureBlocking(false);
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    //将一个bytebuffer作为附件关联到selectionKey上
                    SelectionKey scKey = socketChannel.register(selector, SelectionKey.OP_READ, buffer);
                    log.debug("scKey: {}", scKey);
                } else if (key.isReadable()) {
                    try {
                        //因为Selector中事件类型为Read的channel可能有多个，因此这个sc和上面的socketChannel不一定是同一个
                        SocketChannel sc = (SocketChannel) key.channel();//拿到触发事件的channel
                        // ByteBuffer buffer = ByteBuffer.allocate(16);
                        //获取selectionKey上面关联的附件
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = sc.read(buffer);// 如果客户端是正常断开，读不到数据了，那么 read 的方法的返回值是 -1
                        if (read == -1) {
                            key.cancel();
                        } else {
                            buffer.flip();//将buffer切换为读模式（默认为写）
                            //处理消息的边界问题，比如(黏包，半包问题)
                            for (int i = 0; i < buffer.limit(); i++) {
                                //找到一条完整数据 buffer.get(i)通过索引方式从buffer读数据，position指针不会向后移动
                                if (buffer.get(i) == '\n'){
                                    int length = i + 1 - buffer.position();
                                    //将这条完整的数据存入新的buffer
                                    ByteBuffer target = ByteBuffer.allocate(length);
                                    //从buffer读向target写
                                    for (int j = 0; j < length; j++) {
                                        //buffer.get()获取buffer中position位置的下一个，position指针向后移动
                                        target.put(buffer.get());
                                    }
                                    ByteBufferUtil.debugAll(target);
                                }
                            }
                            buffer.compact();//将buffer切换为写模式
                            //需要扩容的话
                            if (buffer.position() == buffer.limit()) {
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                buffer.flip();
                                newBuffer.put(buffer);
                                key.attach(newBuffer);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();// 因为客户端断开了,因此需要将 key 取消（从 selector 的 keys 集合中真正删除 key）
                    }
                }
            }
        }
    }
}

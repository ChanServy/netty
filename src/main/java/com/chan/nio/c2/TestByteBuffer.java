package com.chan.nio.c2;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j(topic = "TestByteBuffer")
public class TestByteBuffer {
    public static void main(String[] args) {
        m1();
    }
    /* ctrl+alt+M */
    private static void m1() {
        //FileChannel3种获得方式
        //1.输入输出流，2.RandomAccessFile
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            //准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true) {
                //从channel读取数据，就是向buffer写入
                int len = channel.read(buffer);
                log.debug("读取到的字节数{}",len);
                if (len == -1){//没有内容了
                    break;
                }
                //打印buffer的内容
                buffer.flip();//切换至读模式
                while (buffer.hasRemaining()){ //是否还有剩余未读的数据
                    byte b = buffer.get();
                    System.out.print((char) b);
                    log.debug("实际字节{}",(char) b);
                }
                //读完了之后buffer要切换为写模式
                buffer.clear();//切换为写模式
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

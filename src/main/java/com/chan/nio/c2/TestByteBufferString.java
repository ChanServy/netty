package com.chan.nio.c2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.chan.nio.c2.ByteBufferUtil.debugAll;

public class TestByteBufferString {
    public static void main(String[] args) {
        //字符串转为ByteBuffer,三种方式
        //1.将字符串转换成字节放入buffer
        ByteBuffer buffer1 = ByteBuffer.allocate(16);
        buffer1.put("hello".getBytes());//默认是写模式，现在依旧是写模式 有position: [5], limit: [16]可看出
        debugAll(buffer1);
        //输出结果：
        /*
        +--------+-------------------- all ------------------------+----------------+
                position: [5], limit: [16]
        +-------------------------------------------------+
                |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
        +--------+-------------------------------------------------+----------------+
                |00000000| 68 65 6c 6c 6f 00 00 00 00 00 00 00 00 00 00 00 |hello...........|
        +--------+-------------------------------------------------+----------------+
        */

        //2.Charset
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello");//buffer默认是写模式，这种方式会自动切换成读模式   position: [0], limit: [5]
        debugAll(buffer2);
        /*
        +--------+-------------------- all ------------------------+----------------+
                position: [0], limit: [5]
        +-------------------------------------------------+
                |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
        +--------+-------------------------------------------------+----------------+
                |00000000| 68 65 6c 6c 6f                                  |hello           |
        +--------+-------------------------------------------------+----------------+
        */

        //3.wrap
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());//buffer默认是写模式，这种方式会自动切换成读模式   position: [0], limit: [5]
        debugAll(buffer3);
        /*
        +--------+-------------------- all ------------------------+----------------+
                position: [0], limit: [5]
        +-------------------------------------------------+
                |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
        +--------+-------------------------------------------------+----------------+
                |00000000| 68 65 6c 6c 6f                                  |hello           |
        +--------+-------------------------------------------------+----------------+
        */

        //转为字符串
        buffer1.flip();//由于前面方式1将字符串的字节写入buffer后不会自动切换成读模式，因此要手动切换才能转换成字符串
        String str1 = StandardCharsets.UTF_8.decode(buffer1).toString();
        String str2 = StandardCharsets.UTF_8.decode(buffer2).toString();
        String str3 = StandardCharsets.UTF_8.decode(buffer3).toString();
        System.out.println("str1:" + str1);
        System.out.println("str2:" + str2);
        System.out.println("str3:" + str3);
    }
}

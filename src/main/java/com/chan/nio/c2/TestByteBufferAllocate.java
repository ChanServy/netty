package com.chan.nio.c2;

import java.nio.ByteBuffer;

public class TestByteBufferAllocate {
    public static void main(String[] args) {
        System.out.println(ByteBuffer.allocate(12).getClass());
        System.out.println(ByteBuffer.allocateDirect(12).getClass());
        /*
            class java.nio.HeapByteBuffer  -java堆内存，读写效率较低，受到GC的影响
            class java.nio.DirectByteBuffer  -直接内存，读写效率高（少一次拷贝），不会受到GC的影响，分配的效率低
            如果使用不当，可能内存泄露
         */
    }
}

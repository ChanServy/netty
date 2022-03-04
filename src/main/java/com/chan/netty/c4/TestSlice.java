package com.chan.netty.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class TestSlice {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);//1 //netty的bytebuf可动态扩容
        buf.writeBytes(new byte[]{'a','b','c','a','b','c','a','b','c','a'});
        TestByteBuf.log(buf);

        // 在切片的过程中，没有发生数据复制
        // 注意：如果释放原有的 ByteBuf 内存，那么切片会受到影响，因为用的都是同一块内存，
        // 所以为了防止这种可能发生的错误，切片得出的ByteBuf要配合一次retain操作，然后切片得到的ByteBuf使用完自己release
        ByteBuf buf1 = buf.slice(0, 5);//1 //切片,切片后netty对切片之后得到的ByteBuf的最大容量做了限制，就是说不可再加了因为会影响下个切片的起始位置
        buf1.retain();//2
        ByteBuf buf2 = buf.slice(5, 5);//1 //切片,切片后netty对切片之后得到的ByteBuf的最大容量做了限制，就是说不可再加了因为会影响下个切片的起始位置
        buf2.retain();//2

        System.out.println("释放原有 ByteBuf 内存");
        buf.release();//此时：buf引用计数为0、buf1引用计数为1、buf2引用计数为1

        TestByteBuf.log(buf1);
        TestByteBuf.log(buf2);
        // System.out.println("================");
        // buf1.setByte(0, 'q');//更改切片的第一个元素为'q',这样原ByteBuf的第一个元素也被改为'q'，因为都使用同一个内存空间
        // TestByteBuf.log(buf1);
        // TestByteBuf.log(buf);
        buf1.release();//0
        buf2.release();//0
    }
}

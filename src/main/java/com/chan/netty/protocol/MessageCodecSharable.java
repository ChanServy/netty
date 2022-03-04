package com.chan.netty.protocol;

import com.chan.netty.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
/**
 * 对 message 包中的 Message 消息父类自定义编解码操作，编解码的协议就是我们自定义的如下那些
 * MessageToMessageCodec<ByteBuf, Message> 效果是 ByteBuf 和 Message 之间进行转换，但是不可共享
 * MessageToMessageCodec 必须和 LengthFieldBasedFrameDecoder 一起使用，确保接到的ByteBuf消息是完整的，不需记录状态，可@Sharable
 * <p>
 * 自定义协议要素:
 * * 魔数，用来在第一时间判定是否是无效数据包
 * * 版本号，可以支持协议的升级
 * * 序列化算法，消息正文到底采用哪种序列化反序列化方式，可以由此扩展，例如：json、protobuf、hessian、jdk
 * * 指令类型，是登录、注册、单聊、群聊... 跟业务相关
 * * 请求序号，为了双工通信，提供异步能力
 * * 正文长度
 * * 消息正文，可能比较复杂，可能会有好多个属性，username、password 等等... JSON、xml、对象流...
 * <p>
 * 入站和出站说的都是ByteBuf，所以ByteBuf入站之后要解码，出站之前要将其它的编码成ByteBuf才能出站
 * 自定义处理器，包含编码（出站）和解码（入站）
 */
@ChannelHandler.Sharable
@Slf4j
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 4字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});

        // 1字节的版本
        out.writeByte(1);

        // 1字节的序列化方式 jdk 0;json 0
        out.writeByte(0);

        // 1字节的指令类型
        out.writeByte(msg.getMessageType());

        // 4字节的请求序号
        out.writeInt(msg.getSequenceId());

        // 无意义，对齐填充 java中如果固定的字节数不是2的整数倍就不专业 其余的加一起是15 这里凑一个字节
        out.writeByte(0xff);

        // 获取内容的字节数组，因为对象不能直接写入ByteBuf，因此就需要转换成字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);//可以吧对象转换成二进制的字节数组
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();//至此message这个java对象就变成了字节数组

        // 4字节的正文长度
        out.writeInt(bytes.length);

        // 写入内容
        out.writeBytes(bytes);

        // 传递给下一个出站处理器
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();//读出4字节的魔数
        byte version = in.readByte();//这里不能使用get。因为ByteBuf的get读取之后指针不会向后走，read才可以
        byte serializerType = in.readByte();//读出1字节的序列化类型
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();//读出4字节的消息长度
        byte[] bytes = new byte[length];//新建个缓冲区来装消息
        in.readBytes(bytes, 0, length);//读出消息装入bytes
        // if (serializerType == 0/*自定 编号0对应jdk的序列化方式*/) {
        // jdk的序列化算法
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Message message = (Message) ois.readObject();
        // }
        log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);
        log.debug("{}", message);
        // netty约定了解码出来的结果要存到一个参数中，不然接下来的handler拿不到
        out.add(message);
    }
}

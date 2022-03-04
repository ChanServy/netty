package com.chan.netty.protocol;

import com.chan.netty.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),/*解决黏包半包*/
                new LoggingHandler(),
                new MessageCodec()
        );
        // 入站和出站的都是ByteBuf，所以ByteBuf入站之后要解码，出站之前要将其它的编码成ByteBuf才能出站
        // encode
        /*
            17:47:42 [DEBUG] [main] i.n.h.l.LoggingHandler - [id: 0xembedded, L:embedded - R:embedded] WRITE: 220B
                     +-------------------------------------------------+
                     |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
            +--------+-------------------------------------------------+----------------+
            |00000000| 01 02 03 04 01 00 00 00 00 00 00 ff 00 00 00 cc |................|
            |00000010| ac ed 00 05 73 72 00 2a 63 6f 6d 2e 63 68 61 6e |....sr.*com.chan|
            |00000020| 2e 6e 65 74 74 79 2e 6d 65 73 73 61 67 65 2e 4c |.netty.message.L|
            |00000030| 6f 67 69 6e 52 65 71 75 65 73 74 4d 65 73 73 61 |oginRequestMessa|
            |00000040| 67 65 3b 18 be bc e6 36 ab da 02 00 02 4c 00 08 |ge;....6.....L..|
            |00000050| 70 61 73 73 77 6f 72 64 74 00 12 4c 6a 61 76 61 |passwordt..Ljava|
            |00000060| 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b 4c 00 08 |/lang/String;L..|
            |00000070| 75 73 65 72 6e 61 6d 65 71 00 7e 00 01 78 72 00 |usernameq.~..xr.|
            |00000080| 1e 63 6f 6d 2e 63 68 61 6e 2e 6e 65 74 74 79 2e |.com.chan.netty.|
            |00000090| 6d 65 73 73 61 67 65 2e 4d 65 73 73 61 67 65 be |message.Message.|
            |000000a0| e9 f5 d5 0c b2 07 2c 02 00 02 49 00 0b 6d 65 73 |......,...I..mes|
            |000000b0| 73 61 67 65 54 79 70 65 49 00 0a 73 65 71 75 65 |sageTypeI..seque|
            |000000c0| 6e 63 65 49 64 78 70 00 00 00 00 00 00 00 00 74 |nceIdxp........t|
            |000000d0| 00 03 31 32 33 74 00 04 63 68 61 6e             |..123t..chan    |
            +--------+-------------------------------------------------+----------------+
            17:47:42 [DEBUG] [main] i.n.h.l.LoggingHandler - [id: 0xembedded, L:embedded - R:embedded] FLUSH
        */
        LoginRequestMessage message = new LoginRequestMessage("chan", "123");
        channel.writeOutbound(message);// 出站，编码

        // decode
        /*
            17:47:42 [DEBUG] [main] i.n.h.l.LoggingHandler - [id: 0xembedded, L:embedded - R:embedded] READ: 220B
                     +-------------------------------------------------+
                     |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
            +--------+-------------------------------------------------+----------------+
            |00000000| 01 02 03 04 01 00 00 00 00 00 00 ff 00 00 00 cc |................|
            |00000010| ac ed 00 05 73 72 00 2a 63 6f 6d 2e 63 68 61 6e |....sr.*com.chan|
            |00000020| 2e 6e 65 74 74 79 2e 6d 65 73 73 61 67 65 2e 4c |.netty.message.L|
            |00000030| 6f 67 69 6e 52 65 71 75 65 73 74 4d 65 73 73 61 |oginRequestMessa|
            |00000040| 67 65 3b 18 be bc e6 36 ab da 02 00 02 4c 00 08 |ge;....6.....L..|
            |00000050| 70 61 73 73 77 6f 72 64 74 00 12 4c 6a 61 76 61 |passwordt..Ljava|
            |00000060| 2f 6c 61 6e 67 2f 53 74 72 69 6e 67 3b 4c 00 08 |/lang/String;L..|
            |00000070| 75 73 65 72 6e 61 6d 65 71 00 7e 00 01 78 72 00 |usernameq.~..xr.|
            |00000080| 1e 63 6f 6d 2e 63 68 61 6e 2e 6e 65 74 74 79 2e |.com.chan.netty.|
            |00000090| 6d 65 73 73 61 67 65 2e 4d 65 73 73 61 67 65 be |message.Message.|
            |000000a0| e9 f5 d5 0c b2 07 2c 02 00 02 49 00 0b 6d 65 73 |......,...I..mes|
            |000000b0| 73 61 67 65 54 79 70 65 49 00 0a 73 65 71 75 65 |sageTypeI..seque|
            |000000c0| 6e 63 65 49 64 78 70 00 00 00 00 00 00 00 00 74 |nceIdxp........t|
            |000000d0| 00 03 31 32 33 74 00 04 63 68 61 6e             |..123t..chan    |
            +--------+-------------------------------------------------+----------------+
            17:47:42 [DEBUG] [main] c.c.n.p.MessageCodec - 16909060, 1, 0, 0, 0, 204
            17:47:42 [DEBUG] [main] c.c.n.p.MessageCodec - LoginRequestMessage(super=Message(sequenceId=0, messageType=0), username=chan, password=123)
            17:47:42 [DEBUG] [main] i.n.h.l.LoggingHandler - [id: 0xembedded, L:embedded - R:embedded] READ COMPLETE
        */
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buffer);
        channel.writeInbound(buffer);// 入站，解码
    }
}

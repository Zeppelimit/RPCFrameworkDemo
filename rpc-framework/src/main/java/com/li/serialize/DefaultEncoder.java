package com.li.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import com.li.transport.DefaultRequest;

@Slf4j
public class DefaultEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        log.info("进行编码");
        byte flag = 0;
        if(o instanceof DefaultRequest){
            flag = 0;
        }else{
            flag = 1;
        }
        byte[] serialize = Serializer.Algorithm.Json.serialize(o);

        byteBuf.writeByte(flag);
        assert serialize != null;
        byteBuf.writeInt(serialize.length);
        byteBuf.writeBytes(serialize);
        log.info("编码完成");
    }
}

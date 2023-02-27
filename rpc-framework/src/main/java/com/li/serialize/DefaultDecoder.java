package com.li.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import com.li.transport.DefaultRequest;
import com.li.transport.DefaultResponse;

import java.util.List;
@Slf4j
public class DefaultDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        log.info("进行解码");
        byte flag = byteBuf.readByte();
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes, 0, length);
        Class<?> clazz = flag == 0? DefaultRequest.class : DefaultResponse.class;
        Object deserialize = Serializer.Algorithm.Json.deserialize(clazz,bytes);
        if(flag == 0){
            DefaultRequest request = (DefaultRequest) deserialize;
            list.add(request);
        }else{
            DefaultResponse response = (DefaultResponse) deserialize;
            list.add(response);
        }
        log.info("解码完成");
    }
}

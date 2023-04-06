package com.li.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import com.li.transport.DefaultRequest;
import com.li.transport.DefaultResponse;

import java.util.List;
@Slf4j
@Deprecated
public class DefaultCodec extends ByteToMessageCodec {

    private final int MAGIC = 0xCAFE; // 魔数

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        byte requestType;
        if(o instanceof DefaultRequest){
            requestType = (byte) MessageType.MESSAGE_REQUEST.getValue();
        }else if(o instanceof DefaultResponse){
            requestType = (byte) MessageType.MESSAGE_RESPONSE.getValue();
        }else{
            throw new UnsupportedOperationException("flag unknown:" + o);
        }

        byte serializeMethod = (byte) SerializerMethod.Json.getValue();

        byte[] serialize = Serializer.Algorithm.Json.serialize(o);

        byteBuf.writeInt(MAGIC);
        byteBuf.writeByte(requestType);
        byteBuf.writeByte(serializeMethod);
        byteBuf.writeInt(serialize.length);
        byteBuf.writeBytes(serialize);

    }

    @Override

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List list) throws Exception {
        int magic = byteBuf.readInt();

        if(magic != MAGIC){
            channelHandlerContext.close();
            throw new UnsupportedOperationException("error Magic:" + magic);
        }

        int requestType = (int) byteBuf.readByte();
        int serializeMethod = (int) byteBuf.readByte();
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes, 0, length);
        Class<?> clazz = requestType == 1? DefaultRequest.class : DefaultResponse.class;

        Object deserialize = null;
        if(serializeMethod == SerializerMethod.Json.getValue()){
            Serializer.Algorithm serializer = Serializer.Algorithm.Json;
            deserialize = serializer.deserialize(clazz,bytes);
        }else if(serializeMethod == SerializerMethod.Binary.getValue()){

        }else{
            throw new UnsupportedOperationException("serializeMethod unknown:" + serializeMethod);
        }

        if(requestType == MessageType.MESSAGE_REQUEST.getValue()){
            DefaultRequest request = (DefaultRequest) deserialize;
            list.add(request);
        }else if(requestType == MessageType.MESSAGE_RESPONSE.getValue()){
            DefaultResponse response = (DefaultResponse) deserialize;
            list.add(response);
        }else{
            throw new UnsupportedOperationException("requestType unknown:" + requestType);
        }
    }

}

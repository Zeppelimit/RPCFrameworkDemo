package com.li.serialize;

import com.li.serialize.struct.RpcHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import com.li.serialize.struct.RpcMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {
        log.info("开始编码");
        if(rpcMessage == null || rpcMessage.getRpcHeader() == null){
            throw new Exception("编码失败,没有数据信息!");
        }

        RpcHeader rpcHeader = rpcMessage.getRpcHeader();

        byteBuf.writeInt(rpcHeader.getMAGIC());
        byteBuf.writeByte(rpcHeader.getMessageType());
        byteBuf.writeByte(rpcHeader.getIsEvent());
        byteBuf.writeByte(rpcHeader.getStatus());
        byteBuf.writeByte(rpcHeader.getSerializeMethod());
        byteBuf.writeLong(rpcHeader.getId());

        int serializeMethod = rpcHeader.getSerializeMethod();

        Serializer serializer = Serializer.Algorithm.Json;
        if(serializeMethod == SerializerMethod.Json.getValue()){
            serializer = Serializer.Algorithm.Json;
        }

        byte[] serialize = serializer.serialize(rpcMessage.getData());

        byteBuf.writeInt(serialize.length);
        byteBuf.writeBytes(serialize);

    }
}

package com.li.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import com.li.transport.client.struct.RpcHeader;
import com.li.transport.client.struct.RpcMessage;
import com.li.transport.DefaultRequest;
import com.li.transport.DefaultResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcDecoder extends LengthFieldBasedFrameDecoder {

    public RpcDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame =(ByteBuf) super.decode(ctx, in);

        if(frame == null){
            return null;
        }

        RpcMessage rpcMessage = new RpcMessage();
        RpcHeader rpcHeader = new RpcHeader();

        int magic = frame.readInt();

        byte messageType = frame.readByte();
        rpcHeader.setMessageType(messageType);
        rpcHeader.setIsEvent(frame.readByte());
        rpcHeader.setStatus(frame.readByte());
        byte serializeMethod = frame.readByte();
        rpcHeader.setSerializeMethod(serializeMethod);
        rpcHeader.setId(frame.readLong());

        rpcMessage.setRpcHeader(rpcHeader);

        int length = frame.readInt();
        byte[] bytes = new byte[length];
        frame.readBytes(bytes, 0, length);

        Class<?> clazz = null;
        if(messageType == MessageType.MESSAGE_REQUEST.getValue()){
            clazz = DefaultRequest.class;
        }else{
            clazz = DefaultResponse.class;
        }
        Object data = clazz.getConstructor().newInstance();
        if((int) serializeMethod == SerializerMethod.Json.getValue()){
            data = Serializer.Algorithm.Json.deserialize(clazz,bytes);
        }else if((int) serializeMethod == SerializerMethod.Protostuff.getValue()){
            data = Serializer.Algorithm.Protostuff.deserialize(clazz,bytes);
        }else{
            try{
                data = new KryoSerializer().deserialize(clazz, bytes);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }

        }

        rpcMessage.setData(data);


        return rpcMessage;
    }
}

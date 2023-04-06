package com.li.registry.protocol;

import com.li.common.ServiceManager;
import com.li.common.TaskThreadPool;
import com.li.rpc.AppRpcResult;
import com.li.rpc.RpcResult;
import com.li.serialize.KryoSerializer;
import com.li.serialize.Serializer;
import com.li.serialize.SerializerMethod;
import com.li.transport.client.struct.RpcHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import com.li.rpc.invoker.RpcInvocation;
import com.li.rpc.invoker.Invoker;
import com.li.serialize.MessageFactory;
import com.li.transport.client.struct.RpcMessage;
import com.li.transport.DefaultRequest;
import com.li.transport.DefaultResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component("RpcProtocol")
@Scope("prototype")
public class RpcProtocol extends AbstractProtocol{

    @Override
    public int getDefaultPort() {
        return 8083;
    }

    @Resource
    TaskThreadPool taskThreadPool;

    public void reply(ChannelHandlerContext ctx, DefaultRequest request) throws Exception {
        taskThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                long id = request.getId();
                RpcInvocation invocation = request.getInvocation();

                String refServiceName = invocation.getRefServiceName();

                Invoker invoker = ServiceManager.getInvoker(refServiceName);

                RpcResult res = invoker.invoke(invocation);

                DefaultResponse response = new DefaultResponse();
                response.setId(id);
                response.setStateCode(0);
                response.setRetData((AppRpcResult) res);

                RpcMessage rpcMessage = MessageFactory.buildRspMessage(response, id);
//                log.info("回复请求：{}", id);


                try {
                    ctx.writeAndFlush(encode(rpcMessage));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }


    @Override
    public void destroy() {

    }

    protected ByteBuf encode(RpcMessage rpcMessage) throws Exception {
        if(rpcMessage == null || rpcMessage.getRpcHeader() == null){
            throw new Exception("编码失败,没有数据信息!");
        }
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(30);
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
        }else if(serializeMethod == SerializerMethod.Protostuff.getValue()){
            serializer = Serializer.Algorithm.Protostuff;
        }else{
            serializer = new KryoSerializer();
        }
        try{
            byte[] serialize = serializer.serialize(rpcMessage.getData());
            byteBuf.writeInt(serialize.length);
            byteBuf.writeBytes(serialize);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return byteBuf;
    }
}

package com.li.serialize;

import com.li.serialize.struct.RpcHeader;
import com.li.serialize.struct.RpcMessage;
import com.li.transport.DefaultRequest;
import com.li.transport.DefaultResponse;

public class MessageFactory {

    public static RpcMessage buildRspMessage(DefaultResponse response, long id){
        RpcMessage rpcMessage = new RpcMessage();
        RpcHeader rpcHeader = new RpcHeader();
        rpcHeader.setMessageType((byte) MessageType.MESSAGE_RESPONSE.getValue());
        rpcHeader.setId(id);
        rpcHeader.setStatus((byte) ResponseStatus.OK.getValue());
        rpcHeader.setIsEvent((byte) 1);
        rpcHeader.setSerializeMethod((byte) SerializerMethod.Json.getValue());
        rpcMessage.setRpcHeader(rpcHeader);
        rpcMessage.setData(response);
        return rpcMessage;
    }

    public static RpcMessage buildReqMessage(DefaultRequest request){
        RpcMessage rpcMessage = new RpcMessage();
        RpcHeader rpcHeader = new RpcHeader();
        rpcHeader.setMessageType((byte) MessageType.MESSAGE_REQUEST.getValue());
        rpcHeader.setId(request.getId());
        rpcHeader.setStatus((byte) 0);
        rpcHeader.setIsEvent((byte) 1);
        rpcHeader.setSerializeMethod((byte)SerializerMethod.Json.getValue());
        rpcMessage.setRpcHeader(rpcHeader);
        rpcMessage.setData(request);
        return rpcMessage;
    }

    public static RpcMessage buildNotFoundMessage(long id){
        RpcMessage rpcMessage = new RpcMessage();
        RpcHeader rpcHeader = new RpcHeader();
        rpcHeader.setId(id);
        rpcHeader.setStatus((byte) ResponseStatus.SERVICE_NOT_FOUND.getValue());
        rpcHeader.setIsEvent((byte) 0);
        rpcMessage.setRpcHeader(rpcHeader);
        return rpcMessage;
    }
}

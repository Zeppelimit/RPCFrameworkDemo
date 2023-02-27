package com.li.transport;

import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import com.li.serialize.struct.RpcMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class ResponseCache {

    public static Map<Long, RpcMessage> messageCache = new ConcurrentHashMap<>(32);

    public static Map<Long, DefaultPromise<RpcMessage>> responsePromiseCache = new ConcurrentHashMap<>(32);

    public static void putMessageCache(RpcMessage rpcMessage){
        messageCache.put(rpcMessage.getRpcHeader().getId(),rpcMessage);
    }

    public static void putResponsePromiseCache(long id, DefaultPromise<RpcMessage> responsePromise){
        responsePromiseCache.put(id,responsePromise);
    }


    public static void setResponsePromise(RpcMessage rpcMessage){
        log.info("设置cache， id ：{}",rpcMessage.getRpcHeader().getId());
        DefaultPromise<RpcMessage> promise = responsePromiseCache.getOrDefault(rpcMessage.getRpcHeader().getId(), null);
        if(promise == null){
            log.error("没找到promise，id:{}",rpcMessage.getRpcHeader().getId());
            return;
        }
        promise.setSuccess(rpcMessage);
    }
}

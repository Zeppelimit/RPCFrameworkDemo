package com.li.rpc.proxy;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import com.li.registry.ZookeeperClient;
import com.li.rpc.invoker.RpcInvocation;
import com.li.serialize.MessageFactory;
import com.li.serialize.struct.RpcMessage;
import com.li.spring.config.RpcProperties;
import com.li.transport.DefaultRequest;
import com.li.transport.DefaultResponse;
import com.li.transport.client.NettyClient;
import com.li.transport.ResponseCache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class ReferenceProxy implements InvocationHandler{

    Class<?> interfaceClass;

    private RpcInvocation invocation;

    private ZookeeperClient zookeeperClient;

    private String registryAddress;

    private NioEventLoopGroup eventExecutors = new NioEventLoopGroup(5);

    private Map<String, Object> proxyCache = new ConcurrentHashMap<>();

    private RpcProperties rpcProperties;

    private final int retryCount;

    public ReferenceProxy(RpcInvocation invocation, RpcProperties rpcProperties) {
        this.invocation = invocation;
        this.rpcProperties = rpcProperties;
        this.registryAddress = rpcProperties.getRegistryAddress();
        retryCount = 3;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        log.info("调用方法{}", method.getName());
        invocation.setArgs(args);
        invocation.setMethodName(method.getName());
        invocation.setParameterType(method.getParameterTypes());

        zookeeperClient = ZookeeperClient.zookeeperConnSet.getOrDefault(registryAddress,null);
        if(zookeeperClient == null){
            zookeeperClient = new ZookeeperClient(registryAddress);
        }


        String path = resolveNodePath(invocation);
        String address = zookeeperClient.getNode(path).get(0);

        DefaultRequest defaultRequest = new DefaultRequest();
        defaultRequest.setInvocation(invocation);
        long id = defaultRequest.getId();

        RpcMessage rpcMessage = MessageFactory.buildReqMessage(defaultRequest);

        NettyClient nettyClient = getNettyClient(address);
        if(!nettyClient.isAlive()){
            log.error("连接{}失败", address);
            return null;
        }
        log.info("发送请求，id : {}", id);
        nettyClient.send(rpcMessage);
        log.info("等待回复");

        DefaultPromise promise = getPromise(id);


        RpcMessage rpcMessageResp =(RpcMessage) promise.get();


        if(rpcMessageResp == null){
            log.error("超时了......");
            return null;
        }

        ResponseCache.putMessageCache(rpcMessageResp);

        DefaultResponse data =(DefaultResponse) rpcMessageResp.getData();

        return data.getRetData();
    }

    public  <T> T getProxy(final Class interfaceClass){
        this.interfaceClass = interfaceClass;
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},this);
    }

    private String resolveNodePath(RpcInvocation rpcInvocation) {
        StringBuilder path = new StringBuilder("/rpcTest");
        path.append("/" + rpcInvocation.getGroup())
                .append("/" + rpcInvocation.getInterfaceName())
                .append("/providers");
        return path.toString();
    }

    public DefaultPromise getPromise(Long id) throws ExecutionException, InterruptedException {
        EventLoop next = eventExecutors.next();

        DefaultPromise<RpcMessage> promise = new DefaultPromise<>(next);
        log.info("设置promiseCache, id : {}",id);
        ResponseCache.putResponsePromiseCache(id, promise);

        log.info("等待回复");

        return promise;
    }

    public DefaultResponse getResponse(DefaultPromise promise) throws ExecutionException, InterruptedException, TimeoutException {
        Object resp = null;
        resp = promise.get(10, TimeUnit.SECONDS);
        return (DefaultResponse) resp;
    }

    private NettyClient getNettyClient(String address) throws InterruptedException {
        NettyClient nettyClient = NettyClient.nettyClientMap.getOrDefault(address, null);
        if(nettyClient == null){
            nettyClient = new NettyClient(address);
            NettyClient.nettyClientMap.put(address,nettyClient);
        }

        if(!nettyClient.isAlive()){
            nettyClient.connect();
        }

        return nettyClient;
    }

}

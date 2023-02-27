package com.li.rpc.invoker;

import com.li.Exception.RpcException;
import com.li.common.URL;
import com.li.rpc.PromiseContext;
import com.li.rpc.RpcResult;
import com.li.serialize.MessageFactory;
import com.li.serialize.struct.RpcMessage;
import com.li.transport.DefaultRequest;
import com.li.transport.DefaultResponse;
import com.li.transport.ResponseCache;
import com.li.transport.client.NettyClient;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RpcRefInvoker<T> implements Invoker<T> {

    private URL url;

    private Class<T> serviceInterfaceClass;

    private String version;

    private String group;

    private NioEventLoopGroup eventExecutors = new NioEventLoopGroup(5);

    public RpcRefInvoker(URL url, Class<T> serviceInterfaceClass) {
        this.url = url;
        this.serviceInterfaceClass = serviceInterfaceClass;
    }

    public RpcRefInvoker(URL url, Class<T> serviceInterfaceClass, String version, String group) {
        this.url = url;
        this.serviceInterfaceClass = serviceInterfaceClass;
        this.version = version;
        this.group = group;
    }

    @Override
    public Class<T> getInterface() {
        return serviceInterfaceClass;
    }

    @Override
    public RpcResult invoke(RpcInvocation invocation) throws RpcException{

        NettyClient client = NettyClient.getClient(url.getHost() + ":" + url.getPort());

        DefaultRequest request = new DefaultRequest();

        invocation.setGroup(url.getGroup());
        invocation.setVersion(url.getVersion());
        invocation.setInterfaceName(serviceInterfaceClass.getName());

        request.setInvocation(invocation);

        RpcMessage rpcMessage = MessageFactory.buildReqMessage(request);


        RpcMessage rpcMessageResp = null;
        try {
            log.info("发送请求，id : {}", request.getId());
            client.send(rpcMessage);

            log.info("等待回复");
            DefaultPromise promise = getPromise(request.getId());

            rpcMessageResp = (RpcMessage) promise.get(10, TimeUnit.SECONDS);

            ResponseCache.putMessageCache(rpcMessageResp);

            DefaultResponse data =(DefaultResponse) rpcMessageResp.getData();

            RpcResult res = data.getRetData();

            return res;
        } catch (TimeoutException e) {
            throw new RpcException(RpcException.TIMEOUT_EXCEPTION, "Invoke remote method timeout. method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        } catch (Exception e){
            throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        }

    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    public DefaultPromise getPromise(Long id){

        EventLoop next = eventExecutors.next();

        DefaultPromise<RpcMessage> promise = new DefaultPromise<>(next);

        log.info("设置promiseCache, id : {}",id);
        ResponseCache.putResponsePromiseCache(id, promise);

        log.info("等待回复");

        return promise;
    }
}

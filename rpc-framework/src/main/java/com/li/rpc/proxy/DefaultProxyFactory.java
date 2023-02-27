package com.li.rpc.proxy;

import com.li.rpc.invoker.Invoker;
import com.li.common.URL;
import com.li.rpc.invoker.RpcProxyInvoker;

import java.lang.reflect.Proxy;

public class DefaultProxyFactory implements RpcProxyFactory{
    @Override
    public <T> T getProxy(Class<?>[] interfaceClass, Invoker<T> invoker) {
        return (T) Proxy.newProxyInstance(invoker.getClass().getClassLoader(), interfaceClass, new InvokerInvocationHandler(invoker));
    }

    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {
        return new RpcProxyInvoker<T>(proxy,type, url);
    }
}


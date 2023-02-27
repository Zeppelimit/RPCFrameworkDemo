package com.li.rpc.proxy;

import com.li.rpc.invoker.Invoker;
import com.li.common.URL;

public interface RpcProxyFactory {

    <T> T getProxy(Class<?>[] interfaceClass, Invoker<T> invoker);

    <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url);

}

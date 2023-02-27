package com.li.rpc.invoker;

import com.li.Hello;
import com.li.HelloTwo;
import com.li.common.URL;
import com.li.rpc.AppRpcResult;
import com.li.rpc.RpcResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@Slf4j
@Data
public class RpcProxyInvoker<T> implements Invoker<T> {

    private final T proxy;

    private final Class<T> type;

    private final URL url;

    private boolean available;
    public RpcProxyInvoker(T proxy, Class<T> type, URL url) {
        this.proxy = proxy;
        this.type = type;
        this.url = url;
    }

    @Override
    public Class<T> getInterface() {
        return type;
    }

    @Override
    public RpcResult invoke(RpcInvocation invocation) {

        String methodName = invocation.getMethodName();

        Object[] args = invocation.getArgs();

        Class<?>[] parameterType = invocation.getParameterType();

        return doInvoke(proxy, methodName, parameterType, args);
    }

    private RpcResult doInvoke(T proxy, String methodName, Class<?>[] paraTypes, Object[] args){
        try {
            Method method = proxy.getClass().getMethod(methodName, paraTypes);
            method.setAccessible(true);
            Object invoke = method.invoke(proxy, args);
            return new AppRpcResult(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            return new AppRpcResult(e);
        }

    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void destroy() {
        setAvailable(false);
    }


}

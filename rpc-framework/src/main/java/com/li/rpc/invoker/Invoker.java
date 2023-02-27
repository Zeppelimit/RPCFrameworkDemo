package com.li.rpc.invoker;

import com.li.Exception.RpcException;
import com.li.common.URL;
import com.li.rpc.RpcResult;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface Invoker<T> {

    Class<T> getInterface();

    RpcResult invoke(RpcInvocation invocation) throws RpcException;

    URL getUrl();

    void destroy();

    boolean isAvailable();

}

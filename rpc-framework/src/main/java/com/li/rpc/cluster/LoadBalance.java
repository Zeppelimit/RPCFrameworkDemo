package com.li.rpc.cluster;

import com.li.common.URL;
import com.li.rpc.invoker.Invoker;
import com.li.rpc.invoker.RpcInvocation;
import org.checkerframework.common.reflection.qual.Invoke;

import java.util.List;

public interface LoadBalance {
    <T> Invoker<T> select(List<Invoker<T>> providerList, URL url, RpcInvocation invocation);
}

package com.li.rpc.cluster;

import com.li.common.URL;
import com.li.rpc.invoker.Invoker;
import com.li.rpc.invoker.RpcInvocation;

import java.util.List;

public class LeastActiveLoadBalance extends AbstractLoadBalance{
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, RpcInvocation invocation) {
        return null;
    }
}

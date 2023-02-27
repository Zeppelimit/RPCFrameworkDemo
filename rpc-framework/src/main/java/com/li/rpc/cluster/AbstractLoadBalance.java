package com.li.rpc.cluster;

import com.li.common.URL;
import com.li.rpc.invoker.Invoker;
import com.li.rpc.invoker.RpcInvocation;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

public abstract class AbstractLoadBalance implements LoadBalance{

    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, RpcInvocation invocation) {
        if (CollectionUtils.isEmpty(invokers)) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        return doSelect(invokers, url, invocation);
    }

    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, RpcInvocation invocation);

    int getWeight(Invoker<?> invoker, RpcInvocation invocation) {
        Map<String, Object> attributes = invoker.getUrl().getAttribute();
        attributes.putIfAbsent("weight", new Integer(2));

        return (int) attributes.get("weight");
    }
}

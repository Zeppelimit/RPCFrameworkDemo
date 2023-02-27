package com.li.rpc.invoker;

import com.li.Exception.RpcException;
import com.li.registry.RegistryDirectory;
import com.li.rpc.RpcResult;
import com.li.rpc.cluster.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
public class FailfastClusterInvoker<T> extends AbstractClusterInvoker<T>{

    public FailfastClusterInvoker(RegistryDirectory directory) {
        super(directory);
    }

    @Override
    protected RpcResult doInvoke(RpcInvocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
        Invoker<T> invoker = select(loadbalance, invocation, invokers, null);

        try {
            return invoker.invoke(invocation);
        } catch (RuntimeException e) {
            log.error("快速失败");
            throw e;
        }
    }
}

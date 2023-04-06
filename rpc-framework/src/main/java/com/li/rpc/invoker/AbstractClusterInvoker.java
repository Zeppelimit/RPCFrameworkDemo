package com.li.rpc.invoker;

import com.li.Exception.RpcException;
import com.li.common.URL;
import com.li.registry.RegistryDirectory;
import com.li.rpc.RpcResult;
import com.li.rpc.cluster.LoadBalance;
import com.li.rpc.cluster.RandomLoadBalance;
import com.li.rpc.cluster.RoundRobinLoadBalance;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.Invocation;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
@Data
@Slf4j
public abstract class AbstractClusterInvoker<T> implements ClusterInvoker<T>{

    private RegistryDirectory<T> directory;

    private URL url;

    private boolean availableCheck;

    private volatile int reselectCount = 10;

    public AbstractClusterInvoker(RegistryDirectory<T> directory) {
        this.directory = directory;
        this.url = directory.getConsumerUrl();
    }

    public Class<T> getInterface() {
        return getDirectory().getServiceType();
    }

    @Override
    public URL getRegistryUrl() {
        return url;
    }


    @Override
    public RegistryDirectory<T> getDirectory() {
        return directory;
    }

    @Override
    public RpcResult invoke(RpcInvocation invocation) throws RpcException {

        List<Invoker<T>> invokers = list(invocation);

        LoadBalance loadbalance = initLoadBalance(invokers, invocation);

        return doInvoke(invocation, invokers, loadbalance);
    }

    private LoadBalance initLoadBalance(List<Invoker<T>> invokers, RpcInvocation invocation) {
        return new RandomLoadBalance();
    }

    protected Invoker<T> select(LoadBalance loadbalance, RpcInvocation invocation,
                                List<Invoker<T>> invokers, List<Invoker<T>> selected){
        if(CollectionUtils.isEmpty(invokers)){
            return null;
        }

        Invoker<T> invoker = doSelect(loadbalance, invocation, invokers, selected);

        return invoker;
    }

    private Invoker<T> doSelect(LoadBalance loadbalance, RpcInvocation invocation,
                                List<Invoker<T>> invokers, List<Invoker<T>> selected) {
        if (CollectionUtils.isEmpty(invokers)) {
            return null;
        }
        if (invokers.size() == 1) {
            Invoker<T> tInvoker = invokers.get(0);
            return tInvoker;
        }
        Invoker<T> invoker = loadbalance.select(invokers, getUrl(), invocation);

        //可用性判定？
        boolean isSelected = selected != null && selected.contains(invoker);

        if (isSelected) {
            try {
                Invoker<T> rInvoker = reselect(loadbalance, invocation, invokers, selected, availableCheck);
                if (rInvoker != null) {
                    invoker = rInvoker;
                } else {
                    //Check the index of current selected invoker, if it's not the last one, choose the one at index+1.
                    int index = invokers.indexOf(invoker);
                    try {
                        //Avoid collision
                        invoker = invokers.get((index + 1) % invokers.size());
                    } catch (Exception e) {
                        log.warn(e.getMessage() + " may because invokers list dynamic change, ignore.", e);
                    }
                }
            } catch (Throwable t) {
                log.error("cluster reselect fail reason is :" + t.getMessage() + " if can not solve, you can set cluster.availablecheck=false in url", t);
            }
        }

        return invoker;
    }

    private Invoker<T> reselect(LoadBalance loadbalance, RpcInvocation invocation,
                                List<Invoker<T>> invokers, List<Invoker<T>> selected, boolean availableCheck){

        List<Invoker<T>> reselectInvokers = new ArrayList<>(Math.min(invokers.size(), reselectCount));

        if (reselectCount >= invokers.size()) {
            for (Invoker<T> invoker : invokers) {
                // 检查可用性


                if (selected == null || !selected.contains(invoker)) {
                    reselectInvokers.add(invoker);
                }
            }
        } else {
            for (int i = 0; i < reselectCount; i++) {
                // select one randomly
                Invoker<T> invoker = invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
                // 检查可用性

                // de-duplication
                if (selected == null || !selected.contains(invoker) || !reselectInvokers.contains(invoker)) {
                    reselectInvokers.add(invoker);
                }
            }
        }

        if (!reselectInvokers.isEmpty()) {
            return loadbalance.select(reselectInvokers, getUrl(), invocation);
        }


        //如果reselectInvokers是空的


        return null;
    }


    protected abstract RpcResult doInvoke(RpcInvocation invocation, List<Invoker<T>> invokers,
                                       LoadBalance loadbalance) throws RpcException;

    protected List<Invoker<T>> list(RpcInvocation invocation) throws RuntimeException {
        return getDirectory().doList(invocation);
    }

    @Override
    public void destroy() {
        setAvailableCheck(false);
    }

    @Override
    public boolean isAvailable() {
        return availableCheck;
    }
}

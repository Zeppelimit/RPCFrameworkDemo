package com.li.registry;


import com.li.common.URL;
import com.li.rpc.invoker.Invoker;
import com.li.rpc.invoker.RpcInvocation;
import com.li.rpc.invoker.RpcRefInvoker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Data
public class RegistryDirectory<T> implements NotifyListener{

    private ZookeeperClient zookeeperClient;

    private ZookeeperRegistryService zookeeperRegistryService;

    private Map<URL,Invoker<T>> urlInvokerMap = new ConcurrentHashMap<>();

    private Map<String, Invoker<T>> methodInvokersMap = new ConcurrentHashMap<>();

    private URL consumerUrl;

    private Class<T> serviceType;

    public RegistryDirectory(URL url, Class<T> serviceType){
        this.consumerUrl = url;
        this.zookeeperClient = ZookeeperClient.getRegistryClient(url);
        this.zookeeperRegistryService = new ZookeeperRegistryService(zookeeperClient);
        this.serviceType = serviceType;
    }

    public List<Invoker<T>> list(RpcInvocation invocation) throws Exception {
        List<Invoker<T>> invokers = doList(invocation);


        //路由


        return invokers;
    }
    public List<Invoker<T>> doList(RpcInvocation invocation) throws RuntimeException {

        List<Invoker<T>> list = new ArrayList<>();
        for(Invoker invoker : urlInvokerMap.values()){
            list.add(invoker);
        }

        return list;
    }


    public void subscribe(URL url) throws Exception {
        List<URL> urls = zookeeperRegistryService.subscribe(url.getPath() + "/providers", this);
        notify(urls);
    }

    public void unSubscribe(URL url) {

    }


    private void refreshInvoker(List<URL> invokerUrls) {

        Map<URL, Invoker<T>> oldUrlInvokerMap = this.urlInvokerMap;

        Map<URL, Invoker<T>> newUrlInvokerMap = toInvoker(invokerUrls);

        this.urlInvokerMap = newUrlInvokerMap;

        destroyUnusedInvokers(oldUrlInvokerMap, newUrlInvokerMap);
    }

    private Map<URL, Invoker<T>> toInvoker(List<URL> urls){
        Map<URL, Invoker<T>> newUrlInvokerMap = new ConcurrentHashMap<>();

        if (CollectionUtils.isEmpty(urls)) {
            return newUrlInvokerMap;
        }

        for(URL providerUrl : urls){
            RpcRefInvoker<T> rpcRefInvoker = new RpcRefInvoker<>(providerUrl, serviceType, providerUrl.getVersion(), providerUrl.getGroup());

            newUrlInvokerMap.computeIfAbsent(providerUrl, k -> rpcRefInvoker);

        }

        return newUrlInvokerMap;
    }






    private void destroyUnusedInvokers(Map<URL, Invoker<T>> oldUrlInvokerMap, Map<URL, Invoker<T>> newUrlInvokerMap){
        if (oldUrlInvokerMap != null) {
            for (URL key : oldUrlInvokerMap.keySet()) {
                if (null != key && !newUrlInvokerMap.containsKey(key)) {
                    Invoker<T> invoker = oldUrlInvokerMap.get(key);
                    if (invoker != null) {
                        try {
                            invoker.destroy();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public boolean isAvailable(){
        if(CollectionUtils.isEmpty(urlInvokerMap)){
            return false;
        }
        return true;
    }

    @Override
    public synchronized void notify(List<URL> urls) {
        refreshInvoker(urls);
    }
}

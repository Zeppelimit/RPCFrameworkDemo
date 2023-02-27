package com.li.spring;

import com.li.common.NetUtils;
import com.li.common.RegistryUrlUtils;
import com.li.common.ServiceManager;
import com.li.common.URL;
import com.li.spring.config.RpcProperties;
import lombok.Data;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import com.li.registry.protocol.Protocol;
import com.li.registry.protocol.RegistryProtocol;
import com.li.rpc.exporter.Exporter;
import com.li.rpc.proxy.DefaultProxyFactory;
import com.li.rpc.invoker.Invoker;
import com.li.rpc.proxy.RpcProxyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Data
public class ServiceBean<T> implements InitializingBean ,ApplicationContextAware, BeanNameAware ,ApplicationEventPublisherAware {

    private transient String beanName;

    private transient volatile boolean exported;

    private String version;

    private String group;
    private Class<T> interfaceClass;
    private ApplicationContext applicationContext;

    private T serviceImpl;

    private RpcProxyFactory proxyFactory = new DefaultProxyFactory();

    private final List<Exporter<?>> exporters = new ArrayList<Exporter<?>>();

    private ApplicationEventPublisher applicationEventPublisher;

    private Protocol registryProtocol = new RegistryProtocol();

    @Autowired
    private RpcProperties rpcProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<ServiceBean> serviceBeanList = ServiceManager.serviceBeanList;
        serviceBeanList.add(this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void export() throws KeeperException {
        List<URL> registryUrls = RegistryUrlUtils.getRegistryUrls(this,rpcProperties);
        for(URL url : registryUrls){
            URL url1 = new URL("rpc", NetUtils.getServerIp(), rpcProperties.getServerPort(), url.getInterfaceName(),
                    url.getGroup(), url.getVersion(),200);
            Invoker<?> invoker = proxyFactory.getInvoker(serviceImpl, interfaceClass, url1);
            //待处理。。。。。
            Map<String, Object> serviceList = ServiceManager.serviceList;
            Map<String, Invoker> invokerMap = ServiceManager.invokerMap;
            serviceList.putIfAbsent(interfaceClass.getName() + "." + group + "." + version, serviceImpl);
            invokerMap.putIfAbsent(interfaceClass.getName() + "." + group + "." + version, invoker);
            Exporter<?> export = registryProtocol.export(url, invoker);
            exporters.add(export);
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }


}

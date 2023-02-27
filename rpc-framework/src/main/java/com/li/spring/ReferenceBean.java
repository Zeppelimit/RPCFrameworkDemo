package com.li.spring;

import com.li.common.ReferenceManager;
import com.li.common.URL;
import com.li.registry.protocol.Protocol;
import com.li.registry.protocol.RegistryProtocol;
import com.li.rpc.invoker.Invoker;
import com.li.rpc.proxy.DefaultProxyFactory;
import com.li.spring.config.RpcProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import java.util.List;

public class ReferenceBean <T> implements FactoryBean<T>,
        ApplicationContextAware, BeanNameAware, InitializingBean {
    private String name;

    private Class<?> interfaceClass;
    private ApplicationContext applicationContext;

    private String interfaceName;

    private String version;

    private String group;

    private String fieldName;

    private DefaultProxyFactory defaultProxyFactory = new DefaultProxyFactory();

    private Invoker<T> invoker;
    @Resource
    private RpcProperties rpcProperties;

    private transient volatile T ref;

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    @Override
    public T getObject() throws Exception {
            return get();
    }

    private synchronized T get() throws Exception {
        if (ref == null) {
            getProxyObject();
        }

        return ref;
    }

    private synchronized void getProxyObject() throws Exception {
        invoker = getInvoker();

        ref =  (T) defaultProxyFactory.getProxy(new Class[]{interfaceClass}, invoker);

    }


    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public void afterPropertiesSet() {
        List<ReferenceBean> referenceBeans = ReferenceManager.referenceBeanList;
        referenceBeans.add(this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Invoker<T> getInvoker() throws Exception {
        Protocol registryProtocol = new RegistryProtocol();
        String[] address = rpcProperties.getRegistryAddress().split(":");

        URL url = new URL("registry",address[0],Integer.parseInt(address[1]), interfaceName, group, version);


        return (Invoker<T>) registryProtocol.refer(interfaceClass, url);
    }

    protected void checkInvokers() throws IllegalStateException{
        if (!invoker.isAvailable()) {
            throw new IllegalStateException("Failed to check the status of the service "
                    + interfaceName
                    + ". No provider available for the service ");
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public DefaultProxyFactory getDefaultProxyFactory() {
        return defaultProxyFactory;
    }

    public void setDefaultProxyFactory(DefaultProxyFactory defaultProxyFactory) {
        this.defaultProxyFactory = defaultProxyFactory;
    }

    public void setInvoker(Invoker<T> invoker) {
        this.invoker = invoker;
    }

    public RpcProperties getRpcProperties() {
        return rpcProperties;
    }

    public void setRpcProperties(RpcProperties rpcProperties) {
        this.rpcProperties = rpcProperties;
    }
}

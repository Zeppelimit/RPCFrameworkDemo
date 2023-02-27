package com.li.common;

import com.li.rpc.invoker.Invoker;
import com.li.spring.ServiceBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceManager {

    public static final String BEAN_NAME = "serviceManager";

    public static Map<String, Object> serviceList = new ConcurrentHashMap<>();

    public static List<ServiceBean> serviceBeanList = new CopyOnWriteArrayList<>();

    public static Map<String, Invoker> invokerMap = new ConcurrentHashMap<>();

    public static Invoker getInvoker(String serviceName){
        return invokerMap.getOrDefault(serviceName,null);
    }

}

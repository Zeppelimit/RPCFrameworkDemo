package com.li.common;

import com.li.spring.ServiceBean;
import com.li.spring.config.RpcProperties;

import java.util.ArrayList;
import java.util.List;

public class RegistryUrlUtils {


    public static List<URL> getRegistryUrls(ServiceBean serviceBean, RpcProperties rpcProperties){
        String registryAddress = rpcProperties.getRegistryAddress();
        String[] addresses = registryAddress.split(",");
        List<URL> urls = new ArrayList<>();
        for(String address : addresses){
            String[] split = address.split(":");
            urls.add(new URL("com/li/registry",split[0],Integer.parseInt(split[1]),serviceBean.getInterfaceClass().getName(),
                    serviceBean.getGroup()
                    ,serviceBean.getVersion(), 1));
        }
        return urls;
    }

    private static String resolveNodePath(ServiceBean serviceBean,RpcProperties rpcProperties) {
        StringBuilder path = new StringBuilder("/rpcTest");
        path.append("/" + serviceBean.getGroup())
                .append("/"+serviceBean.getInterfaceClass().getName())
                .append("/providers")
                .append("/" + NetUtils.getServerIp() + ":" + rpcProperties.getServerPort());
        return path.toString();
    }
}

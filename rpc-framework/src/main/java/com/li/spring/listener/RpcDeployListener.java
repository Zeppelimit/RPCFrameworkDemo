package com.li.spring.listener;

import com.li.common.ServiceManager;
import com.li.spring.ServiceBean;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

import static org.springframework.util.ObjectUtils.nullSafeEquals;

public class RpcDeployListener implements ApplicationListener<ApplicationContextEvent>, ApplicationContextAware{

    private  ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (nullSafeEquals(applicationContext, event.getSource())) {
            if (event instanceof ContextRefreshedEvent) {
                try {
                    exportServices();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void exportServices() throws Exception {
        List<ServiceBean> serviceBeanList = ServiceManager.serviceBeanList;
        for(ServiceBean sc : serviceBeanList){
            sc.export();
        }
    }

}

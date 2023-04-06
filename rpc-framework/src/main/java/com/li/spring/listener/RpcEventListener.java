package com.li.spring.listener;

import com.li.spring.ServiceBean;
import com.li.spring.config.RpcProperties;
import com.li.transport.client.NewNettyClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import com.li.registry.ZookeeperClient;
import com.li.common.ServiceManager;
import com.li.transport.client.NettyClient;
import com.li.transport.server.NettyServer;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static org.springframework.util.ObjectUtils.nullSafeEquals;

@Slf4j
public class RpcEventListener implements ApplicationListener<ApplicationContextEvent>, ApplicationContextAware {
    private ApplicationContext applicationContext;

    private NettyServer nettyServer;

    private ZookeeperClient zookeeperClient;

    @Resource
    private RpcProperties rpcProperties;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (nullSafeEquals(applicationContext, event.getSource())) {
            if (event instanceof ContextRefreshedEvent) {
                onContextRefreshedEvent((ContextRefreshedEvent) event);
            } else if (event instanceof ContextClosedEvent) {
                onContextClosedEvent((ContextClosedEvent) event);
            }
        }
    }

    private void onContextClosedEvent(ContextClosedEvent event) {
        log.info("关闭注册中心的连接");
        Map<String, ZookeeperClient> zookeeperConnSet = ZookeeperClient.zookeeperConnSet;
        for(ZookeeperClient zookeeperClient : zookeeperConnSet.values()){
            zookeeperClient.doClose();
        }

        if(nettyServer != null){
            log.info("关闭服务");
            nettyServer.close();
        }

        log.info("关闭客户端连接");
        for(NewNettyClient nettyClient : NewNettyClient.socketChannelMap.values()){
            nettyClient.disconnect();
        }
    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event) throws Exception {
        List<ServiceBean> serviceBeanList = ServiceManager.serviceBeanList;

        if(!serviceBeanList.isEmpty()){
            nettyServer = NettyServer.getNettyServer();
            nettyServer.start(rpcProperties.getServerPort());
        }
    }



}

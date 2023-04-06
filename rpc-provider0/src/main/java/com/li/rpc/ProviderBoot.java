package com.li.rpc;


import com.li.transport.server.NettyServerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.li.spring.annotation.EnableRpc;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@EnableRpc(scanBasePackages = "com.li")
@SpringBootApplication
@Slf4j
public class ProviderBoot {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(ProviderBoot.class, args);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(()->{
            int qps = HelloImpl.atomicInteger.getAndSet(0);
            log.info("qps : " + qps);
        },0, 1000, TimeUnit.MILLISECONDS);
    }
}

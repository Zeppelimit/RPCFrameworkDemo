package com.li.rpc;

import com.li.spring.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@EnableRpc(scanBasePackages = "com.li")
@SpringBootApplication
@Slf4j
public class ProviderBoot {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(ProviderBoot.class, args);
//        String[] beanDefinitionNames = run.getBeanDefinitionNames();
//        for(String s : beanDefinitionNames){
//            log.info(s);
//        }
//        Map<String, Object> serviceList = ServiceManager.serviceList;
//        for(Map.Entry t : serviceList.entrySet()){
//            log.info("{},{}",t.getKey(),t.getValue());
//        }

    }
}

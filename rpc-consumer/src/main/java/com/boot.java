package com;

import com.li.Consume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.li.spring.annotation.RpcComponentScan;

@RpcComponentScan
@SpringBootApplication
@Slf4j
public class boot {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext run = SpringApplication.run(boot.class, args);
        String[] beanDefinitionNames = run.getBeanDefinitionNames();
        for(String s : beanDefinitionNames){
            log.info(s);
        }
        Consume consume = (Consume) run.getBean("consume");
        consume.sayHello();
    }
}

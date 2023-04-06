package com;

import com.li.Consume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.li.spring.annotation.RpcComponentScan;

import java.util.concurrent.*;

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

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 100, 4000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(300));
        scheduledExecutorService.scheduleAtFixedRate(()->{
            for(int i = 0; i<100 ; i++){
                threadPoolExecutor.submit(()->{
                    try {
                        consume.sayHello();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
//        System.out.println(consume.sayHello());
    }
}

package com.li.spring.config;

import com.li.spring.annotation.RpcService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnClass(RpcService.class)
@EnableConfigurationProperties(RpcProperties.class)
public class RpcAutoConfiguration {

    public RpcAutoConfiguration() {

    }

}

package com.li.spring.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties(prefix = "rpc")
@Validated
public class RpcProperties {

    private String providerName = "li";

    @NotNull(message = "zookeeper服务地址不能为空")
    private String registryAddress;

    private int serverPort = 8083;
}

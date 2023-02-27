package com.li.spring.processor;

import com.li.spring.annotation.RpcComponentScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import com.li.registry.ZookeeperClient;
import com.li.spring.listener.RpcDeployListener;
import com.li.spring.listener.RpcEventListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
@Slf4j
public class RpcComponentScanRegistrar implements ImportBeanDefinitionRegistrar {


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //注册通用的bean
        registerCommonBeans(registry);
        //获取需要扫描的包
        Set<String> packagesToScan = getPackagesToScan(importingClassMetadata);
        //将ServiceAnnotationBeanPostProcessor注册到容器
        registerServiceAnnotationBeanPostProcessor(packagesToScan, registry);
    }


    private void registerServiceAnnotationBeanPostProcessor(Set<String> packagesToScan, BeanDefinitionRegistry registry) {
        log.info("注册ServiceAnnotationBeanPostProcessor到容器");
        BeanDefinitionBuilder builder = rootBeanDefinition(ServiceAnnotationBeanPostProcessor.class);
        builder.addConstructorArgValue(packagesToScan);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
    }



    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        //获取EnableRpc的属性
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(RpcComponentScan.class.getName()));
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        String[] value = attributes.getStringArray("value");
        // Appends value array attributes
        Set<String> packagesToScan = new LinkedHashSet<String>(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }

        if (packagesToScan.isEmpty()) {
            //返回默认的包
            return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return packagesToScan;
    }

    private void registerCommonBeans(BeanDefinitionRegistry registry){
        log.info("注册ReferenceAnnotationBeanPostProcessor到容器");
        registerInfrastructureBean(registry, ReferenceAnnotationBeanPostProcessor.BEAN_NAME,
                ReferenceAnnotationBeanPostProcessor.class);
        log.info("注册RpcEventListener到容器");
        registerInfrastructureBean(registry, RpcEventListener.class.getName(), RpcEventListener.class);
        registerInfrastructureBean(registry, RpcDeployListener.class.getName(), RpcDeployListener.class);
//        log.info("注册ZookeeperClient到容器");
//        registerZookeeperClient(registry,rpcProperties.getRegistryAddress());
    }

    private void registerZookeeperClient(BeanDefinitionRegistry registry, String address) {
        BeanDefinitionBuilder builder = rootBeanDefinition(ZookeeperClient.class);
        builder.addConstructorArgValue(address);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        registry.registerBeanDefinition("zookeeperClient",beanDefinition);
    }

    static boolean registerInfrastructureBean(BeanDefinitionRegistry beanDefinitionRegistry,
                                              String beanName,
                                              Class<?> beanType) {

        boolean registered = false;

        if (!beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            RootBeanDefinition beanDefinition = new RootBeanDefinition(beanType);
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
            registered = true;

            if (log.isDebugEnabled()) {
                log.debug("The Infrastructure bean definition [" + beanDefinition
                        + "with name [" + beanName + "] has been registered.");
            }
        }

        return registered;
    }
}

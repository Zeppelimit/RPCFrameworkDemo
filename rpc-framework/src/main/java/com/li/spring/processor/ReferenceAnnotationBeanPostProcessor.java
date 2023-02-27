package com.li.spring.processor;

import com.li.spring.annotation.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import com.li.spring.ReferenceBean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;


public class ReferenceAnnotationBeanPostProcessor  implements BeanPostProcessor, ApplicationContextAware, BeanFactoryPostProcessor{
    public static final String BEAN_NAME = "referenceAnnotationBeanProcessor";

    private ApplicationContext applicationContext;
    private BeanDefinitionRegistry registry;

    private final ConcurrentMap<String, List<InjectInfo>> injectedFieldReferenceBeanCache =
            new ConcurrentHashMap<>(32);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.registry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for(String beanName : beanNames){
            Class<?> beanType;
            beanType = beanFactory.getType(beanName);
            List<InjectInfo> list = null;

            if (beanType != null) {
                Field[] declaredFields = beanType.getDeclaredFields();
                for(Field f : declaredFields){

                    RpcReference annotation = f.getAnnotation(RpcReference.class);

                    if(annotation != null){

                        Class<?> interfaceClass = f.getType();

                        String fieldName = f.getName();

                        String interfaceName = interfaceClass.getName();

                        Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);

                        AbstractBeanDefinition beanDefinition = buildReferenceBeanDefinition(attributes,interfaceClass,interfaceName,fieldName);

                        String referenceBeanName = generateReferenceBeanName(attributes,interfaceName);

                        if(!registry.containsBeanDefinition(referenceBeanName)){
                            registry.registerBeanDefinition(referenceBeanName, beanDefinition);
                        }

                        if(list == null){
                            list = new ArrayList<>();
                        }

                        list.add(new InjectInfo(f.getName(), referenceBeanName));
                    }

                }
            }

            if(list != null){
                injectedFieldReferenceBeanCache.put(beanName,list);
            }

        }
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        List<InjectInfo> injectInfos = injectedFieldReferenceBeanCache.getOrDefault(beanName,null);
        if(injectInfos != null){
            Class<?> beanClass = bean.getClass();
            for(InjectInfo injectInfo : injectInfos){
                try {
                    Field injectedField = beanClass.getDeclaredField(injectInfo.getFieldName());
                    injectedField.setAccessible(true);
                    Object impl = applicationContext.getBean(injectInfo.getReferenceBeanName());
                    injectedField.set(bean, impl);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }

    private AbstractBeanDefinition buildReferenceBeanDefinition(Map<String, Object> attributes, Class<?> interfaceClass, String interfaceName,String fieldName) {
        BeanDefinitionBuilder builder = rootBeanDefinition(ReferenceBean.class);

        builder.addPropertyValue("version",attributes.get("version"));
        builder.addPropertyValue("group",attributes.get("group"));
        builder.addPropertyValue("interfaceName",interfaceName);
        builder.addPropertyValue("interfaceClass",interfaceClass);
        builder.addPropertyValue("fieldName",fieldName);

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

        return beanDefinition;
    }

    private String generateReferenceBeanName(Map<String, Object> annotationAttributes, String interfaceName) {
        StringBuilder serviceBeanName = new StringBuilder("ReferenceBean.");
        serviceBeanName.append(annotationAttributes.getOrDefault("group",null));
        serviceBeanName.append(interfaceName+".");
        serviceBeanName.append(annotationAttributes.getOrDefault("version",null));
        return serviceBeanName.toString();
    }

    private class InjectInfo{
        private String fieldName;

        private String referenceBeanName;

        public String getFieldName() {
            return fieldName;
        }

        public String getReferenceBeanName() {
            return referenceBeanName;
        }

        public InjectInfo(String fieldName, String referenceBeanName) {
            this.fieldName = fieldName;
            this.referenceBeanName = referenceBeanName;
        }
    }

}

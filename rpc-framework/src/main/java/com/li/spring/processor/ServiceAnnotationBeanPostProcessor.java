package com.li.spring.processor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.li.spring.ServiceBean;
import com.li.spring.annotation.RpcService;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.ClassUtils.getAllInterfacesForClass;
import static org.springframework.util.ClassUtils.resolveClassName;
@Slf4j
public class ServiceAnnotationBeanPostProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
    public static final String BEAN_NAME = "rpcServiceAnnotationPostProcessor";
    protected final Set<String> packagesToScan;

    private BeanDefinitionRegistry registry;
    private ClassLoader ClassLoader;

    private ApplicationContext applicationContext;

    public ServiceAnnotationBeanPostProcessor(Set<String> packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        this.registry = beanDefinitionRegistry;
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(beanDefinitionRegistry);
        scanner.setBeanNameGenerator(new AnnotationBeanNameGenerator());

        scanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));

        for (String packageToScan : packagesToScan) {

            Set<BeanDefinitionHolder> beanDefinitionHolders = getDefinitionHolders(scanner, packageToScan);

            if (!CollectionUtils.isEmpty(beanDefinitionHolders)) {

                processBeanDefinitionHolder(beanDefinitionHolders);
            }

        }
    }

    private void processBeanDefinitionHolder(Set<BeanDefinitionHolder> beanDefinitionHolders) {
        for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {

            String beanClassName = beanDefinitionHolder.getBeanDefinition().getBeanClassName();

            assert beanClassName != null;
            Class<?> beanClass = resolveClassName(beanClassName,ClassLoader);

            Annotation service = findAnnotation(beanClass, RpcService.class);

            if(service == null) continue;

            Map<String, Object> serviceAnnotationAttributes = AnnotationUtils.getAnnotationAttributes(service);

            String serviceInterfaceName = resolveInterfaceName(serviceAnnotationAttributes, beanClass);

            Class<?> serviceInterfaceClass = resolveInterfaceClass(serviceAnnotationAttributes, beanClass);

            String annotatedServiceBeanName = beanDefinitionHolder.getBeanName();

            // ServiceBean Bean name
            String serviceBeanName = generateServiceBeanName(serviceAnnotationAttributes, serviceInterfaceName);

            AbstractBeanDefinition serviceBeanDefinition =
                    buildServiceBeanDefinition(beanClass.getName()
                            ,serviceAnnotationAttributes
                            ,serviceInterfaceClass
                            ,annotatedServiceBeanName);

            registerServiceBeanDefinition(serviceBeanName, serviceBeanDefinition);

        }
    }

    private Class<?> resolveInterfaceClass(Map<String, Object> serviceAnnotationAttributes, Class<?> beanClass) {
        Class<?> interfaceClass = (Class<?>) serviceAnnotationAttributes.get("interfaceClass");
        if (interfaceClass == null || void.class.equals(interfaceClass)) { // default or set void.class for purpose.
            interfaceClass = null;
        }

        if (interfaceClass == null) {
            Class<?>[] allInterfaces = getAllInterfacesForClass(beanClass);
            if (allInterfaces.length > 0) {
                interfaceClass = allInterfaces[0];
            }
        }

        Assert.notNull(interfaceClass, "@Service interfaceClass() or interfaceName() or interface class must be present!");
        Assert.isTrue(interfaceClass.isInterface(), "The annotated type must be an interface!");
        return interfaceClass;
    }

    private Set<BeanDefinitionHolder> getDefinitionHolders(ClassPathBeanDefinitionScanner scanner, String packageToScan) {
        scanner.scan(packageToScan);
        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(packageToScan);
        Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<>(beanDefinitions.size());
        BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

        for (BeanDefinition beanDefinition : beanDefinitions) {

            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            beanDefinitionHolders.add(beanDefinitionHolder);

        }
        return beanDefinitionHolders;
    }

    private void registerServiceBeanDefinition(String beanName, AbstractBeanDefinition serviceBeanDefinition) {
        // check service bean
        if (registry.containsBeanDefinition(beanName)) {
            BeanDefinition existingDefinition = registry.getBeanDefinition(beanName);
            if (existingDefinition.equals(serviceBeanDefinition)) {
                // exist equipment bean definition
                return;
            }

        }

        registry.registerBeanDefinition(beanName, serviceBeanDefinition);
        if (log.isInfoEnabled()) {
            log.info("Register ServiceBean[" + beanName + "]: " + serviceBeanDefinition);
        }


    }

    private AbstractBeanDefinition buildServiceBeanDefinition(String beanClassName, Map<String, Object> serviceAnnotationAttributes, Class serviceInterface, String annotatedServiceBeanName) {
        BeanDefinitionBuilder builder = rootBeanDefinition(ServiceBean.class);
        String implBeanName = getImplBeanName(ClassUtils.getShortName(beanClassName));
        builder.addPropertyReference("serviceImpl", implBeanName);
        builder.addPropertyValue("interfaceClass", serviceInterface);
        builder.addPropertyValue("beanName", annotatedServiceBeanName);
        builder.addPropertyValue("version", serviceAnnotationAttributes.get("version"));
        builder.addPropertyValue("group", serviceAnnotationAttributes.get("group"));

        return builder.getBeanDefinition();
    }

    private String generateServiceBeanName(Map<String, Object> serviceAnnotationAttributes, String serviceInterface) {
        StringBuilder serviceBeanName = new StringBuilder("ServiceBean.");
        serviceBeanName.append(serviceAnnotationAttributes.get("group")+".");
        serviceBeanName.append(serviceInterface);
        serviceBeanName.append(serviceAnnotationAttributes.get("version"));
        return serviceBeanName.toString();
    }

    private String resolveInterfaceName(Map<String, Object> serviceAnnotationAttributes, Class<?> beanClass) {
        String interfaceClassName = (String) serviceAnnotationAttributes.get("interfaceName");
        if (StringUtils.hasText(interfaceClassName)) {
            return interfaceClassName;
        }

        Class<?> interfaceClass = (Class<?>) serviceAnnotationAttributes.get("interfaceClass");
        if (interfaceClass == null || void.class.equals(interfaceClass)) { // default or set void.class for purpose.
            interfaceClass = null;
        }

        if (interfaceClass == null) {
            Class<?>[] allInterfaces = getAllInterfacesForClass(beanClass);
            if (allInterfaces.length > 0) {
                interfaceClass = allInterfaces[0];
            }
        }

        Assert.notNull(interfaceClass, "@Service interfaceClass() or interfaceName() or interface class must be present!");
        Assert.isTrue(interfaceClass.isInterface(), "The annotated type must be an interface!");
        return interfaceClass.getName();
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        BeanDefinitionBuilder builder = rootBeanDefinition(ZookeeperRegistryService.class);
//        builder.addConstructorArgValue("127.0.0.1:2181");
//        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
//        registry.registerBeanDefinition("registryService",beanDefinition);

    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public  String getImplBeanName(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }

        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                Character.isUpperCase(name.charAt(0))){
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }


}

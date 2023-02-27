package com.li.spring.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface RpcReference {
    String version() default "1.0.0";

    String group() default "default";

    String url() default "";

    Class<?> interfaceClass() default void.class;

    String interfaceName() default "";
}

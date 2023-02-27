package com.li.spring.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcService {
    String version() default "1.0.0";

    String group() default "default";

    Class<?> interfaceClass() default void.class;;

    String interfaceName() default "";
}

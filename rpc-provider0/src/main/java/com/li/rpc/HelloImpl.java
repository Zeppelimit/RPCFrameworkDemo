package com.li.rpc;

import com.li.Hello;
import com.li.spring.annotation.RpcService;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@RpcService
public class HelloImpl implements Hello {
    public static AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public String sayHello(){
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        atomicInteger.incrementAndGet();
        return "hello1";
    }
}

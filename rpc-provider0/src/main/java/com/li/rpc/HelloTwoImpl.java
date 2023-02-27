package com.li.rpc;

import com.li.HelloTwo;
import com.li.spring.annotation.RpcService;

@RpcService
public class HelloTwoImpl implements HelloTwo {
    @Override
    public String sayHello(String str) {
        int a = 1/0;
        return "hello," + str;
    }
}

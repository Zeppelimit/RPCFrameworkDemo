package com.li.rpc;

import com.li.Hello;
import com.li.spring.annotation.RpcService;
@RpcService
public class HelloImpl implements Hello {
    @Override
    public String sayHello() {
        return "hello1";
    }
}

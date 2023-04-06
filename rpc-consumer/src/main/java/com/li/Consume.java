package com.li;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;
import com.li.spring.annotation.RpcReference;
@Component
public class Consume {
    @RpcReference
    private Hello hello;

    @RpcReference(group = "li")
    private HelloTwo hello0;

    public String sayHello() throws Exception{
        return hello.sayHello();
    }
}

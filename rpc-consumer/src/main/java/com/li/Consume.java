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

    public void sayHello() throws Exception{
//        System.out.println(hello.sayHello());
//        System.out.println(hello.sayHello());
//        System.out.println(hello.sayHello());
        System.out.println(hello0.sayHello("1"));
        System.out.println(hello0.sayHello("2"));
        System.out.println(hello0.sayHello("3"));
    }

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().create();
        Object ans = 13;
        Test test = new Test("133", ans);
        String s1 = gson.toJson(test);
        Object o = gson.fromJson(s1, Test.class);
        Test str = (Test) o;
        System.out.println(str.aa + str.ans);

    }

    static class Test{
        String aa;
        Object ans;

        public Test(String aa, Object ans) {
            this.aa = aa;
            this.ans = ans;
        }

        public String getAa() {
            return aa;
        }

        public void setAa(String aa) {
            this.aa = aa;
        }

        public Object getAns() {
            return ans;
        }

        public void setAns(Object ans) {
            this.ans = ans;
        }

    }
}

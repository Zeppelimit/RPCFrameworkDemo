package com.li.rpc.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.li.rpc.AppRpcResult;
import com.li.rpc.RpcResult;
import com.li.rpc.invoker.Invoker;
import com.li.common.URL;
import com.li.rpc.invoker.RpcInvocation;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@Slf4j
public class InvokerInvocationHandler implements InvocationHandler {

    private Invoker<?> invoker;

    private URL url;

    public InvokerInvocationHandler(Invoker<?> invoker) {
        this.invoker = invoker;
        this.url = invoker.getUrl();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }

        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length == 0) {
            if ("toString".equals(methodName)) {
                return invoker.toString();
            } else if ("$destroy".equals(methodName)) {
                invoker.destroy();
                return null;
            } else if ("hashCode".equals(methodName)) {
                return invoker.hashCode();
            }
        } else if (parameterTypes.length == 1 && "equals".equals(methodName)) {
            return invoker.equals(args[0]);
        }

        RpcInvocation rpcInvocation = new RpcInvocation(method.getName(), method.getParameterTypes(), args);

        try {
            RpcResult res = invoker.invoke(rpcInvocation);
            Throwable exception = res.getException();

            if (exception != null) {
                try {
                    Object stackTrace = exception.getStackTrace();
                    if (stackTrace == null) {
                        exception.setStackTrace(new StackTraceElement[0]);
                    }
                } catch (Exception e) {
                    // ignore
                }
                throw exception;
            }

            return res.getValue();
        } catch (Exception e) {
            log.error("调用失败");
            log.error(e.getMessage());
            throw e;
        }
    }

}

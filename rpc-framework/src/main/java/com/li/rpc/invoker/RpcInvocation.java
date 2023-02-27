package com.li.rpc.invoker;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

@Data
public class RpcInvocation {
    private String interfaceName;

    private String methodName;

    private String version;

    private String group;

    private Class<?>[] parameterType;

    private Object[] args;

    public RpcInvocation(String interfaceName, String version, String group) {
        this.interfaceName = interfaceName;
        this.version = version;
        this.group = group;
        this.args = args == null ? new Object[0] : args;
    }

    public RpcInvocation(String methodName, Class<?>[] parameterType, Object[] args) {
        this.methodName = methodName;
        this.args = args;
        this.parameterType = parameterType;
    }

    public String getRefServiceName(){
        return interfaceName + "." + group + "." + version;
    }

    public String resolveNodePath() {
        StringBuilder path = new StringBuilder("/rpcTest");
        path.append("/" + group)
                .append("/" + interfaceName)
                .append("/providers");
        return path.toString();
    }
}

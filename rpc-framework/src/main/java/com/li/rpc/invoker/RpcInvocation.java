package com.li.rpc.invoker;

import io.protostuff.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

@Data
@NoArgsConstructor
public class RpcInvocation {
    @Tag(1)
    private String interfaceName;
    @Tag(2)
    private String methodName;
    @Tag(3)
    private String version;
    @Tag(4)
    private String group;
    @Tag(5)
    private Class<?>[] parameterType;
    @Tag(6)
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

package com.li.rpc.invoker;

import com.li.Exception.RpcException;
import com.li.common.URL;
import com.li.registry.RegistryDirectory;
import com.li.rpc.PromiseContext;
import com.li.rpc.RpcResult;

public class MockClusterInvoker<T> implements ClusterInvoker<T> {

    private RegistryDirectory<T> directory;

    private Invoker<T> invoker;

    public MockClusterInvoker(RegistryDirectory<T> directory, Invoker<T> invoker) {
        this.directory = directory;
        this.invoker = invoker;
    }

    @Override
    public URL getRegistryUrl() {
        return null;
    }

    @Override
    public RegistryDirectory<T> getDirectory() {
        return null;
    }

    @Override
    public Class<T> getInterface() {
        return null;
    }

    @Override
    public RpcResult invoke(RpcInvocation invocation) throws RpcException {
        if(!directory.isAvailable()){
            throw new IllegalStateException("Failed to check the status of the service "
                    + directory.getServiceType()
                    + ". No provider available for the service ");
        }
        return invoker.invoke(invocation);
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isAvailable() {
        return directory.isAvailable();
    }
}

package com.li.rpc.invoker;

import com.li.common.URL;
import com.li.registry.RegistryDirectory;
import org.aopalliance.intercept.Invocation;

public interface ClusterInvoker<T> extends Invoker<T>{
    URL getRegistryUrl();

    RegistryDirectory<T> getDirectory();



}

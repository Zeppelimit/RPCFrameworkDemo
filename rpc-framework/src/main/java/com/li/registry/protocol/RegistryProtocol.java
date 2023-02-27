package com.li.registry.protocol;

import com.li.common.URL;
import com.li.registry.RegistryDirectory;
import com.li.registry.ZookeeperClient;
import com.li.registry.ZookeeperRegistryService;
import com.li.rpc.exporter.Exporter;
import com.li.rpc.exporter.RpcExporter;
import com.li.rpc.invoker.*;
import lombok.Data;
import org.apache.zookeeper.KeeperException;

@Data
public class RegistryProtocol extends AbstractProtocol {

    @Override
    public int getDefaultPort() {
        return 0;
    }

    @Override
    public <T> Exporter<T> export(URL registryUrl, Invoker<T> invoker) throws KeeperException {
        ZookeeperClient registryClient = ZookeeperClient.getRegistryClient(registryUrl);
        ZookeeperRegistryService zookeeperRegistryService = new ZookeeperRegistryService(registryClient);
        URL url = invoker.getUrl();
        zookeeperRegistryService.register(url.getPath() + "/providers/" + url);

        return new RpcExporter<T>(invoker);
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws Exception {

        RegistryDirectory<T> registryDirectory = new RegistryDirectory<T>(url, type);

        registryDirectory.subscribe(url);

        return new MockClusterInvoker<>(registryDirectory, new FailoverClusterInvoker<>(registryDirectory));
//        return new FailoverClusterInvoker<>(registryDirectory);
    }


    @Override
    public void destroy() {

    }
}
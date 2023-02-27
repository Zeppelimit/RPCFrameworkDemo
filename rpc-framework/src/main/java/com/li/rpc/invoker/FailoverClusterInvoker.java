package com.li.rpc.invoker;

import com.li.common.NetUtils;
import com.li.common.URL;
import com.li.registry.RegistryDirectory;
import com.li.rpc.RpcResult;
import com.li.rpc.cluster.LoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Slf4j
public class FailoverClusterInvoker<T> extends AbstractClusterInvoker<T>{

    public FailoverClusterInvoker(RegistryDirectory<T> directory) {
        super(directory);
    }

    @Override
    protected RpcResult doInvoke(RpcInvocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RuntimeException {
        List<Invoker<T>> copyInvokers = invokers;


        int len = 2 + 1;

        if (len <= 0) {
            len = 1;
        }

        List<Invoker<T>> invoked = new ArrayList<Invoker<T>>(copyInvokers.size());

        Set<String> providers = new HashSet<String>(len);

        for (int i = 0; i < len; i++) {


            //Reselect before retry to avoid a change of candidate `invokers`.
            //NOTE: if `invokers` changed, then `invoked` also lose accuracy.
            if (i > 0) {
                //检查可用性
//                checkWhetherDestroyed();
                copyInvokers = list(invocation);
                // check again
                checkInvokers(copyInvokers, invocation);
            }
            Invoker<T> invoker = select(loadbalance, invocation, copyInvokers, invoked);
            invoked.add(invoker);
//            RpcContext.getContext().setInvokers((List) invoked);
            try {
                RpcResult result = invoker.invoke(invocation);

                return result;
            } catch (RuntimeException e) {
                log.info("重试 ");
                throw e;
            } finally {
                providers.add(invoker.getUrl().getAddress());
            }
        }

        return null;
    }

    protected void checkInvokers(List<Invoker<T>> invokers, RpcInvocation invocation){
        if (CollectionUtils.isEmpty(invokers)) {
            URL url = getUrl();
            throw new IllegalStateException("Failed to check the status of the service "
                    + invocation.getInterfaceName()
                    + ". No provider available for the service "
                    + getInterface() +
                    (url.getVersion() == null ? "" : ":" + url.getVersion())
                    + " from the url "
                    + url
                    + " to the consumer "
                    + NetUtils.getServerIp() + " use dubbo version " + url.getVersion());
        }
    }
}

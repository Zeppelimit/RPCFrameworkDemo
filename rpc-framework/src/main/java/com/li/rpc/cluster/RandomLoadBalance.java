package com.li.rpc.cluster;

import com.li.common.URL;
import com.li.rpc.invoker.Invoker;
import com.li.rpc.invoker.RpcInvocation;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class RandomLoadBalance extends AbstractLoadBalance{
    @Override
    public <T> Invoker doSelect(List<Invoker<T>> providerList, URL url, RpcInvocation invocation) {
        Random rand=new Random();
        int len = providerList.size();
        if(len == 1){
            return providerList.get(0);
        }
        int[] pre = new int[len+1];
        pre[0] = 0;
        for(int i = 1; i <= len; i++){
            pre[i] = pre[i-1] + providerList.get(i-1).getUrl().getOrder();
        }
        int ran = rand.nextInt(pre[pre.length-1])+1;
        int l=1,r=pre.length-1;
        while(l<r){
            int mid=(l+r)>>1;
            if(pre[mid]>=ran){
                r=mid;
            }else{
                l=mid+1;
            }
        }

        return providerList.get(l-1);
    }


}

package com.li.registry.protocol;

import com.li.common.ServiceManager;
import com.li.rpc.AppRpcResult;
import com.li.rpc.RpcResult;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import com.li.rpc.invoker.RpcInvocation;
import com.li.rpc.invoker.Invoker;
import com.li.serialize.MessageFactory;
import com.li.serialize.struct.RpcMessage;
import com.li.transport.DefaultRequest;
import com.li.transport.DefaultResponse;

@Slf4j
public class RpcProtocol extends AbstractProtocol{

    @Override
    public int getDefaultPort() {
        return 8083;
    }

    public void reply(ChannelHandlerContext ctx, DefaultRequest request) throws Exception {
        long id = request.getId();
        RpcInvocation invocation = request.getInvocation();

        log.info("调用方法 {}.{}.{}",invocation.getInterfaceName(),invocation.getMethodName(),invocation.getVersion());

        String refServiceName = invocation.getRefServiceName();

        Invoker invoker = ServiceManager.getInvoker(refServiceName);

        RpcResult res = invoker.invoke(invocation);

        DefaultResponse response = new DefaultResponse();
        response.setId(id);
        response.setStateCode(0);
        response.setRetData((AppRpcResult) res);

        RpcMessage rpcMessage = MessageFactory.buildRspMessage(response, id);
        log.info("回复请求：{}", id);

        ctx.writeAndFlush(rpcMessage);
    }


    @Override
    public void destroy() {

    }
}

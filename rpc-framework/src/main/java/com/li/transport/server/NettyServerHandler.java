package com.li.transport.server;

import com.li.common.ApplicationContextUtils;
import com.li.transport.DefaultRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import com.li.registry.protocol.RpcProtocol;
import com.li.transport.client.struct.RpcMessage;

@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof RpcMessage){

            RpcMessage rpcMessage = (RpcMessage) msg;

            DefaultRequest request = (DefaultRequest) rpcMessage.getData();

            RpcProtocol rpcProtocol = (RpcProtocol) ApplicationContextUtils.getBean("RpcProtocol");

            rpcProtocol.reply(ctx, request);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端 {} 下线",ctx.channel().remoteAddress());
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        log.info("与客户端 {} 建立连接", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }




}

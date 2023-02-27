package com.li.transport.client;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import com.li.serialize.struct.RpcHeader;
import com.li.serialize.struct.RpcMessage;
import com.li.serialize.DataType;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class HeartBeatReqHandler extends ChannelDuplexHandler {

    private final int MAX_HEART_COUNT = 3;

    private Set<Long> sendHeartBeatSet = new CopyOnWriteArraySet<>();

    private AtomicLong heartBeatId = new AtomicLong();

    public HeartBeatReqHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    private NettyClient nettyClient;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            if(sendHeartBeatSet.size() >= MAX_HEART_COUNT){
                nettyClient.reconnect();
            }else{
                RpcMessage beatMessage = buildHeatBeat();
                log.info("发送Ping: {}",beatMessage.getRpcHeader().getId());
                ctx.writeAndFlush(beatMessage);
                sendHeartBeatSet.add(beatMessage.getRpcHeader().getId());
            }
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof RpcMessage){
            RpcMessage rpcMessage = (RpcMessage) msg;
            RpcHeader rpcHeader = rpcMessage.getRpcHeader();
            byte isEvent = rpcHeader.getIsEvent();
            if(isEvent == DataType.HEART_BEAT.getValue()){
                sendHeartBeatSet.clear();
            }else{
                ctx.fireChannelRead(msg);
            }
        }
    }

    private RpcMessage buildHeatBeat(){
        RpcMessage rpcMessage = new RpcMessage();
        RpcHeader rpcHeader = new RpcHeader();
        rpcHeader.setMessageType((byte) DataType.HEART_BEAT.getValue());
        rpcHeader.setIsEvent((byte) 0);
        rpcHeader.setId(heartBeatId.incrementAndGet());
        rpcMessage.setRpcHeader(rpcHeader);
        return rpcMessage;
    }
}

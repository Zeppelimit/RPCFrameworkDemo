package com.li.transport.client;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import com.li.transport.client.struct.RpcHeader;
import com.li.transport.client.struct.RpcMessage;
import com.li.serialize.DataType;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Deprecated
public class HeartBeatReqHandler extends ChannelDuplexHandler {

    private final int MAX_HEART_COUNT = 3;

    private int currentHeartCount = 0;
    private Set<Long> sendHeartBeatSet = new CopyOnWriteArraySet<>();

    private AtomicLong heartBeatId = new AtomicLong();

    private IdleStateHandler idleStateHandler;
    public static final Integer MAX_GAP = 100;
    public static final Integer MIN_GAP = 20;
    public static Integer minGap = 20;

    public static Integer maxGap = 100;

    public HeartBeatReqHandler(NettyClient nettyClient, IdleStateHandler idleStateHandler) {
        this.nettyClient = nettyClient;
        this.idleStateHandler = idleStateHandler;
    }

    private NettyClient nettyClient;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            if(sendHeartBeatSet.size() >= MAX_HEART_COUNT){
                modifyTime(idleStateHandler, false);
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
                modifyTime(idleStateHandler, true);
                sendHeartBeatSet.clear();
                System.out.println("收到pong" + rpcHeader.getId());
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

    public synchronized void modifyTime(IdleStateHandler idleStateHandler, boolean flag) throws NoSuchFieldException, IllegalAccessException {
        Class<IdleStateHandler> idleStateHandlerClass = IdleStateHandler.class;
        Field readerIdleTimeNanos = idleStateHandlerClass.getDeclaredField("readerIdleTimeNanos");
        readerIdleTimeNanos.setAccessible(true);
        long cur = (long)readerIdleTimeNanos.get(idleStateHandler);
        long tmp;
        if(flag){
            tmp =  (cur + maxGap)/2>MAX_GAP?(cur + maxGap)/2:MAX_GAP;
        }else {
            tmp = (cur + minGap)/2>MIN_GAP?(cur + minGap)/2:MIN_GAP;
        }
        readerIdleTimeNanos.set(idleStateHandler, (cur + tmp)/2);
    }
}

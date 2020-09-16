package com.wataru.blockchain.core.net.client;

import com.wataru.blockchain.core.net.Notifier;
import com.wataru.blockchain.core.net.packet.BlockPacket;
import com.wataru.blockchain.core.net.packet.payload.ResponsePayload;
import com.wataru.blockchain.core.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
public class NodeCoreClientHandler extends ChannelInboundHandlerAdapter {

    public static final Map<String, Pair<Notifier.Callback, Long>> callbackMap = new ConcurrentHashMap<>();
    private static final long RESPONSE_TIMEOUT = 120;
    static {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            callbackMap.entrySet().removeIf(item -> System.currentTimeMillis() - item.getValue().getValue() > RESPONSE_TIMEOUT  * 1000);
        }, RESPONSE_TIMEOUT, RESPONSE_TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        BlockPacket<ResponsePayload> blockPacket = (BlockPacket<ResponsePayload>) msg;
        Pair<Notifier.Callback, Long> callback = callbackMap.get(blockPacket.getUuid());
        if (callback != null) {
            try {
                callback.getKey().apply(blockPacket.getData());
            } catch (Exception e) {
                log.error("", e);
            }
        }
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            log.warn("{}: {}", ctx.channel(), cause.getMessage());
        } else {
            log.error("", cause);
        }
        ctx.close();
    }
}

package com.wataru.blockchain.core.net.server;

import com.wataru.blockchain.core.net.Node;
import com.wataru.blockchain.core.net.Notifier;
import com.wataru.blockchain.core.net.listener.*;
import com.wataru.blockchain.core.net.packet.BlockPacket;
import com.wataru.blockchain.core.net.packet.payload.ResponsePayload;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
public class NodeCoreServerHandler extends ChannelInboundHandlerAdapter {
    private final ThreadPoolExecutor httpCoreReqThreadPool = new ThreadPoolExecutor(
            1,
            10000,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            r -> new Thread(r, "HttpCoreReqThreadPool-" + r.hashCode()),
            (r, executor) -> {
                throw new RuntimeException("HttpCoreReqThreadPool is EXHAUSTED!");
            });

    private final BlockchainCoreBizApi blockchainCoreBizApi = new BlockchainCoreBizApi();

    private final Map<Class, PacketListener> listenerMap = new HashMap<>();
    private final LinkedHashSet<String> packetUuidRedundant = new LinkedHashSet<>();
    private final Queue<String> packetUuidRedundantQueue = new LinkedBlockingQueue<>();

    private Node localNode;

    public NodeCoreServerHandler(Node localNode) {
        this.localNode = localNode;
        addListener(new HeartbeatPacketListener());
        addListener(new AddBlockPacketListener());
        addListener(new ConsensusPacketListener());
        addListener(new TransactionPacketListener());
    }

    public void addListener(PacketListener listener) {
        for (Method declaredMethod : listener.getClass().getDeclaredMethods()) {
            for (Class<?> parameterType : declaredMethod.getParameterTypes()) {
                listenerMap.put(parameterType, listener);
            }
        }
    }

    public boolean checkIfRedundant(BlockPacket blockPacket) {
        if (packetUuidRedundant.size() > 10000) {
            synchronized (packetUuidRedundant) {
                Iterator<String> iter = packetUuidRedundant.iterator();
                while (iter.hasNext() && packetUuidRedundant.size() > 10000) {
                    iter.next();
                    iter.remove();
                }
            }
        }
        boolean redundant =  packetUuidRedundant.contains(blockPacket.getUuid());
        packetUuidRedundant.add(blockPacket.getUuid());
        return redundant;
    }

    private void spread(BlockPacket blockPacket) {
        Notifier.broadcast(blockPacket, Arrays.asList(localNode));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            BlockPacket blockPacket = (BlockPacket) msg;
            if (checkIfRedundant(blockPacket)) {
                return;
            }
            PacketListener listener = listenerMap.get(blockPacket.getData().getClass());
            if (listener == null) {
                return;
            }
            ResponsePayload result = listener.onMessage(blockPacket.getData());
            ctx.write(new BlockPacket<>(result, blockPacket.getUuid()));
            if (blockPacket.getSpread()) {
                spread(blockPacket);
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            ctx.flush();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("", cause);
        ctx.close();
    }
}

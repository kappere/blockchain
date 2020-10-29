package com.wataru.blockchain.core.net.client;

import com.wataru.blockchain.core.net.*;
import com.wataru.blockchain.core.net.model.Pair;
import com.wataru.blockchain.core.net.packet.BlockPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class NodeCoreClient implements InitializingBean, DisposableBean {
    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private Map<Node, Channel> channelMap;
    private NodeCoreClientHandler nodeCoreClientHandler;
    private final ExecutorService spreadExecutorService = Executors.newFixedThreadPool(10);

    public NodeCoreClient() {
        nodeCoreClientHandler = new NodeCoreClientHandler();
        channelMap = new HashMap<>();
        new Thread(() -> {
            workerGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
//                            pipeline.addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS));
                            pipeline.addLast(new NodeCoreDecoder());
                            pipeline.addLast(new NodeCoreEncoder());
                            pipeline.addLast(nodeCoreClientHandler);
                        }
                    });
        }).start();
        Notifier.nodeCoreClient = this;
    }

    public synchronized void refreshClient(NodeList nodeList) {
        channelMap.entrySet().removeIf(item -> {
            if (nodeList.getNodes().contains(item.getKey())) {
                item.getValue().flush();
                item.getValue().close();
                return true;
            }
            return false;
        });
        nodeList.getNodes().forEach(this::newClient);
    }

    public void newClient(Node node) {
        try {
            if (!channelMap.containsKey(node)) {
                Channel channel = bootstrap.connect(node.getIp(), node.getPort()).sync().channel();
                Channel prev = channelMap.put(node, channel);
                if (prev != null) {
                    prev.flush();
                    prev.close();
                }
            }
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    public <T> void broadcast(BlockPacket<T> blockPacket, Notifier.Callback callback, List<Node> excludeNodes) {
        spreadExecutorService.submit(() -> channelMap.forEach((node, channel) -> {
            if (!excludeNodes.contains(node)) {
                if (callback != null) {
                    NodeCoreClientHandler.callbackMap.put(blockPacket.getUuid(), new Pair<>(callback, System.currentTimeMillis()));
                }
                channel.writeAndFlush(blockPacket);
            }
        }));
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void destroy() {
        channelMap.forEach((node, channel) -> channel.close());
        workerGroup.shutdownGracefully();
    }
}

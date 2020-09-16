package com.wataru.blockchain.core.net.server;

import com.wataru.blockchain.core.config.BlockchainCoreConfig;
import com.wataru.blockchain.core.net.AdminApi;
import com.wataru.blockchain.core.net.NodeCoreDecoder;
import com.wataru.blockchain.core.net.NodeCoreEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class NodeCoreServer implements InitializingBean, DisposableBean {
    @Autowired
    private AdminApi adminApi;
    @Autowired
    private BlockchainCoreConfig blockchainCoreConfig;

    public void start(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Channel channel;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
//                                pipeline.addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS));
                            pipeline.addLast(new NodeCoreDecoder());
                            pipeline.addLast(new NodeCoreEncoder());
                            pipeline.addLast(new NodeCoreServerHandler(blockchainCoreConfig.getLocalNode()));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            channel = bootstrap.bind(port).sync().channel();
            log.info("Start blockchain core server");
        } catch (InterruptedException e) {
            log.error("Blockchain core server start failed!", e);
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            try {
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Blockchain core server interrupted!", e);
                throw new RuntimeException(e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }

    @Override
    public void afterPropertiesSet() {
        if ("node".equalsIgnoreCase(blockchainCoreConfig.getRole())) {
            adminApi.register();
        }
    }

    @Override
    public void destroy() {
        adminApi.unregister();
    }
}

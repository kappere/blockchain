package com.wataru.blockchain.core.config;

import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.net.Node;
import com.wataru.blockchain.core.net.NodeRegistry;
import com.wataru.blockchain.core.net.AdminApi;
import com.wataru.blockchain.core.net.client.NodeCoreClient;
import com.wataru.blockchain.core.net.server.NodeCoreServer;
import com.wataru.blockchain.core.util.IpUtil;
import lombok.Getter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@EnableScheduling
@ComponentScan("com.wataru.blockchain.core.task")
public class BlockchainCoreConfig implements DisposableBean {
    @Value("${blockchain.admin.registry}")
    private String adminRegistry;
    @Getter
    @Value("${blockchain.role}")
    private String role;
    @Getter
    private final Integer coreServerPort = IpUtil.findAvailablePort(8088);
    @Getter
    private Node localNode;

    @Bean
    public AdminApi adminApi() {
        return new AdminApi(adminRegistry);
    }

    @Bean
    public NodeCoreClient nodeCoreClient() {
        return new NodeCoreClient();
    }

    @Bean
    public NodeCoreServer nodeCoreServer() {
        NodeCoreServer nodeCoreServer = new NodeCoreServer();
        nodeCoreServer.start(coreServerPort);
        return nodeCoreServer;
    }

    @PostConstruct
    public void init() throws IOException {
        new Blockchain(1);
        NodeRegistry.clientRegistry = new NodeRegistry();
        NodeRegistry.clientRegistry.setLocalPort(coreServerPort);
        localNode = new Node(IpUtil.getLocalIpAddress(), coreServerPort);
        localNode.setExpireTime(-1);
//        Blockchain.instance.restore("chain_" + localNode.toString().replaceAll("[:.]", "_") + ".dat");
    }

    @Override
    public void destroy() throws Exception {
        Blockchain.instance.store("chain_" + localNode.toString().replaceAll("[:.]", "_") + ".dat");
    }
}

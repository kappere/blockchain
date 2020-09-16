package com.wataru.blockchain.admin.biz;

import com.wataru.blockchain.core.net.Node;
import com.wataru.blockchain.core.net.NodeRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NodeAdminRegistry extends NodeRegistry {
    private static final long REGISTER_RATE = 30;
    private static final long REGISTER_CLEAR_RATE = 30;
    public static NodeAdminRegistry adminRegistry;
    @Getter
    private final AtomicBoolean ready;

    public NodeAdminRegistry() {
        ready = new AtomicBoolean(false);
        // 等待获取所有节点注册
        new Thread(() -> {
            try {
                Thread.sleep((REGISTER_RATE + 30) * 1000);
                ready.set(true);
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }).start();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            adminRegistry.getNodeList().removeExpireNode((REGISTER_RATE + 30) * 1000);
        }, REGISTER_CLEAR_RATE, REGISTER_CLEAR_RATE, TimeUnit.SECONDS);
    }

    public void registerNode(Node node) {
        node.setExpireTime(System.currentTimeMillis());
        getNodeList().addNode(node);
    }

    public void unregisterNode(Node node) {
        getNodeList().removeNode(node);
    }

    public void refreshNode(Node node) {
        node.setExpireTime(System.currentTimeMillis());
        getNodeList().addNode(node);
    }
}

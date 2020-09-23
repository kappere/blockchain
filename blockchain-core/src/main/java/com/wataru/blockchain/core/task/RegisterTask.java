package com.wataru.blockchain.core.task;

import com.wataru.blockchain.core.net.AdminApi;
import com.wataru.blockchain.core.net.NodeRegistry;
import com.wataru.blockchain.core.primitive.Miner;
import com.wataru.blockchain.core.task.annotation.TaskSync;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Async
@Slf4j
public class RegisterTask {
    @Autowired
    private AdminApi adminApi;
    @Value("${blockchain.miner.address}")
    private String minerAddress;

    @Scheduled(cron = "0/30 * * * * ?")
    @TaskSync
    public void doRegister() {
        // 节点心跳
        adminApi.heartBeat();
        // 同步节点
        adminApi.syncNodes();
        // 共识
        NodeRegistry.clientRegistry.consensus();
    }

//    @Scheduled(cron = "0/5 * * * * ?")
//    @TaskSync
//    public void doHeartbeat() {
//        Notifier.broadcast(new HeartbeatPayload(), new Notifier.Callback() {
//            @Override
//            public void apply(ResponsePayload payload) {
//                System.out.println(payload);
//            }
//        });
//    }

    @Scheduled(cron = "0/5 * * * * ?")
    @TaskSync
    public void mine() throws InterruptedException {
        if (NodeRegistry.clientRegistry.getNodeList().getNodes().isEmpty()) {
            return;
        }
        new Miner(minerAddress).mine();
    }
}

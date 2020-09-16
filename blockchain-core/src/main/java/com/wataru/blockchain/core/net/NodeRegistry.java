package com.wataru.blockchain.core.net;

import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.net.packet.payload.ConsensusPayload;
import com.wataru.blockchain.core.net.packet.payload.ResponsePayload;
import com.wataru.blockchain.core.primitive.transaction.Transaction;
import com.wataru.blockchain.core.util.IpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class NodeRegistry {
    protected NodeList nodeList;
    public static NodeRegistry clientRegistry;
    private static final String localIp = IpUtil.getLocalIpAddress();
    private int localPort;

    public NodeRegistry() {
        nodeList = new NodeList();
    }

    public void consensus() {
        for (Node node : nodeList.getNodes()) {
            // 忽略本机地址
            if (node.getIp().equals(localIp) && node.getPort() == localPort) {
                continue;
            }
        }
        // 同步最长链
        Notifier.collect(new ConsensusPayload(), new Notifier.Callback() {
            @Override
            public void apply(ResponsePayload payload) {
                Blockchain blockchain = (Blockchain) payload.getData();
                if (blockchain.getChain().size() > Blockchain.instance.getChain().size() && blockchain.checkChainValid()) {
                    Blockchain.instance.replaceChain(blockchain);
                    log.info("Our chain was replaced!");
                }
            }
        });
    }
}

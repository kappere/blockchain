package com.wataru.blockchain.core.net;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class NodeList {
    private List<Node> nodes;

    public NodeList() {
        this.nodes = new ArrayList<>();
    }

    public synchronized void syncNodes(List<Node> syncNodes, Node localNode) {
        List<Node> newNodes = new ArrayList<>();
        newNodes.add(localNode);
        syncNodes.forEach(node -> {
            if (!node.equals(localNode)) {
                newNodes.add(node);
            }
        });
        nodes = newNodes;
    }

    public synchronized void addNode(Node node) {
        int idx = nodes.indexOf(node);
        if (idx == -1) {
            nodes.add(node);
            log.info("New node register: {}", node.toString());
            log.info("Current nodes: {}", nodes);
        } else {
            nodes.get(idx).setExpireTime(node.getExpireTime());
        }
    }

    public synchronized void removeNode(Node node) {
        int idx = nodes.indexOf(node);
        if (idx >= 0) {
            nodes.remove(node);
            log.info("Unregister node: {}", node.toString());
            log.info("Current nodes: {}", nodes);
        }
    }

    public synchronized void removeExpireNode(long expireMillisTime) {
        nodes.removeIf(item -> item.getExpireTime() > 0 && (System.currentTimeMillis() - item.getExpireTime() > expireMillisTime));
    }

    @Override
    public String toString() {
        return nodes.toString();
    }
}

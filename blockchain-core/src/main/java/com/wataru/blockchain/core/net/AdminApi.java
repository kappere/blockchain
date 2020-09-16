package com.wataru.blockchain.core.net;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wataru.blockchain.core.config.BlockchainCoreConfig;
import com.wataru.blockchain.core.net.client.NodeCoreClient;
import com.wataru.blockchain.core.net.model.Response;
import com.wataru.blockchain.core.util.HttpUtil;
import com.wataru.blockchain.core.util.IpUtil;
import com.wataru.blockchain.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用来和admin交互
 */
@Slf4j
public class AdminApi {
    @Autowired
    private BlockchainCoreConfig blockchainCoreConfig;
    @Autowired
    private NodeCoreClient nodeCoreClient;
    private final String adminHost;

    public AdminApi(String adminRegistry) {
        adminHost = adminRegistry.startsWith("http") ? adminRegistry : "http://" + adminRegistry;
    }

    public void heartBeat() {
        String responseJson = HttpUtil.post(
                adminHost + "/core/node/heartBeat",
                5000,
                null,
                JsonUtil.toJson(blockchainCoreConfig.getLocalNode()));
        if (responseJson != null) {
            Response<Object> response = JsonUtil.fromJson(responseJson, Response.class);
            if (!response.getSuccess()) {
                log.error("Heart beat to admin failed! Message: {}", response.getMessage());
            }
        }
    }

    public void register() {
        String responseJson = HttpUtil.post(
                adminHost + "/core/node/register",
                5000,
                null,
                JsonUtil.toJson(blockchainCoreConfig.getLocalNode()));
        if (responseJson != null) {
            Response<Object> response = JsonUtil.fromJson(responseJson, Response.class);
            if (!response.getSuccess()) {
                log.error("Register to admin failed! Message: {}", response.getMessage());
                return;
            }
            log.info("Register to admin success!");
        }
    }

    public void unregister() {
        String responseJson = HttpUtil.post(
                adminHost + "/core/node/unregister",
                5000,
                null,
                JsonUtil.toJson(blockchainCoreConfig.getLocalNode()));
        if (responseJson != null) {
            Response<Object> response = JsonUtil.fromJson(responseJson, Response.class);
            if (!response.getSuccess()) {
                log.error("Unregister from admin failed! Message: {}", response.getMessage());
                return;
            }
            log.info("Unregister from admin success!");
        }
    }

    public void syncNodes() {
        String responseJson = HttpUtil.get(
                adminHost + "/core/node/list",
                5000,
                null);
        if (responseJson != null) {
            Response<NodeList> response = JsonUtil.fromJson(responseJson, new TypeReference<Response<NodeList>>() {});
            NodeList nodeList = NodeRegistry.clientRegistry.getNodeList();
            if (!response.getSuccess()) {
                log.error("Sync nodes failed! Message: {}", response.getMessage());
                if (nodeList.getNodes().isEmpty()) {
                    nodeList.syncNodes(Collections.emptyList(), blockchainCoreConfig.getLocalNode());
                }
            } else {
                nodeList.syncNodes(response.getData().getNodes(), blockchainCoreConfig.getLocalNode());
            }
            nodeCoreClient.refreshClient(NodeRegistry.clientRegistry.nodeList);
        }
    }
}

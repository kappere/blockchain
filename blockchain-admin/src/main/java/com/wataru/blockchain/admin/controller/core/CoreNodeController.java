package com.wataru.blockchain.admin.controller.core;

import com.wataru.blockchain.admin.biz.NodeAdminRegistry;
import com.wataru.blockchain.core.net.Node;
import com.wataru.blockchain.core.net.model.Response;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core/node")
public class CoreNodeController implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        NodeAdminRegistry.adminRegistry = new NodeAdminRegistry();
    }

    @GetMapping("/list")
    public Response<Object> nodeList() {
        if (!NodeAdminRegistry.adminRegistry.getReady().get()) {
            return Response.error("Unavailable nodes!");
        }
        return Response.success(NodeAdminRegistry.adminRegistry.getNodeList());
    }

    @PostMapping("/heartBeat")
    public Response<Object> heartBeat(@RequestBody Node node) {
        if (!node.validate()) {
            return Response.error("Error node format");
        }
        NodeAdminRegistry.adminRegistry.refreshNode(node);
        return Response.success();
    }

    @PostMapping("/register")
    public Response<Object> register(@RequestBody Node node) {
        if (!node.validate()) {
            return Response.error("Error node format");
        }
        NodeAdminRegistry.adminRegistry.registerNode(node);
        return Response.success();
    }

    @PostMapping("/unregister")
    public Response<Object> unregister(@RequestBody Node node) {
        if (!node.validate()) {
            return Response.error("Error node format");
        }
        NodeAdminRegistry.adminRegistry.unregisterNode(node);
        return Response.success();
    }
}

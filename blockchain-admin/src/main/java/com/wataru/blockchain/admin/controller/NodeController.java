package com.wataru.blockchain.admin.controller;

import com.wataru.blockchain.admin.biz.NodeAdminRegistry;
import com.wataru.blockchain.core.net.model.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/node")
public class NodeController {

    @GetMapping("/list")
    public Response<Object> nodeList() {
        return Response.success(NodeAdminRegistry.adminRegistry.getNodeList());
    }
}

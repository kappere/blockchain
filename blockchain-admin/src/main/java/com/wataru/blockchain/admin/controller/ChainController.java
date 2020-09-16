package com.wataru.blockchain.admin.controller;

import com.wataru.blockchain.admin.dto.BlockChainDto;
import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.net.model.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chain")
public class ChainController {
    @GetMapping("/detail")
    public Response<BlockChainDto> detail() {
        return Response.success(new BlockChainDto(Blockchain.instance));
    }
}

package com.wataru.blockchain.core.net.server;

import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.net.model.Response;

public class BlockchainCoreBizApi {
    public Response<Object> chain() {
        return Response.success(Blockchain.instance);
    }
}

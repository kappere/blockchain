package com.wataru.blockchain.core.net.listener;

import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.net.packet.payload.AddBlockPayload;
import com.wataru.blockchain.core.net.packet.payload.ResponsePayload;

public class AddBlockPacketListener implements PacketListener<AddBlockPayload> {
    @Override
    public ResponsePayload onMessage(AddBlockPayload payload) {
        boolean result = Blockchain.instance.addBlock(payload.toBlock());
        return new ResponsePayload(result);
    }
}

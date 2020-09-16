package com.wataru.blockchain.core.net.listener;

import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.net.packet.payload.ConsensusPayload;
import com.wataru.blockchain.core.net.packet.payload.ResponsePayload;

public class ConsensusPacketListener implements PacketListener<ConsensusPayload> {
    @Override
    public ResponsePayload onMessage(ConsensusPayload payload) {
        return new ResponsePayload(Blockchain.instance);
    }
}

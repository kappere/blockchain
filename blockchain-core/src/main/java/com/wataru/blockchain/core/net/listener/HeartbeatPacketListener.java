package com.wataru.blockchain.core.net.listener;

import com.wataru.blockchain.core.net.packet.payload.HeartbeatPayload;
import com.wataru.blockchain.core.net.packet.payload.ResponsePayload;

public class HeartbeatPacketListener implements PacketListener<HeartbeatPayload> {
    @Override
    public ResponsePayload onMessage(HeartbeatPayload payload) {
        return new ResponsePayload("Heartbeat response!");
    }
}

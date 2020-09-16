package com.wataru.blockchain.core.net.listener;

import com.wataru.blockchain.core.net.packet.payload.ResponsePayload;

public interface PacketListener<T> {
    ResponsePayload onMessage(T payload);
}

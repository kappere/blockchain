package com.wataru.blockchain.core.net;

import com.wataru.blockchain.core.net.client.NodeCoreClient;
import com.wataru.blockchain.core.net.packet.BlockPacket;
import com.wataru.blockchain.core.net.packet.payload.BasePayload;
import com.wataru.blockchain.core.net.packet.payload.ResponsePayload;

import java.util.Collections;
import java.util.List;

public class Notifier {
    public static NodeCoreClient nodeCoreClient;
    public static void broadcast(BasePayload payload) {
        nodeCoreClient.broadcast(new BlockPacket<>(payload, true), null, Collections.emptyList());
    }
    public static void broadcast(BasePayload payload, Callback callback) {
        nodeCoreClient.broadcast(new BlockPacket<>(payload, true), callback, Collections.emptyList());
    }
    public static void broadcast(BlockPacket blockPacket, List<Node> excludeNodes) {
        nodeCoreClient.broadcast(blockPacket, null, excludeNodes);
    }
    public static void collect(BasePayload payload) {
        nodeCoreClient.broadcast(new BlockPacket<>(payload, false), null, Collections.emptyList());
    }
    public static void collect(BasePayload payload, Callback callback) {
        nodeCoreClient.broadcast(new BlockPacket<>(payload, false), callback, Collections.emptyList());
    }

    @FunctionalInterface
    public interface Callback {
        void apply(ResponsePayload payload);
    }
}

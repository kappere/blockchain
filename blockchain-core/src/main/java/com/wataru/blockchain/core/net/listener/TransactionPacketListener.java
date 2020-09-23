package com.wataru.blockchain.core.net.listener;

import com.wataru.blockchain.core.net.packet.payload.ResponsePayload;
import com.wataru.blockchain.core.net.packet.payload.TransactionPayload;
import com.wataru.blockchain.core.primitive.transaction.Transaction;
import com.wataru.blockchain.core.util.JsonUtil;

public class TransactionPacketListener implements PacketListener<TransactionPayload> {
    @Override
    public synchronized ResponsePayload onMessage(TransactionPayload payload) {
        Transaction transaction = payload.toTransaction();
        return new ResponsePayload(transaction.processTransaction());
    }
}

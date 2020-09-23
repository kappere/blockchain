package com.wataru.blockchain.core.net.packet.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wataru.blockchain.core.primitive.transaction.Transaction;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties
@EqualsAndHashCode(callSuper = true)
public class TransactionPayload extends BasePayload {
    private static final long serialVersionUID = 1L;
    private byte[] data;

    public TransactionPayload() {}

    public TransactionPayload(Transaction transaction) {
        this.data = transaction.serialize();
    }

    public Transaction toTransaction() {
        Transaction b = new Transaction();
        b.deserialize(data);
        return b;
    }
}

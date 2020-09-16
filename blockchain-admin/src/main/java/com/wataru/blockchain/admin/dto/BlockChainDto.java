package com.wataru.blockchain.admin.dto;

import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.primitive.block.Block;
import com.wataru.blockchain.core.primitive.transaction.Transaction;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BlockChainDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Block> chain;
    private int difficulty = 2;
    private List<Transaction> unconfirmedTransactions;
    public BlockChainDto() {
    }
    public BlockChainDto(Blockchain blockchain) {
        chain = blockchain.getChain();
        difficulty = blockchain.getDifficulty();
        unconfirmedTransactions = blockchain.getUnconfirmedTransactions();
    }
}

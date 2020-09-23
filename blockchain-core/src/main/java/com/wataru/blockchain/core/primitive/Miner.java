package com.wataru.blockchain.core.primitive;

import com.wataru.blockchain.core.net.Notifier;
import com.wataru.blockchain.core.net.packet.payload.AddBlockPayload;
import com.wataru.blockchain.core.net.packet.payload.ResponsePayload;
import com.wataru.blockchain.core.primitive.block.Block;
import com.wataru.blockchain.core.primitive.transaction.Transaction;
import com.wataru.blockchain.core.util.EncodeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class Miner {
    private String address;
    private Blockchain blockchain;
    private static final int TRANSACTIONS_PER_BLOCK = 3;

    public Miner(String address) {
        this.address = address;
        this.blockchain = Blockchain.instance;
    }

    public void mine() throws InterruptedException {
        Block block = doMine();
        if (block == null) {
            return;
        }
        Notifier.broadcast(new AddBlockPayload(block), new Notifier.Callback() {
            @Override
            public void apply(ResponsePayload payload) {
                log.info("Add block result: {}", payload.getData());
            }
        });
    }

    private Block doMine() throws InterruptedException {
        // 打包交易
        List<Transaction> newUnconfirmedTransactions = blockchain.collectUnconfirmedTransactions(TRANSACTIONS_PER_BLOCK);
        if (newUnconfirmedTransactions == null) {
            return null;
        }
        // 定义新区块
        Block block = createNewBlock(newUnconfirmedTransactions);
        // 开始挖矿
        boolean result = block.mine(blockchain.getDifficulty(), index -> index != Blockchain.instance.latestBlock().getIndex() + 1);
        if (result) {
            // 等待消息广播
            Thread.sleep(2000);
            return block;
        }
        return null;
    }

    private Block createNewBlock(List<Transaction> newUnconfirmedTransactions) {
        // 矿工地址
        ByteBlob.Byte160 publicKeyHash = Blockchain.addressToPublicKeyHash(address);
        Block block = new Block(blockchain.latestBlock(), System.currentTimeMillis());
        block.setTransactions(newUnconfirmedTransactions);
        // 计算奖励金额
        long award = 0L;
        for (Transaction transaction : newUnconfirmedTransactions) {
            award += transaction.getInputsValue() - transaction.getOutputsValue();
        }
        // 添加奖励交易
        Transaction awardTransaction = new Transaction(
                Collections.emptyList(),
                Collections.singletonList(new Transaction.TransactionOutput(award, LockScript.formatLockScript(publicKeyHash))));
        newUnconfirmedTransactions.add(0, awardTransaction);
        return block;
    }
}

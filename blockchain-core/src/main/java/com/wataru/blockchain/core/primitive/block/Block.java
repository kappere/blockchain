package com.wataru.blockchain.core.primitive.block;

import com.wataru.blockchain.core.primitive.transaction.Transaction;
import com.wataru.blockchain.core.util.EncodeUtil;
import com.wataru.blockchain.core.util.JsonUtil;
import com.wataru.blockchain.core.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 区块
 */
@Data
@Slf4j
public class Block {
    /**
     * 版本
     */
    private int version;

    /**
     * 前一个区块的Hash值
     */
    private String prevBlockHash;

    /**
     * 包含交易信息的Merkle树根
     */
    private String merkleRootHash;

    /**
     * 区块产生的时间
     */
    private long time;

    /**
     * 工作量证明(POW)的难度
     */
    private int bits;

    /**
     * 要找的符合POW的随机数
     */
    private int nonce;

    /**
     * 区块在区块链中的位置
     */
    private int index;

    /**
     * 当前区块的Hash值
     */
    private String hash;

    /**
     * 区块包含的交易
     */
    private List<Transaction> transactions;

    public Block() {}

    public Block(int index, long time) {
        this.index = index;
        this.setTime(time);
        this.transactions = new ArrayList<>();
    }

    public Block(Block latestBlock, long time) {
        this.index = latestBlock.getIndex() + 1;
        this.setPrevBlockHash(latestBlock.getHash());
        this.setTime(time);
        this.transactions = new ArrayList<>();
    }

    /**
     * 计算block的hash值
     */
    public String computeHash() {
        String hashStr = String.format("%d,%d,%d,%s,%s", index, getTime(), getNonce(), getPrevBlockHash(), JsonUtil.toJson(transactions));
        return EncodeUtil.sha256(hashStr);
    }

    public boolean mine(int difficulty, Function<Integer, Boolean> interruptCondition) {
        log.info("Mining block {}", index);
        String proof = proofOfWork(difficulty, interruptCondition);
        if (proof == null) {
            return false;
        }
        hash = proof;
        log.info("Mined block {}, proof: {}, block:\n{}", index, proof, JsonUtil.toPrettyJson(this));
        return true;
    }

    public boolean validatePoofOfWork(int difficulty) {
        String hashPrefix = StringUtil.dup("0", difficulty);
        String h = computeHash();
        return h.substring(0, difficulty).equals(hashPrefix);
    }

    private String proofOfWork(int difficulty, Function<Integer, Boolean> interruptCondition) {
        String hashPrefix = StringUtil.dup("0", difficulty);
        String h = computeHash();
        while (!h.substring(0, difficulty).equals(hashPrefix)) {
            if (interruptCondition.apply(index)) {
                return null;
            }
            setNonce(getNonce() + 1);
            h = computeHash();
        }
        return h;
    }

    private BlockHeader getBlockHeader() {
        return new BlockHeader()
                .setVersion(getVersion())
                .setHashPrevBlock(getPrevBlockHash())
                .setHashMerkleRoot(getMerkleRootHash())
                .setTime(getTime())
                .setBits(getBits())
                .setNonce(getNonce());
    }

    @Override
    public String toString() {
        return JsonUtil.toPrettyJson(this);
    }
}

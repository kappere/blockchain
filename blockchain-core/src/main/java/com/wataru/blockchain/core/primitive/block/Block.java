package com.wataru.blockchain.core.primitive.block;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wataru.blockchain.core.primitive.ByteBlob;
import com.wataru.blockchain.core.primitive.serialize.ByteArraySerializer;
import com.wataru.blockchain.core.primitive.serialize.ByteSerializable;
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
public class Block implements ByteSerializable {
    /**
     * 版本
     */
    private int version;

    /**
     * 前一个区块的Hash值
     */
    @JsonSerialize(using = ByteArraySerializer.ByteArrayToHexSerializer.class)
    private ByteBlob.Byte256 prevBlockHash;

    /**
     * 包含交易信息的Merkle树根
     */
    @JsonSerialize(using = ByteArraySerializer.ByteArrayToHexSerializer.class)
    private ByteBlob.Byte256 merkleRootHash;

    /**
     * 区块产生的时间
     */
    private long time;

    /**
     * 工作量证明(POW)的难度
     */
    private int difficulty;

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
    @JsonSerialize(using = ByteArraySerializer.ByteArrayToHexSerializer.class)
    private ByteBlob.Byte256 hash;

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
        this.setMerkleRootHash(new ByteBlob.Byte256());
        this.setTime(time);
        this.transactions = new ArrayList<>();
    }

    /**
     * 计算block的hash值
     */
    public ByteBlob.Byte256 computeHash() {
        String hashStr = String.format("%d,%d,%d,%s,%s", index, getTime(), getNonce(), getPrevBlockHash(), JsonUtil.toJson(transactions));
        return EncodeUtil.sha256(hashStr.getBytes());
    }

    public boolean mine(int difficulty, Function<Integer, Boolean> interruptCondition) {
        log.info("Mining block {}", index);
        ByteBlob.Byte256 proof = proofOfWork(difficulty, interruptCondition);
        if (proof == null) {
            return false;
        }
        hash = proof;
        log.info("Mined block {}, proof: {}, block:\n{}", index, proof.toHex(), JsonUtil.toPrettyJson(this));
        return true;
    }

    public boolean validatePoofOfWork(int difficulty) {
        ByteBlob.Byte256 h = computeHash();
        return checkHashPrefix(h, difficulty);
    }

    private boolean checkHashPrefix(ByteBlob.Byte256 hash, int difficulty) {
        String hashPrefix = StringUtil.dup("0", difficulty);
        return hash.toHex().startsWith(hashPrefix);
    }

    private ByteBlob.Byte256 proofOfWork(int difficulty, Function<Integer, Boolean> interruptCondition) {
        ByteBlob.Byte256 h = computeHash();
        while (!checkHashPrefix(h, difficulty)) {
            if (interruptCondition.apply(index)) {
                return null;
            }
            setNonce(getNonce() + 1);
            h = computeHash();
        }
        return h;
    }

    @Override
    public String toString() {
        return JsonUtil.toPrettyJson(this);
    }

    @Override
    public byte[] serialize() {
        return new ByteArraySerializer.Builder()
                .push(this.version)
                .push(this.prevBlockHash, false)
                .push(this.merkleRootHash, false)
                .push(this.time)
                .push(this.difficulty)
                .push(this.nonce)
                .push(this.index)
                .push(this.hash, false)
                .push(4, this.transactions)
                .build(ByteArraySerializer::new).getData();
    }

    @Override
    public int deserialize(byte[] data) {
        return new ByteArraySerializer.Extractor(data)
                .pullInt(var -> this.version = var)
                .pullObject(ByteBlob.Byte256::new, var -> this.prevBlockHash = var)
                .pullObject(ByteBlob.Byte256::new, var -> this.merkleRootHash = var)
                .pullLong(var -> this.time = var)
                .pullInt(var -> this.difficulty = var)
                .pullInt(var -> this.nonce = var)
                .pullInt(var -> this.index = var)
                .pullObject(ByteBlob.Byte256::new, var -> this.hash = var)
                .pullList(4, Transaction::new, var -> this.transactions = var)
                .complete();
    }
}

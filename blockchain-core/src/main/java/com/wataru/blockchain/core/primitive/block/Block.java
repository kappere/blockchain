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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        String hashStr = String.format("%d,%d,%d,%s,%s", index, getTime(), getNonce(), getPrevBlockHash(), getMerkleRootHash());
        return EncodeUtil.sha256(hashStr.getBytes());
    }

    public ByteBlob.Byte256 computeMerkleRootHash() {
        List<ByteBlob.Byte256> transationHashList = transactions.stream().map(Transaction::getHash).collect(Collectors.toList());
        return merkleTree(transationHashList);
    }

    private ByteBlob.Byte256 merkleTree(List<ByteBlob.Byte256> hashList) {
        if (hashList.size() == 1) {
            return hashList.get(0);
        }
        List<ByteBlob.Byte256> newHashList = new ArrayList<>();
        for (int i = 0; i < hashList.size() - 1; i += 2) {
            ByteBlob.Byte256 rootHash = merkleHash(hashList.get(i), hashList.get(i + 1));
            newHashList.add(rootHash);
        }
        if (hashList.size() % 2 == 1) {
            ByteBlob.Byte256 h = hashList.get(hashList.size() - 1);
            ByteBlob.Byte256 rootHash = merkleHash(h, h);
            newHashList.add(rootHash);
        }
        return merkleTree(newHashList);
    }

    private ByteBlob.Byte256 merkleHash(ByteBlob.Byte256 hash1, ByteBlob.Byte256 hash2) {
        ByteBlob.ByteVar hash0 = new ByteBlob.ByteVar(hash1.toLittleEndianNumberHex() + hash2.toLittleEndianNumberHex());
        return EncodeUtil.sha256(EncodeUtil.sha256(hash0.getData()).getData());
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

    public static void main(String[] args) {
        ByteBlob.Byte256 s1 = new ByteBlob.Byte256("8347cee4a1cb5ad1bb0d92e86e6612dbf6cfc7649c9964f210d4069b426e720a");
        ByteBlob.Byte256 s2 = new ByteBlob.Byte256("a16f3ce4dd5deb92d98ef5cf8afeaf0775ebca408f708b2146c4fb42b41e14be");
        System.out.println(new Block().merkleTree(Arrays.asList(
                s1, s2
        )));
    }
}

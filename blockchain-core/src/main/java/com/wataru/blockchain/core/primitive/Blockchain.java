package com.wataru.blockchain.core.primitive;

import com.wataru.blockchain.core.exception.BizException;
import com.wataru.blockchain.core.primitive.block.Block;
import com.wataru.blockchain.core.primitive.crypto.Base58Check;
import com.wataru.blockchain.core.primitive.serialize.ByteArraySerializer;
import com.wataru.blockchain.core.primitive.serialize.ByteSerializable;
import com.wataru.blockchain.core.primitive.transaction.Transaction;
import com.wataru.blockchain.core.primitive.transaction.Utxo;
import com.wataru.blockchain.core.util.EncodeUtil;
import com.wataru.blockchain.core.util.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.util.ByteUtils;

import java.io.*;
import java.security.PublicKey;
import java.util.*;

/**
 * 区块链
 */
@Data
@Slf4j
public class Blockchain implements ByteSerializable {
    private String name;
    private List<Block> chain;
    private int difficulty = 2;
    public static Blockchain instance;
    private Utxo utxo;

    /**
     * 未确认交易
     */
    private List<Transaction> unconfirmedTransactions;

    public Blockchain() {
    }

    public Blockchain(String name) {
        instance = this;
        this.name = name;
        utxo = new Utxo();
        chain = new ArrayList<>();
        unconfirmedTransactions = new ArrayList<>();
        Block genesisBlock = createGenesisBlock();
        chain.add(genesisBlock);
    }

    public Block createGenesisBlock() {
        Block genesisBlock = new Block(0, System.currentTimeMillis());
        ByteBlob.Byte160 genesisAddress = addressToPublicKeyHash("14XkoruvE49hZtSVeLRAauza35F6VHS7Nv");
        List<Transaction.TransactionOutput> outputs = new ArrayList<>();
        // 转账到交易目标账户
        outputs.add(new Transaction.TransactionOutput(10000000000000L, LockScript.formatLockScript(genesisAddress)));
        Transaction transaction = new Transaction(Collections.emptyList(), outputs);
        transaction.processTransaction();
        clearUnconfirmedTransaction();
        genesisBlock.getTransactions().add(transaction);
        genesisBlock.setPrevBlockHash(new ByteBlob.Byte256());
        genesisBlock.setMerkleRootHash(genesisBlock.computeMerkleRootHash());
        genesisBlock.setHash(genesisBlock.computeHash());
        return genesisBlock;
    }

    public Block latestBlock() {
        return chain.get(chain.size() - 1);
    }

    public Transaction getTransactionInUnconfirmed(ByteBlob.Byte256 transactionHash) {
        for (Transaction unconfirmedTransaction : unconfirmedTransactions) {
            if (unconfirmedTransaction.getHash().equals(transactionHash)) {
                return unconfirmedTransaction;
            }
        }
        return null;
    }

    public boolean isTransactionInUnconfirmed(Transaction transaction) {
        return unconfirmedTransactions.stream().anyMatch(item -> item.getHash().equals(transaction.getHash()));
    }

    public boolean isTransactionInChain(Transaction transaction) {
        return getTransactionOutput(transaction.getHash(), 0) != null;
    }

    public boolean isTransactionProcessed(Transaction transaction) {
        if (isTransactionInUnconfirmed(transaction)) {
            return true;
        }
        if (isTransactionInChain(transaction)) {
            return true;
        }
        return false;
    }

    public boolean addNewTransaction(Transaction transaction) {
        log.info("New transaction:\n{}", JsonUtil.toPrettyJson(transaction));
        unconfirmedTransactions.add(transaction);
        return true;
    }

    public void clearUnconfirmedTransaction() {
        unconfirmedTransactions.clear();
    }

    public void removeUnconfirmedTransaction(List<Transaction> transactions) {
        unconfirmedTransactions.removeIf(transaction -> transactions.stream().anyMatch(item -> item.getHash().equals(transaction.getHash())));
    }

    /**
     * 获取out，当UTXO中已经移除该out时，需要查找整条链
     */
    public Transaction.TransactionOutput getTransactionOutput(ByteBlob.Byte256 transactionHash, int vout) {
        for (Block block : chain) {
            for (Transaction transaction : block.getTransactions()) {
                if (transaction.getHash().equals(transactionHash)) {
                    return transaction.getOutputs().get(vout);
                }
            }
        }
        return null;
    }

    public synchronized void replaceChain(Blockchain blockchain) {
        List<Transaction> toRemoveTransactions = new ArrayList<>();
        for (Block block : blockchain.getChain()) {
            for (Transaction transaction : block.getTransactions()) {
                this.unconfirmedTransactions.stream()
                        .filter(item -> item.getHash().equals(transaction.getHash()))
                        .findAny()
                        .ifPresent(toRemoveTransactions::add);
            }
        }
        removeUnconfirmedTransaction(toRemoveTransactions);
        chain = blockchain.getChain();
        difficulty = blockchain.getDifficulty();
        utxo = blockchain.getUtxo();
    }

    public synchronized boolean addBlock(Block block) {
        if (!checkBlockValid(block, latestBlock())) {
            return false;
        }
        chain.add(block);
        removeUnconfirmedTransaction(block.getTransactions());
        // 更新UTXO
        for (Transaction transaction : block.getTransactions()) {
            Blockchain.instance.getUtxo().updateByTransaction(transaction);
        }
//        unconfirmedTransactions.removeIf(transaction -> block.getTransactions().stream().anyMatch(item -> item.getUuid().equals(transaction.getUuid())));
        return true;
    }

    /**
     * 校验区块工作量证明
     */
    private boolean validatePoofOfWork(Block block) {
        return block.validatePoofOfWork(difficulty);
    }

    /**
     * 校验区块金额
     */
    private boolean validateTotalValueOfBlock(Block block) {
        if (block.getTransactions().isEmpty()) {
            return false;
        }
        long total = block.getTransactions().get(0).getOutputsValue();
        long fee = 0L;
        boolean isFeeTransaction = true;
        for (Transaction transaction : block.getTransactions()) {
            if (isTransactionInChain(transaction)) {
                return false;
            } else if (isTransactionInUnconfirmed(transaction)) {
                for (Transaction unconfirmedTransaction : unconfirmedTransactions) {
                    if (unconfirmedTransaction.getHash().equals(transaction.getHash())) {
                        if (!unconfirmedTransaction.equals(transaction)) {
                            return false;
                        }
                    }
                }
            } else {
                if (!isFeeTransaction) {
                    transaction.processTransaction();
                }
            }
            if (!isFeeTransaction) {
                fee += transaction.getInputsValue() - transaction.getOutputsValue();
            }
            isFeeTransaction = false;
        }
        return total == fee;
    }

    private boolean checkBlockValid(Block block, Block previousBlock) {
        for (Transaction transaction : block.getTransactions()) {
            if (!transaction.getHash().equals(transaction.computeHash())) {
                return false;
            }
        }
        if (!block.getMerkleRootHash().equals(block.computeMerkleRootHash())) {
            return false;
        }
        if (!block.getHash().equals(block.computeHash())) {
            return false;
        }
        if (previousBlock != null) {
            if (!block.getPrevBlockHash().equals(previousBlock.getHash())) {
                return false;
            }
            if (block.getIndex() != previousBlock.getIndex() + 1) {
                return false;
            }
        }
        if (!validatePoofOfWork(block)) {
            return false;
        }
        if (!validateTotalValueOfBlock(block)) {
            return false;
        }
        return true;
    }

    public boolean checkChainValid() {
        Block previousBlock = null;
        for (Block block : chain) {
            block.getTransactions().forEach(transaction -> transaction.setHash(transaction.computeHash()));
            block.setMerkleRootHash(block.computeMerkleRootHash());
            block.setHash(block.computeHash());
            if (!checkBlockValid(block, previousBlock)) {
                return false;
            }
            previousBlock = block;
        }
        return true;
    }

    @Override
    public String toString() {
        return JsonUtil.toPrettyJson(this);
    }

    public static String publicKeyToAddress(PublicKey publicKey) {
        ByteBlob.Byte160 publicKeyHash = EncodeUtil.hash160(new ByteBlob.Byte256(publicKey.getEncoded()));
        return Base58Check.bytesToBase58(ByteUtils.concat(VersionPrefix.BITCOIN_ADDRESS, publicKeyHash.getData()));
    }

    public static ByteBlob.Byte160 addressToPublicKeyHash(String address) {
        byte[] bytes = Base58Check.base58ToBytes(address);
        for (int i = 0; i < VersionPrefix.BITCOIN_ADDRESS.length; i++) {
            if (bytes[i] != VersionPrefix.BITCOIN_ADDRESS[i]) {
                throw new BizException("地址转码失败");
            }
        }
        return new ByteBlob.Byte160(Arrays.copyOfRange(bytes, VersionPrefix.BITCOIN_ADDRESS.length, bytes.length));
    }

    public List<Transaction> collectUnconfirmedTransactions(int size) {
        // 收集未确认交易
        // TODO 打包的交易的utxo必须已确认
        if (unconfirmedTransactions.size() < size) {
            return null;
        }
        return new ArrayList<>(unconfirmedTransactions.subList(0, size));
    }

    public void store() throws IOException {
        File file = new File(name + ".dat");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(this.serialize());
        }
        log.info("Blockchain saved in file {}.", name + ".dat");
    }

    public void restore() throws IOException {
        File file = new File(name + ".dat");
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int n;
                while (-1 != (n = fis.read(buffer))) {
                    baos.write(buffer, 0, n);
                }
                this.deserialize(baos.toByteArray());
                for (Block block : this.chain) {
                    for (Transaction transaction : block.getTransactions()) {
                        transaction.setHash(transaction.computeHash());
                    }
                    block.setMerkleRootHash(block.computeMerkleRootHash());
                    block.setHash(block.computeHash());
                }
            }
            log.info("Blockchain restored from file {}.", name + ".dat");
        }
    }

    @Override
    public byte[] serialize() {
        return new ByteArraySerializer.Builder()
                .push(this.difficulty)
                .push(this.utxo, false)
                .push(4, this.chain)
                .push(4, this.unconfirmedTransactions)
                .build(ByteArraySerializer::new).getData();
    }

    @Override
    public int deserialize(byte[] data) {
        return new ByteArraySerializer.Extractor(data)
                .pullInt(var -> this.difficulty = var)
                .pullObject(Utxo::new, var -> this.utxo = var)
                .pullList(4, Block::new, var -> this.chain = var)
                .pullList(4, Transaction::new, var -> this.unconfirmedTransactions = var)
                .complete();
    }
}

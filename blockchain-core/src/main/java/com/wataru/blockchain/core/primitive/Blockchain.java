package com.wataru.blockchain.core.primitive;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wataru.blockchain.core.exception.BizException;
import com.wataru.blockchain.core.primitive.block.Block;
import com.wataru.blockchain.core.primitive.crypto.Base58Check;
import com.wataru.blockchain.core.primitive.crypto.Secp256k1;
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
public class Blockchain {
    private List<Block> chain;
    private int difficulty = 2;
    public static Blockchain instance;
    private Utxo utxo;

    /**
     * 未确认交易
     */
    @JsonIgnore
    private List<Transaction> unconfirmedTransactions;

    public Blockchain() {
    }

    public Blockchain(int s) {
        instance = this;
        utxo = new Utxo();
        chain = new ArrayList<>();
        unconfirmedTransactions = new ArrayList<>();
        Block genesisBlock = createGenesisBlock();
        chain.add(genesisBlock);
    }

    public Block createGenesisBlock() {
        Block genesisBlock = new Block(0, System.currentTimeMillis());
        String genesisAddress = EncodeUtil.bytesToHexString(addressToPublicKeyHash("1Dvi7Rah81zm1UpSSKyQ8sAmfDmi2WHcb8"));
        List<Transaction.TransactionOutput> outputs = new ArrayList<>();
        // 转账到交易目标账户
        outputs.add(new Transaction.TransactionOutput(10000000000000L, LockScript.formatLockScript(genesisAddress)));
        Transaction transaction = new Transaction(Collections.emptyList(), outputs);
        transaction.processTransaction();
        clearUnconfirmedTransaction();
        genesisBlock.getTransactions().add(transaction);
        genesisBlock.setPrevBlockHash("0");
        genesisBlock.setHash(genesisBlock.computeHash());
        return genesisBlock;
    }

    public Block latestBlock() {
        return chain.get(chain.size() - 1);
    }

    public Transaction getTransactionInUnconfirmed(String transactionId) {
        for (Transaction unconfirmedTransaction : unconfirmedTransactions) {
            if (unconfirmedTransaction.getTransactionId().equals(transactionId)) {
                return unconfirmedTransaction;
            }
        }
        return null;
    }

    public boolean isTransactionInUnconfirmed(Transaction transaction) {
        return unconfirmedTransactions.stream().anyMatch(item -> item.getTransactionId().equals(transaction.getTransactionId()));
    }

    public boolean isTransactionInChain(Transaction transaction) {
        return getTransactionOutput(transaction.getTransactionId(), 0) != null;
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
        unconfirmedTransactions.removeIf(transaction -> transactions.stream().anyMatch(item -> item.getTransactionId().equals(transaction.getTransactionId())));
    }

    /**
     * 获取out，当UTXO中已经移除该out时，需要查找整条链
     */
    public Transaction.TransactionOutput getTransactionOutput(String transactionId, int vout) {
        for (Block block : chain) {
            for (Transaction transaction : block.getTransactions()) {
                if (transaction.getTransactionId().equals(transactionId)) {
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
                        .filter(item -> item.getTransactionId().equals(transaction.getTransactionId()))
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
        if (!latestBlock().getHash().equals(block.getPrevBlockHash())) {
            return false;
        }
        if (!validatePoofOfWork(block)) {
            return false;
        }
        if (!validateTotalValueOfBlock(block)) {
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
                    if (unconfirmedTransaction.getTransactionId().equals(transaction.getTransactionId())) {
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

    public boolean checkChainValid() {
        Block previousBlock = null;
        for (Block block : chain) {
            if (!block.getHash().equals(block.computeHash())) {
                return false;
            }
            if (previousBlock != null && !block.getPrevBlockHash().equals(previousBlock.getHash())) {
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
        byte[] publicKeyHash = EncodeUtil.hash160(publicKey.getEncoded());
        return Base58Check.bytesToBase58(ByteUtils.concat(VersionPrefix.BITCOIN_ADDRESS, publicKeyHash));
    }

    public static byte[] addressToPublicKeyHash(String address) {
        byte[] bytes = Base58Check.base58ToBytes(address);
        for (int i = 0; i < VersionPrefix.BITCOIN_ADDRESS.length; i++) {
            if (bytes[i] != VersionPrefix.BITCOIN_ADDRESS[i]) {
                throw new BizException("地址转码失败");
            }
        }
        return Arrays.copyOfRange(bytes, VersionPrefix.BITCOIN_ADDRESS.length, bytes.length);
    }

    public List<Transaction> collectUnconfirmedTransactions(int size) {
        // 收集未确认交易
        if (unconfirmedTransactions.size() < size) {
            return null;
        }
        return new ArrayList<>(unconfirmedTransactions.subList(0, size));
    }

    public void store(String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(JsonUtil.serialize(this));
        }
    }

    public void restore(String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int n;
                while (-1 != (n = fis.read(buffer))) {
                    baos.write(buffer, 0, n);
                }
            }
        }
    }
}

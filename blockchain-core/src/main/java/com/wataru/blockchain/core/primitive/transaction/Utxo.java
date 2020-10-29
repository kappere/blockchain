package com.wataru.blockchain.core.primitive.transaction;

import com.wataru.blockchain.core.primitive.ByteBlob;
import com.wataru.blockchain.core.primitive.LockScript;
import com.wataru.blockchain.core.primitive.Script;
import com.wataru.blockchain.core.primitive.crypto.Secp256k1;
import com.wataru.blockchain.core.primitive.serialize.ByteArraySerializer;
import com.wataru.blockchain.core.primitive.serialize.ByteSerializable;
import lombok.Data;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Utxo implements ByteSerializable {
    /**
     * 保存UTXO
     * transactionHash --- vout --- output
     */
    private Map<ByteBlob.Byte256, Map<Integer, Transaction.TransactionOutput>> utxoMap = new HashMap<>();

    public Utxo() {}

    @Override
    public byte[] serialize() {
        return new ByteArraySerializer.Builder()
                .push(utxoMap)
                .build(ByteArraySerializer::new)
                .getData();
    }

    @Override
    public int deserialize(byte[] data) {
        utxoMap = new HashMap<>();
        AtomicInteger length = new AtomicInteger();
        return new ByteArraySerializer.Extractor(data)
                .pullInt(length::set)
                .forEach(length.get(), extractor -> {
                    AtomicReference<ByteBlob.Byte256> key = new AtomicReference<>();
                    Map<Integer, Transaction.TransactionOutput> tmp = new HashMap<>();
                    AtomicInteger tmpLen = new AtomicInteger();
                    extractor.pullObject(ByteBlob.Byte256::new, key::set)
                            .pullInt(tmpLen::set)
                            .forEach(tmpLen.get(), extractor1 -> {
                                AtomicReference<Integer> key1 = new AtomicReference<>();
                                extractor1.pullInt(key1::set)
                                        .pullObject(Transaction.TransactionOutput::new, var -> tmp.put(key1.get(), var));
                            });
                    utxoMap.put(key.get(), tmp);
                }).complete();
    }

    @Data
    public static class PersonalUtxo {
        private Transaction.OutPoint prevout;
        private Transaction.TransactionOutput output;
        public PersonalUtxo(ByteBlob.Byte256 transactionHash, int vout, Transaction.TransactionOutput output) {
            this.prevout = new Transaction.OutPoint(transactionHash, vout);
            this.output = output;
        }
    }

    public Transaction.TransactionOutput getTransactionOutput(Transaction.OutPoint prevout) {
        return utxoMap.getOrDefault(prevout.getHash(), Collections.emptyMap()).get(prevout.getVout());
    }

    public void addUtxo(ByteBlob.Byte256 transactionHash, List<Transaction.TransactionOutput> outputs) {
        for (int i = 0; i < outputs.size(); i++) {
            utxoMap.computeIfAbsent(transactionHash, key -> new HashMap<>()).put(i, outputs.get(i));
        }
    }

    public void removeUtxo(Transaction.OutPoint prevout) {
        Map<Integer, Transaction.TransactionOutput> transactionOutputsMap = utxoMap.get(prevout.getHash());
        if (transactionOutputsMap != null) {
            transactionOutputsMap.remove(prevout.getVout());
            if (transactionOutputsMap.isEmpty()) {
                utxoMap.remove(prevout.getHash());
            }
        }
    }

    public List<PersonalUtxo> getTransactionByKey(PrivateKey privateKey, PublicKey publicKey) {
        ByteBlob.Byte256 testSignData = new ByteBlob.Byte256(new byte[]{1,2,3});
        Script scriptSig = LockScript.formatUnlockScript(
                Secp256k1.sign(testSignData, privateKey).concat((byte) 0x01),
                new ByteBlob.ByteVar(publicKey.getEncoded()));
        List<PersonalUtxo> result = new ArrayList<>();
        utxoMap.forEach((transactionHash, outputs) -> {
            outputs.forEach((vout, out) -> {
                boolean unlocked = new LockScript().unlock(scriptSig, out.getScriptPubKey(), testSignData);
                if (unlocked) {
                    result.add(new PersonalUtxo(transactionHash, vout, out));
                }
            });
        });
        return result;
    }

    public void updateByTransaction(Transaction transaction) {
        for (Transaction.TransactionInput input : transaction.getInputs()) {
            removeUtxo(input.getPrevout());
        }
        addUtxo(transaction.getHash(), transaction.getOutputs());
    }
}

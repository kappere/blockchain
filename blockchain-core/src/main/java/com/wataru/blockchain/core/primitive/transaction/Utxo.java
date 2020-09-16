package com.wataru.blockchain.core.primitive.transaction;

import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.primitive.LockScript;
import com.wataru.blockchain.core.primitive.crypto.Secp256k1;
import com.wataru.blockchain.core.util.EncodeUtil;
import lombok.Data;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

public class Utxo {
    /**
     * 保存UTXO
     * transactionId --- vout --- output
     */
    private Map<String, Map<Integer, Transaction.TransactionOutput>> utxoMap = new HashMap<>();

    public Utxo() {}

    @Data
    public static class PersonalUtxo {
        private String transactionId;
        private int vout;
        private Transaction.TransactionOutput output;
        public PersonalUtxo(String transactionId, int vout, Transaction.TransactionOutput output) {
            this.transactionId = transactionId;
            this.vout = vout;
            this.output = output;
        }
    }

    public Transaction.TransactionOutput getTransactionOutput(String transactionId, int vout) {
        return utxoMap.getOrDefault(transactionId, Collections.emptyMap()).get(vout);
    }

    public void addUtxo(String transactionId, List<Transaction.TransactionOutput> outputs) {
        for (int i = 0; i < outputs.size(); i++) {
            utxoMap.computeIfAbsent(transactionId, key -> new HashMap<>()).put(i, outputs.get(i));
        }
    }

    public void removeUtxo(String transactionId, int vout) {
        Map<Integer, Transaction.TransactionOutput> transactionOutputsMap = utxoMap.get(transactionId);
        if (transactionOutputsMap != null) {
            transactionOutputsMap.remove(vout);
            if (transactionOutputsMap.isEmpty()) {
                utxoMap.remove(transactionId);
            }
        }
    }

    public List<PersonalUtxo> getTransactionByKey(PrivateKey privateKey, PublicKey publicKey) {
        byte[] testSignData = new byte[]{1,2,3};
        String scriptSig = String.format("%s %s", EncodeUtil.bytesToHexString(Secp256k1.sign(testSignData, privateKey)), EncodeUtil.bytesToHexString(publicKey.getEncoded()));
        List<PersonalUtxo> result = new ArrayList<>();
        utxoMap.forEach((transactionId, outputs) -> {
            outputs.forEach((vout, out) -> {
                boolean unlocked = new LockScript().unlock(scriptSig, out.getScriptPubKey(), testSignData);
                if (unlocked) {
                    result.add(new PersonalUtxo(transactionId, vout, out));
                }
            });
        });
        return result;
    }

    public void updateByTransaction(Transaction transaction) {
        for (Transaction.TransactionInput input : transaction.getInputs()) {
            removeUtxo(input.getTransactionId(), input.getVout());
        }
        addUtxo(transaction.getTransactionId(), transaction.getOutputs());
    }
}

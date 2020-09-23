package com.wataru.blockchain.core.primitive.wallet;

import com.wataru.blockchain.core.exception.BizException;
import com.wataru.blockchain.core.net.Notifier;
import com.wataru.blockchain.core.net.packet.payload.TransactionPayload;
import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.primitive.ByteBlob;
import com.wataru.blockchain.core.primitive.LockScript;
import com.wataru.blockchain.core.primitive.VersionPrefix;
import com.wataru.blockchain.core.primitive.crypto.Base58Check;
import com.wataru.blockchain.core.primitive.crypto.Secp256k1;
import com.wataru.blockchain.core.primitive.transaction.Transaction;
import com.wataru.blockchain.core.primitive.transaction.Utxo;
import com.wataru.blockchain.core.util.EncodeUtil;
import com.wataru.blockchain.core.util.JsonUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.redis.util.ByteUtils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SingleWallet extends Wallet {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String name;
    private List<Utxo.PersonalUtxo> utxos;
    private static final long FEE = 7L;

    public SingleWallet(String name) {
        KeyPair keyPair = Secp256k1.genKeyPair(name.getBytes());
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        this.name = name;
    }

    public void refreshUtxos() {
        Utxo utxo = Blockchain.instance.getUtxo();
        this.utxos = utxo.getTransactionByKey(privateKey, publicKey);
    }

    public long getBalance() {
        return utxos.stream()
                .map(Utxo.PersonalUtxo::getOutput)
                .map(Transaction.TransactionOutput::getValue)
                .reduce(Long::sum)
                .orElse(0L);
    }

    public Transaction createTransaction(String address, long value) {
        ByteBlob.Byte160 publicKeyHash = Blockchain.addressToPublicKeyHash(address);
        refreshUtxos();
        long balance = getBalance();
        if (balance < value) {
            throw new BizException("余额不足");
        }
        if (value <= 0) {
            throw new BizException("转账金额错误");
        }
        List<Transaction.TransactionInput> inputs = new ArrayList<>();
        long total = 0;
        for (int i = 0; i < utxos.size(); i++) {
            Utxo.PersonalUtxo personalUtxo = utxos.get(i);
            total += personalUtxo.getOutput().getValue();
            inputs.add(new Transaction.TransactionInput(
                    personalUtxo.getTransactionId(),
                    personalUtxo.getVout(),
                    null,
                    0));
            if (total >= value) {
                long leftOver = total - value;
                List<Transaction.TransactionOutput> outputs = new ArrayList<>();
                // 转账到交易目标账户
                outputs.add(new Transaction.TransactionOutput(value, LockScript.formatLockScript(publicKeyHash)));
                Transaction transaction = new Transaction(inputs, outputs);
                ByteBlob.Byte256 toSignData = transaction.getToSignData(null);
                for (Transaction.TransactionInput input : transaction.getInputs()) {
                    input.setScriptSig(LockScript.formatUnlockScript(
                            Secp256k1.sign(toSignData, privateKey).concat((byte) 0x01),
                            new ByteBlob.ByteVar(publicKey.getEncoded())
                    ));
                }
                byte[] payload = JsonUtil.toJson(transaction).getBytes();
                long fee = payload.length / 200;
                if (i == utxos.size() - 1) {
                    fee = Math.min(fee, leftOver);
                }
                if (leftOver < fee) {
                    continue;
                }
                leftOver = leftOver - fee;
                // 结余
                if (leftOver > 0L) {
                    outputs.add(new Transaction.TransactionOutput(
                            leftOver,
                            LockScript.formatLockScript(EncodeUtil.hash160(new ByteBlob.Byte256(publicKey.getEncoded())))));
                }
                // 重新计算解锁脚本
                toSignData = transaction.getToSignData(null);
                for (Transaction.TransactionInput input : transaction.getInputs()) {
                    input.setScriptSig(LockScript.formatUnlockScript(
                            Secp256k1.sign(toSignData, privateKey).concat((byte) 0x01),
                            new ByteBlob.ByteVar(publicKey.getEncoded())
                    ));
                }
                Notifier.broadcast(new TransactionPayload(transaction));
                return transaction;
            }
        }

        return null;
    }

    public static void main(String[] args) {
        System.out.println(JsonUtil.toPrettyJson(new SingleWallet("3333")));
    }
}

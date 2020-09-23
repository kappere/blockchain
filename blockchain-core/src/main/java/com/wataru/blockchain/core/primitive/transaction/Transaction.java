package com.wataru.blockchain.core.primitive.transaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.primitive.ByteBlob;
import com.wataru.blockchain.core.primitive.LockScript;
import com.wataru.blockchain.core.primitive.Script;
import com.wataru.blockchain.core.primitive.serialize.ByteSerializable;
import com.wataru.blockchain.core.primitive.serialize.ByteArraySerializer;
import com.wataru.blockchain.core.util.EncodeUtil;
import com.wataru.blockchain.core.util.JsonUtil;
import lombok.Data;
import org.springframework.data.redis.util.ByteUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Transaction implements ByteSerializable {
    private int version;
    private int locktime;
    /**
     * 交易ID
     */
    @JsonSerialize(using = ByteArraySerializer.ByteArrayToHexSerializer.class)
    private ByteBlob.Byte256 transactionId;
    /**
     * 交易输入
     */
    private List<TransactionInput> inputs;
    /**
     * 交易输出
     */
    private List<TransactionOutput> outputs;

    public Transaction() {}

    public Transaction(List<TransactionInput> inputs, List<TransactionOutput> outputs) {
        this.transactionId = new ByteBlob.Byte256(UUID.randomUUID().toString().replace("-", ""));
        this.version = 1;
        this.locktime = 0xFFFFFFFF;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    /**
     * 计算交易hash
     */
    public ByteBlob.Byte256 hash() {
        return getToSignData(null);
    }

    /**
     * 获取待签名/验签的数据
     */
    public ByteBlob.Byte256 getToSignData(Map<TransactionInput, TransactionOutput> srcUtxoMap) {
        if (srcUtxoMap == null) {
            srcUtxoMap = new HashMap<>();
            // 获取input引用的输出
            Utxo utxo = Blockchain.instance.getUtxo();
            for (TransactionInput input : inputs) {
                TransactionOutput src = utxo.getTransactionOutput(input.getTransactionId(), input.getVout());
                srcUtxoMap.put(input, src);
            }
        }
        Map<TransactionInput, TransactionOutput> finalSrcUtxoMap = srcUtxoMap;
        for (TransactionInput input : inputs) {
            if (!finalSrcUtxoMap.containsKey(input)) {
                return null;
            }
        }
        return EncodeUtil.sha256(ByteUtils.concatAll(
                JsonUtil.toJson(inputs.stream()
                        .map(TransactionInput::new)
                        .peek(item -> item.setScriptSig(finalSrcUtxoMap.get(item).getScriptPubKey()))
                        .collect(Collectors.toList())).getBytes(),
                JsonUtil.toJson(outputs).getBytes()));
    }

    /**
     * 根据input获取源output
     */
    private Map<TransactionInput, TransactionOutput> getInputSource(List<TransactionInput> inputs, boolean onlyUtxo) {
        Utxo utxo = Blockchain.instance.getUtxo();
        Map<TransactionInput, TransactionOutput> srcUtxoMap = new HashMap<>();
        // 获取input引用的输出
        for (TransactionInput input : inputs) {
            TransactionOutput src = utxo.getTransactionOutput(input.getTransactionId(), input.getVout());
            if (!onlyUtxo && src == null) {
                Transaction unconfirmed = Blockchain.instance.getTransactionInUnconfirmed(input.getTransactionId());
                if (unconfirmed != null) {
                    src = unconfirmed.getOutputs().get(input.getVout());
                } else {
                    src = Blockchain.instance.getTransactionOutput(input.getTransactionId(), input.getVout());
                }
            }
            srcUtxoMap.put(input, src);
        }
        return srcUtxoMap;
    }

    /**
     * 校验交易合法性
     */
    public boolean validate() {
//        if (!verify()) {
//            return false;
//        }
        Map<TransactionInput, TransactionOutput> srcUtxoMap = getInputSource(inputs, true);
        long inputsValue = getInputsValue();
        long fee = 0;
        if (!Blockchain.instance.getChain().isEmpty()) {
            ByteBlob.Byte256 toSignData = getToSignData(srcUtxoMap);
            if (toSignData == null) {
                return false;
            }
            // 校验交易金额
            if (inputsValue <= 0L) {
                return false;
            }
            // 校验解锁脚本
            for (TransactionInput input : inputs) {
                boolean unlocked = new LockScript().unlock(input.getScriptSig(), srcUtxoMap.get(input).getScriptPubKey(), toSignData);
                if (!unlocked) {
                    return false;
                }
            }
            // 手续费
            fee = inputsValue - getOutputsValue();
            if (fee < 0) {
                return false;
            }
        } else {
        }
        return true;
    }

    /**
     * 处理交易，返回手续费
     */
    public boolean processTransaction() {
        if (!validate()) {
            return false;
        }
        if (Blockchain.instance.isTransactionProcessed(this)) {
            return false;
        }
        // 添加到未确认交易
        if (!Blockchain.instance.addNewTransaction(this)) {
            return false;
        }
        // 更新UTXO
        Blockchain.instance.getUtxo().updateByTransaction(this);
        return true;
    }

    /**
     * 获取交易输入金额
     * 数据来源UTXO
     */
    public long getInputsValue() {
        Map<TransactionInput, TransactionOutput> srcOutMap = getInputSource(inputs, false);
        long total = 0L;
        for (TransactionInput input : inputs) {
            total += srcOutMap.get(input).value;
        }
        return total;
    }

    public long getOutputsValue() {
        long total = 0L;
        for (TransactionOutput output : outputs) {
            total += output.value;
        }
        return total;
    }

    @Override
    public byte[] serialize() {
        return new ByteArraySerializer.Builder()
                .push(this.version)
                .push(transactionId, false)
                .push(this.inputs, true)
                .push(this.outputs, true)
                .push(this.locktime)
                .build(ByteArraySerializer::new).getData();
    }

    @Override
    public int deserialize(byte[] data) {
        return new ByteArraySerializer.Extractor(data)
                .pullInt(var -> this.version = var)
                .pullObject(ByteBlob.Byte256::new, var -> this.transactionId = var)
                .pullListWithSize(TransactionInput::new, var -> this.inputs = var)
                .pullListWithSize(TransactionOutput::new, var -> this.outputs = var)
                .pullInt(var -> this.locktime = var)
                .complete();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "version=" + version +
                ", locktime=" + locktime +
                ", transactionId='" + transactionId + '\'' +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Transaction that = (Transaction) o;
        return version == that.version &&
                locktime == that.locktime &&
                Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(inputs, that.inputs) &&
                Objects.equals(outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, locktime, transactionId, inputs, outputs);
    }

    @Data
    public static class TransactionInput implements ByteSerializable {
        /**
         * 交易ID，引用包含正在使用的UTXO的交易
         */
        @JsonSerialize(using = ByteArraySerializer.ByteArrayToHexSerializer.class)
        private ByteBlob.Byte256 transactionId;
        /**
         * 输出索引（ vout ），标识使用来自该交易的哪个UTXO（第一个从0开始）
         */
        private int vout;
        /**
         * 满足UTXO上的条件的脚本，用于解锁并花费
         * <Sig> <PubKey>
         */
        @JsonSerialize(using = Script.ScriptByteArrayToStringSerializer.class)
        private Script scriptSig;
        /**
         * 序列号
         */
        private int sequence;

        public TransactionInput() {}

        public TransactionInput(TransactionInput transactionInput) {
            this.transactionId = transactionInput.getTransactionId();
            this.vout = transactionInput.getVout();
            this.scriptSig = transactionInput.getScriptSig();
            this.sequence = transactionInput.getSequence();
        }

        public TransactionInput(ByteBlob.Byte256 transactionId, int vout, Script scriptSig, int sequence) {
            this.transactionId = transactionId;
            this.vout = vout;
            this.scriptSig = scriptSig;
            this.sequence = sequence;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TransactionInput that = (TransactionInput) o;
            return vout == that.vout &&
                    sequence == that.sequence &&
                    Objects.equals(transactionId, that.transactionId) &&
                    Objects.equals(scriptSig, that.scriptSig);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transactionId, vout, scriptSig, sequence);
        }

        @Override
        public String toString() {
            return "TransactionInput{" +
                    "transactionId='" + transactionId + '\'' +
                    ", vout=" + vout +
                    ", scriptSig='" + scriptSig.toString() + '\'' +
                    ", sequence=" + sequence +
                    '}';
        }

        @Override
        public byte[] serialize() {
            return new ByteArraySerializer.Builder()
                    .push(this.transactionId, false)
                    .push(this.vout)
                    .push(this.scriptSig, true)
                    .push(this.sequence)
                    .build(ByteArraySerializer::new).getData();
        }

        @Override
        public int deserialize(byte[] data) {
            return new ByteArraySerializer.Extractor(data)
                    .pullObject(ByteBlob.Byte256::new, val -> this.transactionId = val)
                    .pullInt(val -> this.vout = val)
                    .pullObjectWithSize(Script::new, val -> this.scriptSig = val)
                    .pullInt(val -> this.sequence = val)
                    .complete();
        }
    }

    @Data
    public static class TransactionOutput implements ByteSerializable {
        /**
         * 一些比特币，最小单位为 聪 satoshis
         */
        private long value;
        /**
         * 定义了花费这些输出所需条件的加密谜题
         * 这个谜题也被称为 锁定脚本 locking script ，见证脚本 witness script ，或者 scriptPubKey
         * OP_DUP OP_HASH160 <PubkeyHash> OP_EQUALVERIFY OP_CHECKSIG
         */
        @JsonSerialize(using = Script.ScriptByteArrayToStringSerializer.class)
        private Script scriptPubKey;

        public TransactionOutput() {}

        public TransactionOutput(long value, Script scriptPubKey) {
            this.value = value;
            this.scriptPubKey = scriptPubKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TransactionOutput output = (TransactionOutput) o;
            return value == output.value &&
                    Objects.equals(scriptPubKey, output.scriptPubKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, scriptPubKey);
        }

        @Override
        public String toString() {
            return "TransactionOutput{" +
                    "value=" + value +
                    ", scriptPubKey='" + scriptPubKey.toString() + '\'' +
                    '}';
        }

        @Override
        public byte[] serialize() {
            return new ByteArraySerializer.Builder()
                    .push(this.value)
                    .push(this.scriptPubKey, true)
                    .build(ByteArraySerializer::new).getData();
        }

        @Override
        public int deserialize(byte[] data) {
            return new ByteArraySerializer.Extractor(data)
                    .pullLong(val -> this.value = val)
                    .pullObjectWithSize(Script::new, val -> this.scriptPubKey = val)
                    .complete();
        }
    }
}

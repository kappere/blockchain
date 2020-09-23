package com.wataru.blockchain.core.primitive;

import com.wataru.blockchain.core.primitive.crypto.Secp256k1;
import com.wataru.blockchain.core.util.EncodeUtil;
import lombok.extern.slf4j.Slf4j;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LockScript {
    private Stack<String> stack = new Stack<>();

    public static Script formatLockScript(ByteBlob.Byte160 publicKeyHash) {
        return new Script.Builder()
                .push(Script.OpCodeType.OP_DUP)
                .push(Script.OpCodeType.OP_HASH160)
                .push(publicKeyHash, true)
                .push(Script.OpCodeType.OP_EQUALVERIFY)
                .push(Script.OpCodeType.OP_CHECKSIG)
                .build(Script::new);
    }

    public static Script formatUnlockScript(ByteBlob.ByteVar sigFull, ByteBlob.ByteVar publicKey) {
        return new Script.Builder()
                .push(sigFull.getData(), true)
                .push(publicKey, true)
                .build(Script::new);
    }

    /**
     * 解锁脚本
     * @param input 解锁脚本
     * @param script 锁定脚本
     * @param signData 交易数据hash
     */
    public boolean unlock(Script input, Script script, ByteBlob.Byte256 signData) {
        try {
            if (script == null) {
                return false;
            }
            Script sc = new Script.Builder()
                    .push(input.getData(), false)
                    .push(script.getData(), false)
                    .build(Script::new);
            AtomicInteger index = new AtomicInteger(0);
            while (index.get() < sc.getData().length) {
                if (!processVar(sc.getData(), index, signData)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    private boolean processVar(byte[] raw, AtomicInteger index, ByteBlob.Byte256 signData) {
        byte var = raw[index.getAndIncrement()];
        switch (var) {
            case Script.OpCodeType.OP_DUP:
                stack.push(stack.peek());
                return true;
            case Script.OpCodeType.OP_HASH160:
                String data = EncodeUtil.hash160(new ByteBlob.Byte256(stack.pop())).toHex();
                stack.push(data);
                return true;
            case Script.OpCodeType.OP_EQUALVERIFY:
                String var1 = stack.pop();
                String var2 = stack.pop();
                return var1.equals(var2);
            case Script.OpCodeType.OP_CHECKSIG:
                String pub = stack.pop();
                String sigFull = stack.pop();
                String sig = sigFull.substring(0, sigFull.length() - 2);
                String sigHash = sigFull.substring(sigFull.length() - 2);
                PublicKey publicKey = Secp256k1.fromPublicByte(EncodeUtil.hexStringToByte(pub));
                return Secp256k1.verifySign(signData, publicKey, new ByteBlob.ByteVar(sig));
            default:
                int length = var & 0xff;
                stack.push(EncodeUtil.bytesToHexString(Arrays.copyOfRange(raw, index.get(), index.get() + length)));
                index.addAndGet(length);
                return true;
        }
    }
}

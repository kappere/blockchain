package com.wataru.blockchain.core.primitive;

import com.wataru.blockchain.core.primitive.crypto.Secp256k1;
import com.wataru.blockchain.core.util.EncodeUtil;

import java.security.PublicKey;
import java.util.Stack;

public class LockScript {
    private Stack<String> stack = new Stack<>();

    public static String formatLockScript(String publicKeyHashHex) {
        return String.format("OP_DUP OP_HASH160 %s OP_EQUALVERIFY OP_CHECKSIG", publicKeyHashHex);
    }

    public static String formatUnlockScript(String sig, String publicKeyHex) {
        return String.format("%s %s", sig, publicKeyHex);
    }

    /**
     * 解锁脚本
     * @param input 解锁脚本
     * @param script 锁定脚本
     * @param signData 交易数据hash
     */
    public boolean unlock(String input, String script, byte[] signData) {
        try {
            if (script == null) {
                return false;
            }
            String sc = input + " " + script;
            String[] vars0 = sc.split("\\s");
            for (String s : vars0) {
                if (!processVar(s, signData)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean processVar(String var, byte[] signData) {
        switch (var) {
            case "OP_DUP":
                stack.push(stack.peek());
                return true;
            case "OP_HASH160":
                String data = EncodeUtil.hash160Hex(stack.pop());
                stack.push(data);
                return true;
            case "OP_EQUALVERIFY":
                String var1 = stack.pop();
                String var2 = stack.pop();
                return var1.equals(var2);
            case "OP_CHECKSIG":
                String pub = stack.pop();
                String sig = stack.pop();
                PublicKey publicKey = Secp256k1.fromPublicByte(EncodeUtil.hexStringToByte(pub));
                return Secp256k1.verifySign(signData, publicKey, EncodeUtil.hexStringToByte(sig));
            default:
                stack.push(var);
                return true;
        }
    }
}

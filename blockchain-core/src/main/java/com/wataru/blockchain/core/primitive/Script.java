package com.wataru.blockchain.core.primitive;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.wataru.blockchain.core.primitive.serialize.ByteArraySerializer;
import com.wataru.blockchain.core.util.EncodeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

@Slf4j
public class Script extends ByteArraySerializer {

    public Script() {}
    public Script(byte[] d) {
        super(d);
    }

    public static class ScriptByteArrayToStringSerializer extends JsonSerializer<Script> {
        @Override
        public void serialize(Script d,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeObject(byteToString(d.data));
        }
    }

    private static String byteToString(byte[] d) {
        if (d == null || d.length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length; i++) {
            String opcode = OpCodeType.getNameByCode(d[i]);
            if (opcode != null) {
                sb.append(opcode);
            } else {
                int length = d[i] & 0xff;
                byte[] tmp = Arrays.copyOfRange(d, i + 1, i + 1 + length);
                sb.append(EncodeUtil.bytesToHexString(tmp));
                i += length;
            }
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public String toString() {
        return byteToString(data);
    }

    public interface OpCodeType {
        static String getNameByCode(byte code) {
            try {
                for (Field declaredField : OpCodeType.class.getDeclaredFields()) {
                    if (declaredField.get(null).equals(code)) {
                        return declaredField.getName();
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
        // push value
        byte OP_0 = 0x00;
        byte OP_FALSE = OP_0;
        byte OP_PUSHDATA1 = 0x4c;
        byte OP_PUSHDATA2 = 0x4d;
        byte OP_PUSHDATA4 = 0x4e;
        byte OP_1NEGATE = 0x4f;
        byte OP_RESERVED = 0x50;
        byte OP_1 = 0x51;
        byte OP_TRUE=OP_1;
        byte OP_2 = 0x52;
        byte OP_3 = 0x53;
        byte OP_4 = 0x54;
        byte OP_5 = 0x55;
        byte OP_6 = 0x56;
        byte OP_7 = 0x57;
        // 这里跟public大小冲突，暂时屏蔽
//        byte OP_8 = 0x58;
        byte OP_9 = 0x59;
        byte OP_10 = 0x5a;
        byte OP_11 = 0x5b;
        byte OP_12 = 0x5c;
        byte OP_13 = 0x5d;
        byte OP_14 = 0x5e;
        byte OP_15 = 0x5f;
        byte OP_16 = 0x60;

        // control
        byte OP_NOP = 0x61;
        byte OP_VER = 0x62;
        byte OP_IF = 0x63;
        byte OP_NOTIF = 0x64;
        byte OP_VERIF = 0x65;
        byte OP_VERNOTIF = 0x66;
        byte OP_ELSE = 0x67;
        byte OP_ENDIF = 0x68;
        byte OP_VERIFY = 0x69;
        byte OP_RETURN = 0x6a;

        // stack ops
        byte OP_TOALTSTACK = 0x6b;
        byte OP_FROMALTSTACK = 0x6c;
        byte OP_2DROP = 0x6d;
        byte OP_2DUP = 0x6e;
        byte OP_3DUP = 0x6f;
        byte OP_2OVER = 0x70;
        byte OP_2ROT = 0x71;
        byte OP_2SWAP = 0x72;
        byte OP_IFDUP = 0x73;
        byte OP_DEPTH = 0x74;
        byte OP_DROP = 0x75;
        byte OP_DUP = 0x76;
        byte OP_NIP = 0x77;
        byte OP_OVER = 0x78;
        byte OP_PICK = 0x79;
        byte OP_ROLL = 0x7a;
        byte OP_ROT = 0x7b;
        byte OP_SWAP = 0x7c;
        byte OP_TUCK = 0x7d;

        // splice ops
        byte OP_CAT = 0x7e;
        byte OP_SUBSTR = 0x7f;
        byte OP_LEFT = (byte) 0x80;
        byte OP_RIGHT = (byte) 0x81;
        byte OP_SIZE = (byte) 0x82;

        // bit logic
        byte OP_INVERT = (byte) 0x83;
        byte OP_AND = (byte) 0x84;
        byte OP_OR = (byte) 0x85;
        byte OP_XOR = (byte) 0x86;
        byte OP_EQUAL = (byte) 0x87;
        byte OP_EQUALVERIFY = (byte) 0x88;
        byte OP_RESERVED1 = (byte) 0x89;
        byte OP_RESERVED2 = (byte) 0x8a;

        // numeric
        byte OP_1ADD = (byte) 0x8b;
        byte OP_1SUB = (byte) 0x8c;
        byte OP_2MUL = (byte) 0x8d;
        byte OP_2DIV = (byte) 0x8e;
        byte OP_NEGATE = (byte) 0x8f;
        byte OP_ABS = (byte) 0x90;
        byte OP_NOT = (byte) 0x91;
        byte OP_0NOTEQUAL = (byte) 0x92;

        byte OP_ADD = (byte) 0x93;
        byte OP_SUB = (byte) 0x94;
        byte OP_MUL = (byte) 0x95;
        byte OP_DIV = (byte) 0x96;
        byte OP_MOD = (byte) 0x97;
        byte OP_LSHIFT = (byte) 0x98;
        byte OP_RSHIFT = (byte) 0x99;

        byte OP_BOOLAND = (byte) 0x9a;
        byte OP_BOOLOR = (byte) 0x9b;
        byte OP_NUMEQUAL = (byte) 0x9c;
        byte OP_NUMEQUALVERIFY = (byte) 0x9d;
        byte OP_NUMNOTEQUAL = (byte) 0x9e;
        byte OP_LESSTHAN = (byte) 0x9f;
        byte OP_GREATERTHAN = (byte) 0xa0;
        byte OP_LESSTHANOREQUAL = (byte) 0xa1;
        byte OP_GREATERTHANOREQUAL = (byte) 0xa2;
        byte OP_MIN = (byte) 0xa3;
        byte OP_MAX = (byte) 0xa4;

        byte OP_WITHIN = (byte) 0xa5;

        // crypto
        byte OP_RIPEMD160 = (byte) 0xa6;
        byte OP_SHA1 = (byte) 0xa7;
        byte OP_SHA256 = (byte) 0xa8;
        byte OP_HASH160 = (byte) 0xa9;
        byte OP_HASH256 = (byte) 0xaa;
        byte OP_CODESEPARATOR = (byte) 0xab;
        byte OP_CHECKSIG = (byte) 0xac;
        byte OP_CHECKSIGVERIFY = (byte) 0xad;
        byte OP_CHECKMULTISIG = (byte) 0xae;
        byte OP_CHECKMULTISIGVERIFY = (byte) 0xaf;

        // expansion
        byte OP_NOP1 = (byte) 0xb0;
        byte OP_CHECKLOCKTIMEVERIFY = (byte) 0xb1;
        byte OP_NOP2 = OP_CHECKLOCKTIMEVERIFY;
        byte OP_CHECKSEQUENCEVERIFY = (byte) 0xb2;
        byte OP_NOP3 = OP_CHECKSEQUENCEVERIFY;
        byte OP_NOP4 = (byte) 0xb3;
        byte OP_NOP5 = (byte) 0xb4;
        byte OP_NOP6 = (byte) 0xb5;
        byte OP_NOP7 = (byte) 0xb6;
        byte OP_NOP8 = (byte) 0xb7;
        byte OP_NOP9 = (byte) 0xb8;
        byte OP_NOP10 = (byte) 0xb9;

        byte OP_INVALIDOPCODE = (byte) 0xff;
    };
}

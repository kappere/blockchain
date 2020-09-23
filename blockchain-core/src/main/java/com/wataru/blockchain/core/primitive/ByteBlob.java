package com.wataru.blockchain.core.primitive;

import com.wataru.blockchain.core.primitive.serialize.ByteArraySerializer;
import com.wataru.blockchain.core.util.EncodeUtil;

import java.util.Arrays;

public class ByteBlob {
    /**
     * 160bit字节流
     */
    public static class Byte160 extends FixedBaseBlob {
        public Byte160() {
            super(160);
            putZero();
        }
        public Byte160(byte[] bytes) {
            this();
            putData(bytes);
        }
        public Byte160(String hex) {
            this();
            putData(hex);
        }
        @Override
        public int deserialize(byte[] data) {
            System.arraycopy(data, 0, this.data, 0, this.data.length);
            return this.data.length;
        }
    }
    /**
     * 256bit字节流
     */
    public static class Byte256 extends FixedBaseBlob {
        public Byte256() {
            super(256);
            putZero();
        }
        public Byte256(byte[] bytes) {
            this();
            putData(bytes);
        }
        public Byte256(String hex) {
            this();
            putData(hex);
        }
        @Override
        public int deserialize(byte[] data) {
            System.arraycopy(data, 0, this.data, 0, this.data.length);
            return this.data.length;
        }
    }
    /**
     * 不定长字节流
     */
    public static class ByteVar extends BaseBlob {
        public ByteVar() {
            super(0);
        }
        public ByteVar(byte[] bytes) {
            super(bytes.length * 8);
            putData(bytes);
        }
        public ByteVar(String hex) {
            super((hex.length() + 1) / 2 * 8);
            putData(hex);
        }
        public ByteVar concat(byte bytes) {
            return concat(new byte[]{bytes});
        }
        public ByteVar concat(byte[] bytes) {
            byte[] result = Arrays.copyOf(data, data.length + bytes.length);
            System.arraycopy(bytes, 0, result, data.length, bytes.length);
            data = result;
            return this;
        }
    }

    public static class FixedBaseBlob extends BaseBlob {
        FixedBaseBlob(int bits) {
            super(bits);
        }
    }

    public static class BaseBlob extends ByteArraySerializer {
        BaseBlob(int bits) {
            data = new byte[bits / 8];
        }
        void putZero() {
            Arrays.fill(data, (byte) 0);
        }
        void putData(byte[] bytes) {
            System.arraycopy(bytes, 0, data, 0, Math.min(bytes.length, data.length));
        }
        void putData(String hex) {
            byte[] bytes = EncodeUtil.hexStringToByte(hex);
            System.arraycopy(bytes, 0, data, 0, Math.min(bytes.length, data.length));
        }
    }
}

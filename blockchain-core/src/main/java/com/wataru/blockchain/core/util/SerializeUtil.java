package com.wataru.blockchain.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SerializeUtil {
    public static void writeLong(ByteArrayOutputStream bos, long data) {
        writeInt(bos, (int)data);
        writeInt(bos, (int)(data >>> 32));
    }
    public static void writeInt(ByteArrayOutputStream bos, int data) {
        writeByte(bos, (byte)data);
        writeByte(bos, (byte)(data >>> 8));
        writeByte(bos, (byte)(data >>> 16));
        writeByte(bos, (byte)(data >>> 24));
    }
    public static void writeByte(ByteArrayOutputStream bos, byte data) {
        bos.write(data);
    }
    public static void writeByteArray(ByteArrayOutputStream bos, byte[] data) {
        try {
            bos.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeString(ByteArrayOutputStream bos, String data) {
        writeByteArray(bos, data.getBytes());
        writeByte(bos, (byte) 0x00);
    }
}

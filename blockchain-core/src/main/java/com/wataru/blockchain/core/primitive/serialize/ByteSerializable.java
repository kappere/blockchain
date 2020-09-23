package com.wataru.blockchain.core.primitive.serialize;

public interface ByteSerializable {
    byte[] serialize();
    int deserialize(byte[] data);
}

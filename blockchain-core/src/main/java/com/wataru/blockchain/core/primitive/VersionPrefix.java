package com.wataru.blockchain.core.primitive;

public interface VersionPrefix {
    byte[] BITCOIN_ADDRESS = new byte[]{0x00};
    byte[] PAYTOSCRIPTHASH_ADDRESS = new byte[]{0x05};
    byte[] BITCOIN_TESTNET_ADDRESS = new byte[]{0x6F};
    byte[] PRIVATE_KEY_WIF = new byte[]{(byte) 0x80};
    byte[] BIP38_ENCRYPT_PRIVATE_KEY = new byte[]{0x42, 0x01};
    byte[] BIP38_ENCRYPT_PUBLIC_KEY = new byte[]{0x1E, (byte) 0xB2, (byte) 0x88, 0x04};
}

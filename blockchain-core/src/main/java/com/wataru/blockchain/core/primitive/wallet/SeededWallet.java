package com.wataru.blockchain.core.primitive.wallet;

import com.wataru.blockchain.core.primitive.crypto.Secp256k1;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeededWallet extends Wallet {
    private List<String> mnemonicCodeWords;
    private List<KeyPair> keys;

    public SeededWallet(List<String> mnemonicCodeWords) {
        this.mnemonicCodeWords = mnemonicCodeWords;
        this.keys = new ArrayList<>();
    }

    public void createNewAddress() {
        String seed = keys.isEmpty()
                ? String.join(":", mnemonicCodeWords)
                : keys.get(keys.size() - 1).getPrivate().toString();
        keys.add(Secp256k1.genKeyPair(seed.getBytes()));
    }

    public static void main(String[] args) {
        SeededWallet wallet1 = new SeededWallet(Arrays.asList("22", "33"));
        wallet1.createNewAddress();

        SeededWallet wallet2 = new SeededWallet(Arrays.asList("44", "55"));
        wallet2.createNewAddress();

    }
}

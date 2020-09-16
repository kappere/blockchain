package com.wataru.blockchain.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wataru.blockchain.core.primitive.Blockchain;
import com.wataru.blockchain.core.primitive.wallet.SingleWallet;
import lombok.Data;

@Data
public class WalletDto {
    private String name;
    private String address;
    private Long balance;
    @JsonIgnore
    private SingleWallet singleWallet;

    public WalletDto(SingleWallet wallet) {
        wallet.refreshUtxos();
        this.name = wallet.getName();
        this.balance = wallet.getBalance();
        this.singleWallet = wallet;
        this.address = Blockchain.publicKeyToAddress(singleWallet.getPublicKey());
    }
}

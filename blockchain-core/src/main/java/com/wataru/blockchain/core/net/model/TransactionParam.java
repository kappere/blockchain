package com.wataru.blockchain.core.net.model;

import lombok.Data;

@Data
public class TransactionParam {
    private String sender;
    private String recipient;
    private int amount;
}

package com.wataru.blockchain.admin.controller;

import com.wataru.blockchain.admin.dto.WalletDto;
import com.wataru.blockchain.core.net.NodeRegistry;
import com.wataru.blockchain.core.net.model.Response;
import com.wataru.blockchain.core.primitive.transaction.Transaction;
import com.wataru.blockchain.core.primitive.wallet.SingleWallet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    private static List<WalletDto> walletList = new ArrayList<>();

    public void init() {
        walletList.add(new WalletDto(new SingleWallet("Admin")));
        walletList.add(new WalletDto(new SingleWallet("Wataru")));
    }

    @GetMapping("/wallets")
    public Response<Object> wallets() {
        if (walletList.isEmpty()) {
            init();
        }
        walletList.forEach(walletDto -> walletDto.getSingleWallet().refreshUtxos());
        walletList.forEach(walletDto -> walletDto.setBalance(walletDto.getSingleWallet().getBalance()));
        return Response.success(walletList);
    }

    @PostMapping("/createTransaction")
    public Response<Object> createTransaction(String walletName, String address, Long value) {
        if (NodeRegistry.clientRegistry.getNodeList().getNodes().isEmpty()) {
            return Response.error("未获取到节点");
        }
        AtomicReference<Transaction> transaction = new AtomicReference<>();
        walletList.stream().filter(item -> item.getName().equals(walletName)).forEach(walletDto -> {
            transaction.set(walletDto.getSingleWallet().createTransaction(address, value));
        });
        if (transaction.get() == null) {
            return Response.error("未找到钱包数据");
        }
        return Response.success(transaction.get());
    }
}

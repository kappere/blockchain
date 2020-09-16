package com.wataru.blockchain.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BlockchainCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlockchainCoreApplication.class, args);
    }
}

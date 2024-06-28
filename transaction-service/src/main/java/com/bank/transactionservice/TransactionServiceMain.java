package com.bank.transactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:kafka.properties")
public class TransactionServiceMain {
    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceMain.class, args);
    }
}
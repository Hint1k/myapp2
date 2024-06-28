package com.bank.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:kafka.properties")
public class AccountServiceMain {
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceMain.class, args);
    }
}
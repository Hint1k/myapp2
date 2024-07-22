package com.bank.customerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:kafka.properties")
public class CustomerServiceMain {
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceMain.class, args);
    }
}
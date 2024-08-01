package com.bank.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:kafka.properties")
public class GatewayServiceMain {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceMain.class, args);
    }
}
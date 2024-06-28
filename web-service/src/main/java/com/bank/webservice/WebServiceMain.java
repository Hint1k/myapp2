package com.bank.webservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:kafka.properties")
public class WebServiceMain {
    public static void main(String[] args) {
        SpringApplication.run(WebServiceMain.class, args);
    }
}
package com.bank.webservice;

import com.bank.webservice.controller.AccountController;
import com.bank.webservice.service.SwaggerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.kafka.bootstrap-servers=localhost:9092", // Ensure it does not attempt to resolve Kafka
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.consumer.enable-auto-commit=false",
        "spring.cloud.zookeeper.enabled=false" // To avoid connection attempts to zookeeper
})
public class WebServiceMainTests {

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate; // Mock Kafka to satisfy dependencies

    @MockBean
    private SwaggerService swaggerService; // Mock SwaggerService to avoid gateway-service.url requirement

    private final AccountController accountController;

    @Autowired
    public WebServiceMainTests(AccountController accountController) {
        this.accountController = accountController;
    }

    //checking if the tests are working at all
    @Test
    public void contextLoads() {
        Assertions.assertNotNull(accountController);
    }
}
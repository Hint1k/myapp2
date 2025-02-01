package com.bank.transactionservice;

import org.junit.jupiter.api.Test;
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
public class TransactionServiceMainTests {

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate; // Mock Kafka to satisfy dependencies

    @Test
    public void contextLoads() {

    }
}
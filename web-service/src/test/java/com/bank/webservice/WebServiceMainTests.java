package com.bank.webservice;

import com.bank.webservice.service.SwaggerServiceImpl;
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
public class WebServiceMainTests {

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate; // Mock Kafka to satisfy dependencies

    @MockBean
    private SwaggerServiceImpl swaggerService; // Mock SwaggerService to avoid gateway-service.url requirement

    //checking if the tests are working at all
    @Test
    public void contextLoads() {

    }
}
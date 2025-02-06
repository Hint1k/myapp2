package com.bank.webservice.testConfig;

import com.bank.webservice.service.LatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CountDownLatch;

@TestConfiguration
@Slf4j
public class TestLatchConfig {

    private final CountDownLatch testLatch = new CountDownLatch(1);

    @Bean
    public LatchService latchService() {
        return new LatchService() {
            @Override
            public void setLatch(CountDownLatch latch) {
                // Do nothing
            }

            @Override
            public CountDownLatch getLatch() {
            // Always return the shared instance so the controller and the test classes use the same latch
                return testLatch;
            }

            @Override
            public void resetLatch() {
                // Do nothing
            }
        };
    }
}
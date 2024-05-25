package com.demo.AccountService.publisher;

import com.demo.AccountService.entity.Account;
import com.demo.AccountService.event.AccountCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountEventPublisher {

    @Autowired
    private KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate;

    public void publishAccountCreatedEvent(Account account) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getAccountNumber(),
                account.getBalance()
        );
        kafkaTemplate.send("account-created", event);
        log.info("Published account created event: {}", event);
    }
}
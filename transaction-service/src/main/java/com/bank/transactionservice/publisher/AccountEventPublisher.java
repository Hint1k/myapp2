package com.bank.transactionservice.publisher;

import com.bank.transactionservice.entity.Account;
import com.bank.transactionservice.event.account.AccountCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountEventPublisher {

    private final KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate;

    @Autowired
    public AccountEventPublisher(KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAccountCreatedEvent(Account account) {

    }
}
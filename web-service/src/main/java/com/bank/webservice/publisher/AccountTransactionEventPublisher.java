package com.bank.webservice.publisher;

import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.combined.AccountTransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AccountTransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public AccountTransactionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAccountTransactionEvent(Long accountId, List<Transaction> transactions) {
        AccountTransactionEvent event = new AccountTransactionEvent(accountId, transactions);
        kafkaTemplate.send("account-transactions-requested", event);
        log.info("Published account-transactions-requested event for account id: {}", event.getAccountId());
    }
}
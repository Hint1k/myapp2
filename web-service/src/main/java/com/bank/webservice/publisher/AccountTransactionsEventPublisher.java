package com.bank.webservice.publisher;

import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.combined.AccountTransactionsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AccountTransactionsEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public AccountTransactionsEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAccountTransactionEvent(Long accountNumber, List<Transaction> transactions) {
        AccountTransactionsEvent event = new AccountTransactionsEvent(
                accountNumber,
                transactions
        );
        kafkaTemplate.send("account-transactions-requested", event);
        log.info("Published account-transactions-requested event for account number: {}", event.getAccountNumber());
    }
}
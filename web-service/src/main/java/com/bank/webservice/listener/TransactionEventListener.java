package com.bank.webservice.listener;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.event.transaction.InitialTransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionEventListener {

    @Autowired
    private AccountCache cache;

    @KafkaListener(topics = "initial-transaction-made", groupId = "web-service")
    public void handleAccountCreatedEvent(InitialTransactionEvent event, Acknowledgment acknowledgment) {
        log.info("Received initial transaction event for account number: {}", event.getAccountNumber());
        Account account = new Account(
                event.getAccountNumber(),
                event.getBalance()
        );
        cache.updateAccountFromCacheByNumber(account.getAccountNumber(), account);
        acknowledgment.acknowledge();
    }
}
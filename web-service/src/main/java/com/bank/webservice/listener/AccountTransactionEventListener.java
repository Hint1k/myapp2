package com.bank.webservice.listener;

import com.bank.webservice.cache.AccountTransactionCache;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.combined.AccountTransactionEvent;
import com.bank.webservice.service.LatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class AccountTransactionEventListener {

    private final LatchService latchService;
    private final AccountTransactionCache cache;

    @Autowired
    public AccountTransactionEventListener(LatchService latchService, AccountTransactionCache cache) {
        this.latchService = latchService;
        this.cache = cache;
    }

    @KafkaListener(topics = "account-transactions-received", groupId = "web-service")
    public void handleAccountTransactionsEvent(AccountTransactionEvent event, Acknowledgment acknowledgment) {
        List<Transaction> transactions = event.getTransactions();
        log.info("Received account-transactions-received event with {} transactions", transactions.size());
        cache.addAccountTransactionsToCache(transactions);
        CountDownLatch latch = latchService.getLatch();
        if (latch != null) {
            latch.countDown();
        }
        acknowledgment.acknowledge();
    }
}
package com.bank.webservice.listener;

import com.bank.webservice.cache.AccountTransactionsCache;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.event.combined.AccountTransactionsEvent;
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
public class AccountTransactionsEventListener {

    private final LatchService latchService;
    private final AccountTransactionsCache cache;

    @Autowired
    public AccountTransactionsEventListener(LatchService latchService, AccountTransactionsCache cache) {
        this.latchService = latchService;
        this.cache = cache;
    }

    @KafkaListener(topics = "account-transactions-received", groupId = "web-service")
    public void handleAccountTransactionsEvent(AccountTransactionsEvent event, Acknowledgment acknowledgment) {
        List<Transaction> transactions = event.getTransactions();
        Long accountNumber = event.getAccountNumber();
        log.info("Received account-transactions-received event with {} transactions for account number: {}",
                transactions.size(), accountNumber);
        cache.addAccountTransactionsToCache(accountNumber, transactions);
        CountDownLatch latch = latchService.getLatch();
        if (latch != null) {
            latch.countDown();
        }
        acknowledgment.acknowledge();
    }
}
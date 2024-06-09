package com.bank.webservice.listener;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.event.account.*;
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
public class AccountEventListener {

    private final LatchService latchService;
    private final AccountCache cache;

    @Autowired
    public AccountEventListener(LatchService latchService, AccountCache cache) {
        this.latchService = latchService;
        this.cache = cache;
    }

    @KafkaListener(topics = "account-created", groupId = "web-service")
    public void handleAccountCreatedEvent(AccountCreatedEvent event, Acknowledgment acknowledgment) {
        Account account = new Account(
                event.getAccountId(),
                event.getAccountNumber(),
                event.getBalance(),
                event.getCurrency(),
                event.getAccountType(),
                event.getAccountStatus(),
                event.getOpenDate(),
                event.getCustomerId()
        );
        log.info("Received account-created event for account id: {}", event.getAccountId());
        cache.addAccountToCache(account.getAccountId(), account);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "account-updated", groupId = "web-service")
    public void handleAccountUpdatedEvent(AccountUpdatedEvent event, Acknowledgment acknowledgment) {
        Account account = new Account(
                event.getAccountId(),
                event.getAccountNumber(),
                event.getBalance(),
                event.getCurrency(),
                event.getAccountType(),
                event.getAccountStatus(),
                event.getOpenDate(),
                event.getCustomerId()
        );
        Long accountId = event.getAccountId();
        log.info("Received account-updated event for account id: {}", accountId);
        cache.updateAccountFromCache(accountId, account);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "account-deleted", groupId = "web-service")
    public void handleAccountDeletedEvent(AccountDeletedEvent event, Acknowledgment acknowledgment) {
        Long accountId = event.getAccountId();
        log.info("Received account-deleted event for account id: {}", accountId);
        cache.deleteAccountFromCache(accountId);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "all-accounts-received", groupId = "web-service")
    public void handleAllAccountsEvent(AllAccountsEvent event, Acknowledgment acknowledgment) {
        List<Account> accounts = event.getAccounts();
        log.info("Received all-accounts-received event with {} accounts", accounts.size());
        cache.addAllAccountsToCache(accounts);
        CountDownLatch latch = latchService.getLatch();
        if (latch != null) {
            latch.countDown();
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "account-details-received", groupId = "web-service")
    public void handleAccountDetailsEvent(AccountDetailsEvent event, Acknowledgment acknowledgment) {
        Account account = new Account(
                event.getAccountId(),
                event.getAccountNumber(),
                event.getBalance(),
                event.getCurrency(),
                event.getAccountType(),
                event.getAccountStatus(),
                event.getOpenDate(),
                event.getCustomerId()
        );
        log.info("Received account-details-received event for account id: {}", event.getAccountId());
        cache.addAccountToCache(account.getAccountId(), account);
        acknowledgment.acknowledge();
    }
}
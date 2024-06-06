package com.bank.webservice.listener;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.event.account.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AccountEventListener {

    @Autowired
    private AccountCache cache;

    @KafkaListener(topics = "account-created", groupId = "web-service")
    public void handleAccountCreatedEvent(AccountCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received account-created event for account id: {}", event.getAccountId());
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
        cache.addAccountToCache(account.getAccountId(), account);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "account-details-received", groupId = "web-service")
    public void handleAccountDetailsEvent(AccountDetailsEvent event, Acknowledgment acknowledgment) {
        log.info("Received account-details-received event for account id: {}", event.getAccountId());
        Account account = new Account(
                event.getAccountId(),
                event.getAccountNumber(),
                event.getBalance(),
                event.getCurrency(),
                event.getAccountType(),
                event.getAccountStatus(),
                event.getOpenDate(),
                // TODO add account history
                event.getCustomerId()
        );
        cache.addAccountToCache(account.getAccountId(), account);
        acknowledgment.acknowledge(); // commit offset after successfully added to cache
    }

    @KafkaListener(topics = "all-accounts-received", groupId = "web-service")
    public void handleAllAccountsEvent(AllAccountsEvent event, Acknowledgment acknowledgment) {
        List<Account> accounts = event.getAccounts();
        log.info("Received all-accounts-received event with {} accounts", accounts.size());
        cache.addAllAccountsToCache(accounts);
        acknowledgment.acknowledge(); // commit offset after successfully added to cache
    }

    @KafkaListener(topics = "account-deleted", groupId = "web-service")
    public void handleAccountDeletedEvent(AccountDeletedEvent event, Acknowledgment acknowledgment) {
        Long accountId = event.getAccountId();
        log.info("Received account-deleted event for account id: {}", accountId);
        cache.deleteAccountFromCacheById(accountId);
        acknowledgment.acknowledge(); // commit offset after successfully added to cache
    }

    @KafkaListener(topics = "account-updated", groupId = "web-service")
    public void handleAccountUpdatedEvent(AccountUpdatedEvent event, Acknowledgment acknowledgment) {
        Account account = new Account(
                // TODO remove fields that cannot be updated later
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
        cache.updateAccountFromCacheById(accountId, account);
        acknowledgment.acknowledge(); // commit offset after successfully added to cache
    }
}
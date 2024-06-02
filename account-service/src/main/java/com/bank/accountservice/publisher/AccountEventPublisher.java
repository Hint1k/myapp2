package com.bank.accountservice.publisher;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AccountEventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAccountCreatedEvent(Account account) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                account.getCustomerId()
        );
        kafkaTemplate.send("account-created", event);
        log.info("Published account-created event for account id: {}", event.getAccountId());
        //TODO add check later with completableFuture
    }

    public void publishAccountDetailsEvent(Account account) {
        AccountDetailsEvent event = new AccountDetailsEvent(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                // TODO add account history
                account.getCustomerId()
        );
        kafkaTemplate.send("account-details-received", event);
        log.info("Published account-details-received event for account id: {}", event.getAccountId());
        // TODO add check later with completableFuture
    }

    public void publishAllAccountsEvent(List<Account> accounts) {
        AllAccountsEvent event = new AllAccountsEvent(accounts);
        kafkaTemplate.send("all-accounts-received", event);
        log.info("Published all-accounts-received event with {} accounts", accounts.size());
        // TODO add check later with completableFuture
    }

    public void publishAccountDeletedEvent(Long accountId) {
        AccountDeletedEvent event = new AccountDeletedEvent(accountId);
        kafkaTemplate.send("account-deleted", event);
        log.info("Published account-deleted event for account id: {}", event.getAccountId());
        // TODO add check later with completableFuture
    }

    public void publishAccountUpdatedEvent(Account account) {
        AccountUpdatedEvent event = new AccountUpdatedEvent(
                // TODO remove fields that cannot be updated later
                account.getAccountId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                account.getCustomerId()
        );
        kafkaTemplate.send("account-updated", event);
        log.info("Published account-updated event for account id: {}", event.getAccountId());
        // TODO add check later with completableFuture
    }
}
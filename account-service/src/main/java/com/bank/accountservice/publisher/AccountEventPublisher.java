package com.bank.accountservice.publisher;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.event.AccountCreatedEvent;
import com.bank.accountservice.event.AccountDetailsEvent;
import com.bank.accountservice.event.AllAccountsEvent;
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
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                account.getCustomerId()
        );
        kafkaTemplate.send("account-created", event);
        log.info("Published account created event: {}", event);
        //TODO add check later with completableFuture
    }

    public void publishAccountDetailsEvent(Account account) {
        AccountDetailsEvent event = new AccountDetailsEvent(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                account.getCustomerId()
        );
        kafkaTemplate.send("account-details-received", event);
        log.info("Published account details event: {}", event);
        // TODO add check later with completableFuture
    }

    public void publishAllAccountsEvent(List<Account> accounts) {
        AllAccountsEvent event = new AllAccountsEvent(accounts);
        kafkaTemplate.send("all-accounts-received", event);
        log.info("Published event with {} accounts", accounts.size());
        // TODO add check later with completableFuture
    }
}
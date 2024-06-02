package com.bank.webservice.publisher;

import com.bank.webservice.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.bank.webservice.dto.Account;

import java.util.List;

@Component
@Slf4j
public class AccountEventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAccountCreatedEvent(Account account) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                account.getCustomerId()
        );
        kafkaTemplate.send("account-creation-requested", event);
        log.info("Published account-creation-requested event for account number: {}",
                event.getAccountNumber());
        // add check later with CompletableFuture
    }

    public void publishAccountDetailsEvent(Account account) {
        AccountDetailsEvent event = new AccountDetailsEvent(
                account.getAccountId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        kafkaTemplate.send("account-details-requested", event);
        log.info("Published account-details-requested event for account id: {}",
                event.getAccountId());
        // add check later with CompletableFuture
    }

    public void publishAllAccountsEvent(List<Account> accounts) {
        AllAccountsEvent event = new AllAccountsEvent(accounts);
        kafkaTemplate.send("all-accounts-requested", event);
        log.info("Published all-accounts-requested event");
        // add check later with CompletableFuture
    }

    public void publishAccountDeletedEvent(Long accountId) {
        AccountDeletedEvent event = new AccountDeletedEvent(accountId);
        kafkaTemplate.send("account-deletion-requested", event);
        log.info("Published account-deletion-requested event for account id: {}",
                event.getAccountId());
        // add check later with CompletableFuture
    }

//    public void publishAccountUpdatedEvent(Account account) {
//        AccountUpdatedEvent event = new AccountUpdatedEvent(
//                account.getAccountId(),
//                account.getBalance(),
//                account.getCurrency(),
//                account.getAccountStatus()
//        );
//        kafkaTemplate.send("account-update-requested", event);
//        log.info("Published account-update-requested event for account id: {}",
//                event.getAccountId());
//        // add check later with CompletableFuture
//    }
}
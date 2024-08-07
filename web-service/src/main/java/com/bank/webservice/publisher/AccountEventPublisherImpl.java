package com.bank.webservice.publisher;

import com.bank.webservice.event.account.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.bank.webservice.dto.Account;

import java.util.ArrayList;

@Component
@Slf4j
public class AccountEventPublisherImpl implements AccountEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public AccountEventPublisherImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishAccountCreatedEvent(Account account) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                account.getCustomerNumber()
        );
        kafkaTemplate.send("account-creation-requested", event);
        log.info("Published account-creation-requested event for account number: {}", event.getAccountNumber());
    }

    @Override
    public void publishAccountUpdatedEvent(Account account) {
        AccountUpdatedEvent event = new AccountUpdatedEvent(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                account.getCustomerNumber()
        );
        kafkaTemplate.send("account-update-requested", event);
        log.info("Published account-update-requested event for account id: {}", event.getAccountId());
    }

    @Override
    public void publishAccountDeletedEvent(Long accountId) {
        AccountDeletedEvent event = new AccountDeletedEvent(
                accountId,
                null
        );
        kafkaTemplate.send("account-deletion-requested", event);
        log.info("Published account-deletion-requested event for account id: {}", event.getAccountId());
    }

    @Override
    public void publishAllAccountsEvent() {
        AllAccountsEvent event = new AllAccountsEvent(new ArrayList<>());
        kafkaTemplate.send("all-accounts-requested", event);
        log.info("Published all-accounts-requested event");
    }

    @Override
    public void publishAccountDetailsEvent(Long accountId) {
        AccountDetailsEvent event = new AccountDetailsEvent(
                accountId,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        kafkaTemplate.send("account-details-requested", event);
        log.info("Published account-details-requested event for account id: {}", event.getAccountId());
    }
}
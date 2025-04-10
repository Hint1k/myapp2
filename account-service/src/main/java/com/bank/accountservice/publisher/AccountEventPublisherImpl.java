package com.bank.accountservice.publisher;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.event.account.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountEventPublisherImpl implements AccountEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishAccountCreatedEvent(Account account) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                account.getCustomerNumber()
        );
        kafkaTemplate.send("account-created", event);
        log.info("Published account-created event for account id: {}", event.getAccountId());
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
        kafkaTemplate.send("account-updated", event);
        log.info("Published account-updated event for account id: {}", event.getAccountId());
    }

    @Override
    public void publishAccountDeletedEvent(Long accountId, Long accountNumber) {
        AccountDeletedEvent event = new AccountDeletedEvent(
                accountId,
                accountNumber
        );
        kafkaTemplate.send("account-deleted", event);
        log.info("Published account-deleted event for account id: {}", event.getAccountId());
    }

    @Override
    public void publishAllAccountsEvent(List<Account> accounts) {
        AllAccountsEvent event = new AllAccountsEvent(accounts);
        kafkaTemplate.send("all-accounts-received", event);
        log.info("Published all-accounts-received event with {} accounts", accounts.size());
    }

    @Override
    public void publishAccountDetailsEvent(Account account) {
        AccountDetailsEvent event = new AccountDetailsEvent(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                account.getCustomerNumber()
        );
        kafkaTemplate.send("account-details-received", event);
        log.info("Published account-details-received event for account id: {}", event.getAccountId());
    }
}
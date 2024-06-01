package com.bank.webservice.publisher;

import com.bank.webservice.event.AccountCreatedEvent;
import com.bank.webservice.event.AccountDetailsEvent;
import com.bank.webservice.event.AllAccountsEvent;
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
        log.info("Published account created event for account number: {}",
                event.getAccountNumber());
        // add check later with CompletableFuture
    }

    public void publishAccountDetailsRequestedEvent(Account account) {
        AccountDetailsEvent event = new AccountDetailsEvent(
                account.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        kafkaTemplate.send("account-details-requested", event);
        log.info("Published account details request for account id: {}",
                event.getId());
        // add check later with CompletableFuture
    }

    public void publishAllAccountsEvent(List<Account> accounts) {
        AllAccountsEvent event = new AllAccountsEvent(accounts);
        kafkaTemplate.send("all-accounts-requested", event);
        log.info("Published all accounts request");
        // add check later with CompletableFuture
    }
}
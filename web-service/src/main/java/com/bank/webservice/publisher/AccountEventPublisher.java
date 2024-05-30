package com.bank.webservice.publisher;

import com.bank.webservice.event.AccountCreatedEvent;
import com.bank.webservice.event.AccountDetailsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.bank.webservice.dto.Account;

@Component
@Slf4j
public class AccountEventPublisher {

    @Autowired
    private KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate1;

    @Autowired
    private KafkaTemplate<String, AccountDetailsEvent> kafkaTemplate2;

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
        kafkaTemplate1.send("account-created-requested", event);
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

        kafkaTemplate2.send("account-details-requested", event);
        log.info("Published account details request for account id: {}",
                event.getId());
        // add check later with CompletableFuture
    }
}
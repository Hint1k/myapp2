package com.bank.webservice.listener;

import com.bank.webservice.cache.AccountDetailsCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.event.AccountDetailsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountEventListener {

    @Autowired
    private AccountDetailsCache cache;

    @KafkaListener(topics = "account-details-received", groupId = "web-service")
    public void handleAccountDetailsEvent(AccountDetailsEvent event, Acknowledgment acknowledgment) {
        log.info("Received account details for account number: {}",
                event.getAccountNumber());
        Account account = new Account(
                event.getId(),
                event.getAccountNumber(),
                event.getBalance(),
                event.getCurrency(),
                event.getAccountType(),
                event.getAccountStatus(),
                event.getOpenDate(),
                event.getCustomerId()
        );
        cache.addAccountDetails(account.getId(), account);
        acknowledgment.acknowledge(); // commit offset after successfully added to cache
    }
}
package com.bank.webservice.listener;

import com.bank.webservice.cache.AccountCreatedCache;
import com.bank.webservice.cache.AccountDetailsCache;
import com.bank.webservice.cache.AllAccountsCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.event.AccountCreatedEvent;
import com.bank.webservice.event.AccountDetailsEvent;
import com.bank.webservice.event.AllAccountsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AccountEventListener {

    @Autowired // TODO make it just one cache field
    private AccountDetailsCache cache1;

    @Autowired
    private AllAccountsCache cache2;

    @Autowired
    private AccountCreatedCache cache3;

    @KafkaListener(topics = "account-created", groupId = "web-service")
    public void handleAccountCreatedEvent(AccountCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received account created event for account id: {}", event.getId());
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
        cache3.addAccount(account.getId(), account);
        acknowledgment.acknowledge();
    }


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
        cache1.addAccountDetails(account.getId(), account);
        acknowledgment.acknowledge(); // commit offset after successfully added to cache
    }

    @KafkaListener(topics = "all-accounts-received", groupId = "web-service")
    public void handleAllAccountsEvent(AllAccountsEvent event, Acknowledgment acknowledgment) {
        List<Account> accounts = event.getAccounts();
        log.info("Received {} accounts", accounts.size());
        cache2.addAllAccounts(accounts);
        acknowledgment.acknowledge(); // commit offset after successfully added to cache
    }
}
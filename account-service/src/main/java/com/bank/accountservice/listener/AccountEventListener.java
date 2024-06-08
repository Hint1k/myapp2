package com.bank.accountservice.listener;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.event.account.*;
import com.bank.accountservice.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AccountEventListener {

    private final AccountService accountService;

    @Autowired
    public AccountEventListener(AccountService accountService) {
        this.accountService = accountService;
    }

    @KafkaListener(topics = "account-creation-requested", groupId = "account-service")
    public void handleAccountCreatedEvent(AccountCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received account-creation-requested event for account number: {}", event.getAccountNumber());
        Account account = new Account(
                event.getAccountNumber(),
                event.getBalance(),
                event.getCurrency(),
                event.getAccountType(),
                event.getAccountStatus(),
                event.getOpenDate(),
                event.getCustomerId()
        );
        try {
            accountService.saveAccount(account);
            log.info("Saved account number: {}", account.getAccountNumber());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error saving account: {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "account-update-requested", groupId = "account-service")
    public void handleAccountUpdatedEvent(AccountUpdatedEvent event, Acknowledgment acknowledgment) {
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
        log.info("Received account-update-requested event for account id: {}", event.getAccountId());
        try {
            accountService.updateAccount(account);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error updating account by id: {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "account-deletion-requested", groupId = "account-service")
    public void handleAccountDeletedEvent(AccountDeletedEvent event, Acknowledgment acknowledgment) {
        Long accountId = event.getAccountId();
        log.info("Received account-deletion-requested event for account id: {}", accountId);
        try {
            accountService.deleteAccount(accountId);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error deleting account by id: {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "all-accounts-requested", groupId = "account-service")
    public void handleAllAccountsEvent(AllAccountsEvent event, Acknowledgment acknowledgment) {
        List<Account> accounts = event.getAccounts();
        log.info("Received all-accounts-requested event");
        try {
            accounts = accountService.findAllAccounts();
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error finding all accounts: {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "account-details-requested", groupId = "account-service")
    public void handleAccountDetailsEvent(AccountDetailsEvent event, Acknowledgment acknowledgment) {
        Long accountId = event.getAccountId();
        log.info("Received account-details-requested event for account id: {}", accountId);
        try {
            accountService.findAccountById(accountId);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error finding account by id: {}", e.getMessage());
            // TODO implement error handling later
        }
    }
}
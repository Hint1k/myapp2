package com.bank.accountservice.listener;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.event.*;
import com.bank.accountservice.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j // logger
public class AccountEventListener {

    @Autowired
    private AccountService accountService;

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
        account.setTransactionHistories(new ArrayList<>());
        try {
            accountService.saveAccount(account);
            log.info("Saved account number: {}", account.getAccountNumber());
            acknowledgment.acknowledge(); // Commit offset after successful save
        } catch (Exception e) {
            log.error("Error saving account: {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "account-details-requested", groupId = "account-service")
    public void handleAccountDetailsEvent(AccountDetailsEvent event, Acknowledgment acknowledgment) {
        Long accountId = event.getAccountId();
        log.info("Received account-details-requested event for account id: {}", accountId);
        log.debug("Deserialized AccountDetailsEvent: {}", event);
        try {
            accountService.findAccountById(accountId);
            acknowledgment.acknowledge(); // Commit offset after the account found
        } catch (Exception e) {
            log.error("Error finding account by id: {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "all-accounts-requested", groupId = "account-service")
    public void handleAllAccountsEvent(AllAccountsEvent event, Acknowledgment acknowledgment) {
        List<Account> accounts = event.getAccounts();
        log.info("Received all-accounts-requested event");
        try {
            accounts = accountService.findAllAccounts();
            acknowledgment.acknowledge(); // Commit offset after all accounts found
        } catch (Exception e) {
            log.error("Error finding all accounts: {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "account-deletion-requested", groupId = "account-service")
    public void handleAccountDeletedEvent(AccountDeletedEvent event, Acknowledgment acknowledgment) {
        Long accountId = event.getAccountId();
        log.info("Received account-deletion-requested event for account id: {}", accountId);
        try {
            accountService.deleteAccountById(accountId);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error deleting account by id: {}", e.getMessage());
            // TODO implement error handling later
        }
    }

    @KafkaListener(topics = "account-update-requested", groupId = "account-service")
    public void handleAccountUpdatedEvent(AccountUpdatedEvent event, Acknowledgment acknowledgment) {
        Account account = new Account(
                event.getAccountNumber(),
                event.getBalance(),
                event.getCurrency(),
                event.getAccountType(),
                event.getAccountStatus(),
                event.getOpenDate(),
                event.getCustomerId()
        );
        Long accountId = event.getAccountId();
        account.setAccountId(accountId);
        log.info("Received account-update-requested event for account id: {}", accountId);
        try {
            accountService.updateAccountById(account);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error updating account by id: {}", e.getMessage());
            // TODO implement error handling later
        }
    }
}
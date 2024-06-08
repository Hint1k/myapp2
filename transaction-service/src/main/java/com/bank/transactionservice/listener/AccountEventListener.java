//package com.bank.transactionservice.listener;
//
//import com.bank.transactionservice.entity.Account;
//import com.bank.transactionservice.event.account.AccountCreatedEvent;
//import com.bank.transactionservice.event.account.AccountDeletedEvent;
//import com.bank.transactionservice.event.account.AccountUpdatedEvent;
//import com.bank.transactionservice.service.AccountService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class AccountEventListener {
//
//    private final AccountService accountService;
//
//    @Autowired
//    public AccountEventListener(AccountService accountService) {
//        this.accountService = accountService;
//    }
//
//    // TODO update this later when account-created event would work
//    @KafkaListener(topics = "account-created", groupId = "transaction-service")
//    public void handleAccountCreatedEvent(AccountCreatedEvent event, Acknowledgment acknowledgment) {
//        log.info("Received account creation event for account number: {}", event.getAccountNumber());
//        try {
//            Account account = new Account(
//                    event.getAccountNumber(),
//                    event.getBalance(),
//                    event.getCurrency(),
//                    event.getAccountType(),
//                    event.getAccountStatus(),
//                    event.getOpenDate(),
//                    event.getTransactions(),
//                    event.getCustomerId()
//            );
//            accountService.saveAccount(account);
//            log.info("Saved account number: {}", account.getAccountNumber());
//            acknowledgment.acknowledge();
//        } catch (Exception exception) {
//            log.error("Failed to handle account creation event", exception);
//            // TODO Handle exception here later
//        }
//    }
//
//    @KafkaListener(topics = "account-deleted", groupId = "transaction-service")
//    public void handleAccountDeletedEvent(AccountDeletedEvent event, Acknowledgment acknowledgment) {
//        Long accountId = event.getAccountId();
//        log.info("Received account-deleted event for account id: {}", accountId);
//        accountService.deleteAccount(accountId);
//        acknowledgment.acknowledge(); // commit offset after successfully added to cache
//    }
//
//    @KafkaListener(topics = "account-updated", groupId = "transaction-service")
//    public void handleAccountUpdatedEvent(AccountUpdatedEvent event, Acknowledgment acknowledgment) {
//        Account account = new Account(
//                // TODO remove fields that cannot be updated later
//                event.getAccountId(),
//                event.getAccountNumber(),
//                event.getBalance(),
//                event.getCurrency(),
//                event.getAccountType(),
//                event.getAccountStatus(),
//                event.getOpenDate(),
//                event.getTransactions(),
//                event.getCustomerId()
//        );
//        Long accountId = event.getAccountId();
//        log.info("Received account-updated event for account id: {}", accountId);
//        accountService.updateAccount(account);
//        acknowledgment.acknowledge(); // commit offset after successfully added to cache
//    }
//}
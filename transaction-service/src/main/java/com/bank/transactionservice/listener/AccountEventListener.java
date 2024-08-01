package com.bank.transactionservice.listener;

import com.bank.transactionservice.event.account.AccountUpdatedEvent;
import com.bank.transactionservice.service.TransactionService;
import com.bank.transactionservice.util.AccountStatus;
import lombok.extern.slf4j.Slf4j;
import com.bank.transactionservice.event.account.AccountDeletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountEventListener {

    private final TransactionService service;

    @Autowired
    public AccountEventListener(TransactionService service) {
        this.service = service;
    }

    @KafkaListener(topics = "account-deleted", groupId = "transaction-service")
    public void handleAccountDeletedEvent(AccountDeletedEvent event, Acknowledgment acknowledgment) {
        Long accountId = event.getAccountId();
        Long accountNumber = event.getAccountNumber();
        log.info("Received account-deleted event for account id: {}", accountId);
        service.freezeTransactions(accountNumber);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "account-updated", groupId = "transaction-service")
    public void handleAccountUpdatedEvent(AccountUpdatedEvent event, Acknowledgment acknowledgment) {
        Long accountId = event.getAccountId();
        log.info("Received account-update-requested event for account id: {}", accountId);
        Long accountNumber = event.getAccountNumber();
        AccountStatus accountStatus = event.getAccountStatus();
        if (!accountStatus.equals(AccountStatus.ACTIVE)) {
            service.suspendOrUnsuspendTransactions(accountNumber, "suspend");
        } else {
            service.suspendOrUnsuspendTransactions(accountNumber, "unSuspend");
        }
        acknowledgment.acknowledge();
    }
}
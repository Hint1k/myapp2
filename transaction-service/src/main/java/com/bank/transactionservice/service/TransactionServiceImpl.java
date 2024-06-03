package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Account;
import com.bank.transactionservice.event.AccountCreatedEvent;
import com.bank.transactionservice.publisher.TransactionEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionEventPublisher eventPublisher;

    @Override
    public Account createInitialTransaction(AccountCreatedEvent event) {
        Account account = new Account(
                event.getAccountNumber(),
                event.getBalance()
        );

        // temp code to give a new customer their money
        BigDecimal newBalance = BigDecimal.valueOf(1000);

        // transfer initial money to a newly created account
        account.setBalance(newBalance);

        // publishing event with new balance
        eventPublisher.publishInitialTransactionEvent(account);
        log.info("New transaction for account number {} has been made", account.getAccountId());

        return account;
    }
}
package com.bank.transactionservice.service;

import com.bank.transactionservice.dto.Account;
import com.bank.transactionservice.dto.AccountCreatedEvent;
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
                event.getBalance() // it should be zero for new account
        );

        // temp code to give a new customer their money
        BigDecimal newBalance = BigDecimal.valueOf(1000);

        // transfer initial money to a newly created account
        account.setBalance(newBalance);

        // publishing event with new balance
        eventPublisher.publishTransactionCreatedEvent(account);
        log.info("New transaction for account {} has been made", account);

        return account;
    }
}
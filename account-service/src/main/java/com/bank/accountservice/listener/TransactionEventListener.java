package com.bank.accountservice.listener;

import com.bank.accountservice.dto.TransactionCreatedEventDTO;
import com.bank.accountservice.service.AccountService;
import com.bank.accountservice.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j // logger
public class TransactionEventListener {

    @Autowired
    private AccountService accountService;

    @KafkaListener(topics = "transaction-created", groupId = "account-service")
    public void handleTransactionCreatedEvent(TransactionCreatedEventDTO event) {
        log.info("Received TransactionCreatedEvent: {}", event);

        // get new balance
        BigDecimal amount = event.getAmount();

        // get accountId
        Long accountId = event.getAccountId();

        // get account
        Account account = accountService.findAccountById(accountId);

        // change balance
        BigDecimal balance = account.getBalance();
        balance = balance.add(amount);
        account.setBalance(balance);

        // save account with new balance
        accountService.saveAccount(account);
    }
}
package com.demo.AccountService.listener;

import com.demo.AccountService.dto.TransactionCreatedEventDTO;
import com.demo.AccountService.entity.Account;
import com.demo.AccountService.service.AccountService;
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
package com.demo.AccountService.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.demo.AccountService.entity.Account;
import com.demo.AccountService.event.AccountCreatedEvent;
import com.demo.AccountService.repository.AccountRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

    private final KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate;

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(KafkaTemplate<String,
            AccountCreatedEvent> kafkaTemplate, AccountRepository accountRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public Account createAccount(Account account) {
        // Save the account to the database
        Account savedAccount = accountRepository.save(account);

        // Create the event
        AccountCreatedEvent event = new AccountCreatedEvent(
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getBalance(),
                savedAccount.getCurrency(),
                savedAccount.getAccountType(),
                savedAccount.getStatus(),
                savedAccount.getOpenDate(),
                savedAccount.getUserId()
        );

        // Send the event to the Kafka topic
        kafkaTemplate.send("account-created-topic", event);

        return savedAccount;
    }
}
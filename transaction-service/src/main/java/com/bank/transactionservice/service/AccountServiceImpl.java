package com.bank.transactionservice.service;

import com.bank.transactionservice.entity.Account;
import com.bank.transactionservice.publisher.AccountEventPublisher;
import com.bank.transactionservice.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

//    private final AccountEventPublisher publisher;

    private final AccountRepository repository;

    @Autowired
    public AccountServiceImpl(AccountEventPublisher publisher, AccountRepository repository) {
//        this.publisher = publisher;
        this.repository = repository;
    }

    @Override
    @Transactional
    public Account findAccountById(Long accountId) {
        Account account = repository.findById(accountId).orElse(null);
        if (account == null) {
            // TODO return message to the web-service
            throw new EntityNotFoundException("Account with id " + accountId + " not found");
        }
        log.info("Retrieved account with id: {}", accountId);
        return account;
    }

    @Override
    @Transactional
    public Account saveAccount(Account account) {
        Account savedAccount = repository.save(account);
        log.info("Saved account {}", savedAccount);
        return account;
    }

    @Override
    @Transactional
    public void deleteAccountById(Long accountId){
        repository.deleteById(accountId);
        log.info("Account with id: {} deleted", accountId);
    }

    @Override
    @Transactional
    public void updateAccount(Account account){
        // JPA repository should merge instead of save
        repository.save(account);
        log.info("Account with id: {} updated", account.getAccountId());
    }
}
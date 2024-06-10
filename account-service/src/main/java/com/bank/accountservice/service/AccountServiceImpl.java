package com.bank.accountservice.service;

import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.publisher.AccountEventPublisher;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.bank.accountservice.entity.Account;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountEventPublisher publisher;
    private final AccountRepository repository;

    @Autowired
    public AccountServiceImpl(AccountRepository repository,
                              AccountEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public void saveAccount(Account account) {
        Account savedAccount = repository.save(account);
        publisher.publishAccountCreatedEvent(savedAccount);
        log.info("Account saved: {}", savedAccount);
    }

    @Override
    @Transactional
    public void updateAccount(Account account) {
        // JPA repository should merge instead of save
        repository.save(account);
        publisher.publishAccountUpdatedEvent(account);
        log.info("Account with id: {} updated", account.getAccountId());
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        repository.deleteById(accountId);
        publisher.publishAccountDeletedEvent(accountId);
        log.info("Account with id: {} deleted", accountId);
    }

    @Override
    @Transactional
    public List<Account> findAllAccounts() {
        List<Account> accounts = repository.findAll();
        publisher.publishAllAccountsEvent(accounts);
        log.info("Retrieved {} accounts", accounts.size());
        return accounts;
    }

    @Override
    @Transactional
    public Account findAccountById(Long accountId) {
        Account account = repository.findById(accountId).orElse(null);
        if (account == null) {
            // TODO return message to the web-service
            log.error("Account with id: {} not found", accountId);
            throw new EntityNotFoundException("Account with id " + accountId + " not found");
        }
        publisher.publishAccountDetailsEvent(account);
        log.info("Retrieved account with id: {}", accountId);
        return account;
    }

    @Override
    @Transactional
    public Account findAccountByItsNumber(Long accountNumber) {
        Account account = repository.findAccountByItsNumber(accountNumber);
        if (account == null) {
            // TODO return message to the web-service
            log.error("Account with number: {} not found", accountNumber);
            throw new EntityNotFoundException("Account with number " + accountNumber + " not found");
        }
        log.info("Retrieved account with number: {}", accountNumber);
        return account;
    }
}
package com.bank.accountservice.service;

import com.bank.accountservice.publisher.AccountEventPublisher;
import com.bank.accountservice.repository.AccountRepository;
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

    private final AccountRepository repository;
    private final AccountEventPublisher publisher;

    @Autowired
    public AccountServiceImpl(AccountRepository repository, AccountEventPublisher publisher) {
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
        repository.save(account);
        publisher.publishAccountUpdatedEvent(account);
        log.info("Account with id: {} updated", account.getAccountId());
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = repository.findById(accountId).orElse(null);
        if (account == null) {
            handleNullAccount(accountId);
        } else {
            Long accountNumber = account.getAccountNumber();
            repository.deleteById(accountId);
            publisher.publishAccountDeletedEvent(accountId, accountNumber);
            log.info("Account with id: {} deleted", accountId);
        }
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
            handleNullAccount(accountId);
        }
        publisher.publishAccountDetailsEvent(account);
        log.info("Retrieved account with id: {}", accountId);
        return account;
    }

    private void handleNullAccount(Long accountId) {
        // TODO return message to the web-service
        log.error("Account with id: {} not found", accountId);
        throw new EntityNotFoundException("Account with id " + accountId + " not found");
    }
}
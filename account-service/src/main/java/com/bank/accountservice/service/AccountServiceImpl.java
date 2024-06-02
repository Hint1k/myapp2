package com.bank.accountservice.service;

import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.publisher.AccountEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.bank.accountservice.entity.Account;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountEventPublisher accountEventPublisher;

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              AccountEventPublisher accountEventPublisher) {
        this.accountRepository = accountRepository;
        this.accountEventPublisher = accountEventPublisher;
    }

    @Override
    @Transactional
    public Account saveAccount(Account account) {
        Account savedAccount = accountRepository.save(account);
        accountEventPublisher.publishAccountCreatedEvent(savedAccount);
        log.info("Account saved: {}", savedAccount);
        return savedAccount;
    }

    @Override
    @Transactional
    public Account findAccountById(Long accountId) {
        Account account = accountRepository.getReferenceById(accountId);
        accountEventPublisher.publishAccountDetailsEvent(account);
        log.info("Retrieved account with id: {}", accountId);
        return account;
    }

    @Override
    @Transactional
    public List<Account> findAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        accountEventPublisher.publishAllAccountsEvent(accounts);
        log.info("Retrieved {} accounts", accounts.size());
        return accounts;
    }

    @Override
    @Transactional
    public void deleteAccountById(Long accountId){
        accountRepository.deleteById(accountId);
        accountEventPublisher.publishAccountDeletedEvent(accountId);
        log.info("Account with id: {} deleted", accountId);
    }

    @Override
    @Transactional
    public void updateAccountById(Account account){
//        accountRepository.save(account); // JPA repository should merge it internally
//        accountEventPublisher.publishAccountUpdatedEvent(accountId);
//        log.info("Account with id: {} updated", accountId);
    }
}
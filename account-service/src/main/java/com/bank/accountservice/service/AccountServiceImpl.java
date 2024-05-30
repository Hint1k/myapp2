package com.bank.accountservice.service;

import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.publisher.AccountEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.bank.accountservice.entity.Account;
import org.springframework.transaction.annotation.Transactional;

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
    public Account findAccountById(Long id) {
        Account account = accountRepository.getReferenceById(id);
        accountEventPublisher.publishAccountDetailsEvent(account);
        log.info("Retrieved account by id: {}", id);
        return account;
    }
}
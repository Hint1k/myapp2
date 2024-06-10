package com.bank.accountservice.service;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.util.TransactionType;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    private final AccountRepository accountRepository;

    @Autowired
    public BalanceServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    @Synchronized // TODO implement optimistic locking with JPA instead
//    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void updateAccountBalance(Long accountNumber, BigDecimal amount, Long transactionId,
                                     TransactionType transactionType) {

        Account account = accountRepository.findAccountByItsNumber(accountNumber);

        if (account == null) {
            // TODO implement return message to the web-service
            log.error("Account with number: {} not found", accountNumber);
            throw new RuntimeException("Account for number: " + accountNumber + " not found");
        }

        BigDecimal newBalance = calculateNewBalance(account.getBalance(), amount, transactionId, transactionType);
        account.setBalance(newBalance);

        // JPA repository should merge instead of save
        accountRepository.save(account);
        log.info("Updated balance for account number: {}", accountNumber);
    }

    private BigDecimal calculateNewBalance(BigDecimal currentBalance, BigDecimal amount, Long transactionId,
                                           TransactionType transactionType) {
        switch (transactionType) {
            case DEPOSIT:
                return currentBalance.add(amount);
            case WITHDRAWAL:
                if (currentBalance.compareTo(amount) < 0) {
                    // TODO implement negative balance handling later
                    amount = currentBalance; // temp code to avoid errors
//                    throw new InsufficientFundsException("Insufficient funds for account: " + accountNumber);
                }
                return currentBalance.subtract(amount);
            case TRANSFER:
                // TODO implement TRANSFER type later
                currentBalance = currentBalance.add(BigDecimal.valueOf(0)); // temp code to avoid errors
            default:
                // wrong transaction type should be handled in the web-service module of the project
                log.warn("Unsupported transaction type: {}", transactionType);
                return currentBalance;
        }
    }
}
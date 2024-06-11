package com.bank.accountservice.service;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.publisher.TransactionEventPublisher;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.util.TransactionType;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    private final AccountRepository accountRepository;
    private final TransactionEventPublisher publisher;

    @Autowired
    public BalanceServiceImpl(AccountRepository accountRepository, TransactionEventPublisher publisher) {
        this.accountRepository = accountRepository;
        this.publisher = publisher;
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

        BigDecimal newBalance = calculateNewBalance(account.getBalance(), amount, transactionType);
        if (Objects.equals(newBalance, BigDecimal.valueOf(-1))) {
            log.error("Insufficient funds on account number: {}. Withdrawal failed.", accountNumber);
            publisher.publishTransactionFailedEvent(transactionId);
        } else if (Objects.equals(newBalance, BigDecimal.valueOf(-2))) {
            log.error("Insufficient funds on source account number: {}. Transfer failed.", accountNumber);
            // TODO implement transfer failure
        } else {
            account.setBalance(newBalance);
            accountRepository.save(account);
            publisher.publishTransactionApprovedEvent(transactionId);
            log.info("Updated balance for account number: {}", accountNumber);
        }
    }

    private BigDecimal calculateNewBalance(BigDecimal currentBalance, BigDecimal amount,
                                           TransactionType transactionType) {
        switch (transactionType) {
            case DEPOSIT:
                return currentBalance.add(amount);
            case WITHDRAWAL:
                if (currentBalance.compareTo(amount) < 0) {
                    // TODO implement negative balance handling later
                    return BigDecimal.valueOf(-1);
                }
                return currentBalance.subtract(amount);
            case TRANSFER:
                // TODO implement TRANSFER type later
                if (currentBalance.compareTo(amount) < 0) {
                    // TODO implement negative balance handling later
                    return BigDecimal.valueOf(-2);
                }
                return currentBalance.add(BigDecimal.valueOf(0)); // temp code to avoid errors
            default:
                // wrong transaction type should be handled in the web-service module of the project
                log.warn("Unsupported transaction type: {}", transactionType);
                return BigDecimal.valueOf(-999);
        }
    }
}
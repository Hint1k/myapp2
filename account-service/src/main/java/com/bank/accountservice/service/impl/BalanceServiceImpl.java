package com.bank.accountservice.service.impl;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.publisher.TransactionEventPublisher;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.service.BalanceService;
import com.bank.accountservice.util.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final AccountRepository repository;
    private final TransactionEventPublisher publisher;

    @Override
    public boolean changeBalance(Account account, BigDecimal amount, TransactionType transactionType,
                                 Long transactionId) {
        boolean hasProblems;
        BigDecimal balance = calculateBalance(account.getBalance(), amount, transactionType);
        hasProblems = hasBalanceProblems(balance, account, transactionId);
        if (hasProblems) {
            return false;
        }
        saveBalanceToDatabase(account, balance);
        return true;
    }

    @Override
    public boolean reverseBalance(Account account, BigDecimal amount, TransactionType transactionType,
                                  Long transactionId) {
        return changeBalance(account, amount.negate(), transactionType, transactionId);
    }

    @Override
    public boolean makeTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount,
                                Long transactionId) {
        boolean isBalanceChanged;
        isBalanceChanged = changeBalance(sourceAccount, amount, TransactionType.WITHDRAWAL, transactionId);
        if (!isBalanceChanged) {
            return false;
        }
        isBalanceChanged = changeBalance(destinationAccount, amount, TransactionType.DEPOSIT, transactionId);
        return isBalanceChanged;
    }

    @Override
    public boolean reverseTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount,
                                   Long transactionId) {
        return makeTransfer(destinationAccount, sourceAccount, amount, transactionId);
    }

    @Override
    public BigDecimal calculateBalance(BigDecimal currentBalance, BigDecimal amount,
                                       TransactionType transactionType) {
        if (transactionType == null) {
            return BigDecimal.valueOf(-2); // Handled null case
        }
        return switch (transactionType) {
            case DEPOSIT -> currentBalance.add(amount);
            case WITHDRAWAL -> {
                if (currentBalance.compareTo(amount.abs()) < 0) {
                    yield BigDecimal.valueOf(-1);
                }
                yield currentBalance.subtract(amount);
            }
            // wrong transaction type should be handled in the web-service module of the project
            default -> BigDecimal.valueOf(-2);
        };
    }

    @Override
    public boolean hasBalanceProblems(BigDecimal balance, Account account, Long transactionId) {
        if (Objects.equals(balance, BigDecimal.valueOf(-1))) {
            handleInsufficientFunds(account, transactionId);
            return true;
        }
        if (Objects.equals(balance, BigDecimal.valueOf(-2))) {
            handleWrongTransactionType(transactionId);
            return true;
        }
        return false;
    }

    @Override
    public void saveBalanceToDatabase(Account account, BigDecimal newBalance) {
        account.setBalance(newBalance);
        repository.save(account);
        log.info("Updated balance for account number: {}", account.getAccountNumber());
    }

    @Override
    public Account getAccountFromDatabase(Long accountNumber, Long transactionId) {
        return repository.findAccountByAccountNumber(accountNumber);
    }

    @Override
    public void handleInsufficientFunds(Account account, Long transactionId) {
        log.error("Insufficient funds on account number: {}. Transaction id {} is failed.",
                account.getAccountNumber(), transactionId);
        publisher.publishTransactionFailedEvent(transactionId);
    }

    @Override
    public void handleWrongTransactionType(Long transactionId) {
        log.error("Wrong transaction type for transaction id: {}", transactionId);
        publisher.publishTransactionFailedEvent(transactionId);
    }
}
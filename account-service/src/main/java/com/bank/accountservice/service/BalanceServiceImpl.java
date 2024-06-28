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
    public void updateAccountBalance(Long accountSourceNumber, Long accountDestinationNumber, BigDecimal amount,
                                     Long transactionId, TransactionType transactionType) {
        Account sourceAccount = getAccountFromDatabase(accountSourceNumber, transactionId);
        if (sourceAccount != null) {
            if (!transactionType.equals(TransactionType.TRANSFER)) {
                BigDecimal newSourceBalance = calculateNewBalance(sourceAccount.getBalance(), amount, transactionType);
                if (Objects.equals(newSourceBalance, BigDecimal.valueOf(-1))) {
                    handleInsufficientFunds(sourceAccount, transactionId);
                } else if (Objects.equals(newSourceBalance, BigDecimal.valueOf(-2))) {
                    handleWrongTransactionType(transactionId);
                } else {
                    saveNewBalanceToDatabase(sourceAccount, newSourceBalance);
                    publisher.publishTransactionApprovedEvent(transactionId);
                }
            } else {
                transferFundsBetweenAccounts(sourceAccount, accountDestinationNumber, transactionId, amount);
            }
        }
    }

    private void transferFundsBetweenAccounts(Account sourceAccount, Long accountDestinationNumber, Long transactionId,
                                              BigDecimal amount) {
        Account destinationAccount = getAccountFromDatabase(accountDestinationNumber, transactionId);
        if (destinationAccount != null) {
            BigDecimal newSourceBalance = calculateNewBalance(sourceAccount.getBalance(), amount,
                    TransactionType.WITHDRAWAL); // removing money from the source account
            BigDecimal newDestinationBalance = calculateNewBalance(destinationAccount.getBalance(), amount,
                    TransactionType.DEPOSIT); // adding money to the destination account
            if (Objects.equals(newSourceBalance, BigDecimal.valueOf(-1))) {
                handleInsufficientFunds(sourceAccount, transactionId);
            } else if (Objects.equals(newDestinationBalance, BigDecimal.valueOf(-2))) {
                handleWrongTransactionType(transactionId);
            } else {
                saveNewBalanceToDatabase(sourceAccount, newSourceBalance);
                saveNewBalanceToDatabase(destinationAccount, newDestinationBalance);
                publisher.publishTransactionApprovedEvent(transactionId);
            }
        }
    }

    private BigDecimal calculateNewBalance(BigDecimal currentBalance, BigDecimal amount,
                                           TransactionType transactionType) {
        switch (transactionType) {
            case DEPOSIT:
                return currentBalance.add(amount);
            case WITHDRAWAL:
                if (currentBalance.compareTo(amount) < 0) {
                    return BigDecimal.valueOf(-1);
                }
                return currentBalance.subtract(amount);
            default:
                // wrong transaction type should be handled in the web-service module of the project
                return BigDecimal.valueOf(-2);
        }
    }

    private void saveNewBalanceToDatabase(Account account, BigDecimal newBalance) {
        account.setBalance(newBalance);
        accountRepository.save(account);
        log.info("Updated balance for account number: {}", account.getAccountNumber());
    }

    private Account getAccountFromDatabase(Long accountNumber, Long transactionId) {
        Account account = accountRepository.findAccountByItsNumber(accountNumber);
        if (account == null) {
            log.error("Account with number: {} not found", accountNumber);
            // TODO add reason of failure to the event?
            publisher.publishTransactionFailedEvent(transactionId);
            return null;
        }
        return account;
    }

    private void handleInsufficientFunds(Account account, Long transactionId) {
        log.error("Insufficient funds on account number: {}. Transaction id {} is failed.",
                account.getAccountNumber(), transactionId);
        // TODO add reason of failure to the event?
        publisher.publishTransactionFailedEvent(transactionId);
    }

    private void handleWrongTransactionType(Long transactionId) {
        log.error("Wrong transaction type for transaction id: {}", transactionId);
        // TODO add reason of failure to the event?
        publisher.publishTransactionFailedEvent(transactionId);
    }
}
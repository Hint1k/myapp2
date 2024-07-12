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
    public void updateAccountBalanceForCreatedTransaction(Long accountSourceNumber, Long accountDestinationNumber,
                                                          BigDecimal amount, Long transactionId,
                                                          TransactionType transactionType) {
        boolean hasProblems = false;
        Account sourceAccount = getAccountFromDatabase(accountSourceNumber, transactionId);
        if (sourceAccount != null) {
            if (!transactionType.equals(TransactionType.TRANSFER)) {
                BigDecimal newSourceBalance = calculateBalance(sourceAccount.getBalance(), amount, transactionType);
                hasProblems = hasBalanceProblems(newSourceBalance, sourceAccount, transactionId);
                if (hasProblems) {
                    return;
                }
                saveNewBalanceToDatabase(sourceAccount, newSourceBalance);
            } else {
                if (!isFundsTransferSuccessful(sourceAccount, accountDestinationNumber, amount, transactionId)) {
                    return;
                }
            }
        } else {
            return;
        }
        publisher.publishTransactionApprovedEvent(transactionId);
    }

    @Override
    @Transactional
    @Synchronized // TODO implement optimistic locking with JPA instead
//    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void updateAccountBalanceForUpdatedTransaction(Long accountSourceNumber, Long accountDestinationNumber,
                                                          BigDecimal oldAmount, BigDecimal newAmount,
                                                          Long transactionId, TransactionType oldTransactionType,
                                                          TransactionType newTransactionType) {
        boolean hasProblems = false;
        Account sourceAccount = getAccountFromDatabase(accountSourceNumber, transactionId);
        if (sourceAccount != null) {
            if (!oldTransactionType.equals(TransactionType.TRANSFER)) {
                // Reversing the account balance by adding negative value of the old amount
                BigDecimal oldBalance =
                        calculateBalance(sourceAccount.getBalance(), oldAmount.negate(), oldTransactionType);
                hasProblems = hasBalanceProblems(oldBalance, sourceAccount, transactionId);
                if (hasProblems) {
                    return;
                }
                // Saving the new amount on the account with reversed balance
                if (!newTransactionType.equals(TransactionType.TRANSFER)) {
                    BigDecimal newBalance = calculateBalance(oldBalance, newAmount, newTransactionType);
                    hasProblems = hasBalanceProblems(newBalance, sourceAccount, transactionId);
                    if (hasProblems) {
                        return;
                    }
                    saveNewBalanceToDatabase(sourceAccount, newBalance);
                } else {
                    if (!isFundsTransferSuccessful(sourceAccount, accountDestinationNumber, newAmount,
                            transactionId)) {
                        return;
                    }
                }
            } else {
                Account destinationAccount = getAccountFromDatabase(accountDestinationNumber, transactionId);
                if (destinationAccount != null) {
                    // Reversing the old transfer by rolling back the account balances
                    BigDecimal oldSourceBalance =
                            calculateBalance(sourceAccount.getBalance(), oldAmount.negate(), oldTransactionType);
                    hasProblems = hasBalanceProblems(oldSourceBalance, sourceAccount, transactionId);
                    if (hasProblems) {
                        return;
                    }
                    BigDecimal oldDestinationBalance =
                            calculateBalance(destinationAccount.getBalance(), oldAmount.negate(), oldTransactionType);
                    hasProblems = hasBalanceProblems(oldDestinationBalance, destinationAccount, transactionId);
                    if (hasProblems) {
                        return;
                    }
                    if (!newTransactionType.equals(TransactionType.TRANSFER)) {
                        // Saving new balance on source account (old transaction type is transfer)
                        BigDecimal newSourceBalance =
                                calculateBalance(oldSourceBalance, newAmount, newTransactionType);
                        hasProblems = hasBalanceProblems(newSourceBalance, sourceAccount, transactionId);
                        if (hasProblems) {
                            return;
                        }
                        saveNewBalanceToDatabase(sourceAccount, newSourceBalance);
                        // Reversing the old balance on destination account (old transaction type is transfer)
                        saveNewBalanceToDatabase(destinationAccount, oldDestinationBalance);
                    } else {
                        // Reversing the old balance on both accounts
                        saveNewBalanceToDatabase(sourceAccount, oldSourceBalance);
                        saveNewBalanceToDatabase(destinationAccount, oldDestinationBalance);
                        // Removing money from the source account
                        BigDecimal newSourceBalance =
                                calculateBalance(sourceAccount.getBalance(), newAmount, TransactionType.WITHDRAWAL);
                        hasProblems = hasBalanceProblems(newSourceBalance, sourceAccount, transactionId);
                        if (hasProblems) {
                            return;
                        }
                        // Adding money to the destination account
                        BigDecimal newDestinationBalance =
                                calculateBalance(destinationAccount.getBalance(), newAmount, TransactionType.DEPOSIT);
                        // Making a transfer between two accounts with reversed balances
                        saveNewBalanceToDatabase(sourceAccount, newSourceBalance);
                        saveNewBalanceToDatabase(destinationAccount, newDestinationBalance);
                    }
                } else {
                    return;
                }
            }
        } else {
            return;
        }
        publisher.publishTransactionApprovedEvent(transactionId);
    }

    private boolean isFundsTransferSuccessful(Account sourceAccount, Long accountDestinationNumber,
                                              BigDecimal amount, Long transactionId) {
        boolean hasProblems = false;
        Account destinationAccount = getAccountFromDatabase(accountDestinationNumber, transactionId);
        if (destinationAccount != null) {
            // removing money from the source account:
            BigDecimal newSourceBalance =
                    calculateBalance(sourceAccount.getBalance(), amount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(newSourceBalance, sourceAccount, transactionId);
            if (hasProblems) {
                return false;
            }
            // adding money to the destination account:
            BigDecimal newDestinationBalance =
                    calculateBalance(destinationAccount.getBalance(), amount, TransactionType.DEPOSIT);
            // Making a transfer between two accounts with reversed balances
            saveNewBalanceToDatabase(sourceAccount, newSourceBalance);
            saveNewBalanceToDatabase(destinationAccount, newDestinationBalance);
        } else {
            return false;
        }
        return true;
    }

    private BigDecimal calculateBalance(BigDecimal currentBalance, BigDecimal amount,
                                        TransactionType transactionType) {
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

    private boolean hasBalanceProblems(BigDecimal balance, Account account, Long transactionId) {
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
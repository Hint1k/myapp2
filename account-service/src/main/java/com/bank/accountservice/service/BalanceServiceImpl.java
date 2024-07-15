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
                                                          BigDecimal amount, TransactionType transactionType,
                                                          Long transactionId) {
        boolean hasProblems = false;
        Account sourceAccount = getAccountFromDatabase(accountSourceNumber, transactionId);
        if (sourceAccount != null) {
            if (!transactionType.equals(TransactionType.TRANSFER)) {
                // Saving new balance for the account
                BigDecimal newSourceBalance = calculateBalance(sourceAccount.getBalance(), amount, transactionType);
                hasProblems = hasBalanceProblems(newSourceBalance, sourceAccount, transactionId);
                if (hasProblems) {
                    return;
                }
                saveNewBalanceToDatabase(sourceAccount, newSourceBalance);
            } else {
                Account destinationAccount = getAccountFromDatabase(accountDestinationNumber, transactionId);
                if (destinationAccount != null) {
                    // Making a transfer between account
                    if (!isFundsTransferSuccessful(sourceAccount, destinationAccount, amount, transactionId)) {
                        return;
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

    @Override
    @Transactional
    @Synchronized // TODO implement optimistic locking with JPA instead
//    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void updateAccountBalanceForUpdatedTransaction(Long oldAccountSourceNumber, Long newAccountSourceNumber,
                                                          Long oldAccountDestinationNumber,
                                                          Long newAccountDestinationNumber, BigDecimal oldAmount,
                                                          BigDecimal newAmount, TransactionType oldTransactionType,
                                                          TransactionType newTransactionType, Long transactionId) {
        // TODO implement a design pattern for this method instead of current code. Probably strategy pattern.
        boolean hasProblems = false;
        boolean isSourceAccountChanged = !Objects.equals(oldAccountSourceNumber, newAccountSourceNumber);
        boolean isDestinationAccountChanged = !Objects.equals(oldAccountDestinationNumber, newAccountDestinationNumber);
        boolean isOldTransactionATransfer = Objects.equals(oldTransactionType, TransactionType.TRANSFER);
        boolean isNewTransactionATransfer = Objects.equals(newTransactionType, TransactionType.TRANSFER);

        if (isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged &&
                !isDestinationAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            Account oldDestinationAccount = getAccountFromDatabase(oldAccountDestinationNumber, transactionId);
            if (oldDestinationAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal oldDestinationBalance =
                    calculateBalance(oldDestinationAccount.getBalance(), oldAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(oldDestinationBalance, oldDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance = calculateBalance(oldSourceBalance, newAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(newSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newDestinationBalance =
                    calculateBalance(oldDestinationBalance, newAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(newDestinationBalance, oldDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, newSourceBalance);
            saveNewBalanceToDatabase(oldDestinationAccount, newDestinationBalance);
        }
        if (!isOldTransactionATransfer && !isNewTransactionATransfer && !isSourceAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount.negate(), oldTransactionType);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance = calculateBalance(oldSourceBalance, newAmount, newTransactionType);
            hasProblems = hasBalanceProblems(newSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, newSourceBalance);
        }
        if (!isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            Account newDestinationAccount = getAccountFromDatabase(newAccountDestinationNumber, transactionId);
            if (newDestinationAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount.negate(), oldTransactionType);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance = calculateBalance(oldSourceBalance, newAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(newSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newDestinationBalance =
                    calculateBalance(newDestinationAccount.getBalance(), newAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(newDestinationBalance, newDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, newSourceBalance);
            saveNewBalanceToDatabase(newDestinationAccount, newDestinationBalance);
        }
        if (isOldTransactionATransfer && !isNewTransactionATransfer && !isSourceAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            Account oldDestinationAccount = getAccountFromDatabase(oldAccountDestinationNumber, transactionId);
            if (oldDestinationAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal oldDestinationBalance =
                    calculateBalance(oldDestinationAccount.getBalance(), oldAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(oldDestinationBalance, oldDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance = calculateBalance(oldSourceBalance, newAmount, newTransactionType);
            hasProblems = hasBalanceProblems(newSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, newSourceBalance);
            saveNewBalanceToDatabase(oldDestinationAccount, oldDestinationBalance);
        }
        if (isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged &&
                isDestinationAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            Account oldDestinationAccount = getAccountFromDatabase(oldAccountDestinationNumber, transactionId);
            if (oldDestinationAccount == null) {
                return;
            }
            Account newDestinationAccount = getAccountFromDatabase(newAccountDestinationNumber, transactionId);
            if (newDestinationAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal oldDestinationBalance =
                    calculateBalance(oldDestinationAccount.getBalance(), oldAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(oldDestinationBalance, oldDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance = calculateBalance(oldSourceBalance, newAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(newSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newDestinationBalance =
                    calculateBalance(newDestinationAccount.getBalance(), newAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(newDestinationBalance, newDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, newSourceBalance);
            saveNewBalanceToDatabase(oldDestinationAccount, oldDestinationBalance);
            saveNewBalanceToDatabase(newDestinationAccount, newDestinationBalance);
        }
        if (isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                isDestinationAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            Account oldDestinationAccount = getAccountFromDatabase(oldAccountDestinationNumber, transactionId);
            if (oldDestinationAccount == null) {
                return;
            }
            Account newSourceAccount = getAccountFromDatabase(newAccountSourceNumber, transactionId);
            if (newSourceAccount == null) {
                return;
            }
            Account newDestinationAccount = getAccountFromDatabase(newAccountDestinationNumber, transactionId);
            if (newDestinationAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal oldDestinationBalance =
                    calculateBalance(oldDestinationAccount.getBalance(), oldAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(oldDestinationBalance, oldDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance =
                    calculateBalance(newSourceAccount.getBalance(), newAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(newSourceBalance, newSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newDestinationBalance =
                    calculateBalance(newDestinationAccount.getBalance(), newAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(newDestinationBalance, newDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, oldSourceBalance);
            saveNewBalanceToDatabase(newSourceAccount, newSourceBalance);
            saveNewBalanceToDatabase(oldDestinationAccount, oldDestinationBalance);
            saveNewBalanceToDatabase(newDestinationAccount, newDestinationBalance);
        }
        if (!isOldTransactionATransfer && !isNewTransactionATransfer && isSourceAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            Account newSourceAccount = getAccountFromDatabase(newAccountSourceNumber, transactionId);
            if (newSourceAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount.negate(), oldTransactionType);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance =
                    calculateBalance(newSourceAccount.getBalance(), newAmount, newTransactionType);
            hasProblems = hasBalanceProblems(newSourceBalance, newSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, oldSourceBalance);
            saveNewBalanceToDatabase(newSourceAccount, newSourceBalance);
        }
        if (!isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            Account newSourceAccount = getAccountFromDatabase(newAccountSourceNumber, transactionId);
            if (newSourceAccount == null) {
                return;
            }
            Account newDestinationAccount = getAccountFromDatabase(newAccountDestinationNumber, transactionId);
            if (newDestinationAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount.negate(), oldTransactionType);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance =
                    calculateBalance(newSourceAccount.getBalance(), newAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(newSourceBalance, newSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newDestinationBalance =
                    calculateBalance(newDestinationAccount.getBalance(), newAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(newDestinationBalance, newDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, oldSourceBalance);
            saveNewBalanceToDatabase(newSourceAccount, newSourceBalance);
            saveNewBalanceToDatabase(newDestinationAccount, newDestinationBalance);
        }
        if (isOldTransactionATransfer && !isNewTransactionATransfer && isSourceAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            Account oldDestinationAccount = getAccountFromDatabase(oldAccountDestinationNumber, transactionId);
            if (oldDestinationAccount == null) {
                return;
            }
            Account newSourceAccount = getAccountFromDatabase(newAccountSourceNumber, transactionId);
            if (newSourceAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal oldDestinationBalance =
                    calculateBalance(oldDestinationAccount.getBalance(), oldAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(oldDestinationBalance, oldDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance =
                    calculateBalance(newSourceAccount.getBalance(), newAmount, newTransactionType);
            hasProblems = hasBalanceProblems(newSourceBalance, newSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, oldSourceBalance);
            saveNewBalanceToDatabase(newSourceAccount, newSourceBalance);
            saveNewBalanceToDatabase(oldDestinationAccount, oldDestinationBalance);
        }
        if (isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                !isDestinationAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            Account oldDestinationAccount = getAccountFromDatabase(oldAccountDestinationNumber, transactionId);
            if (oldDestinationAccount == null) {
                return;
            }
            Account newSourceAccount = getAccountFromDatabase(newAccountSourceNumber, transactionId);
            if (newSourceAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance =
                    calculateBalance(newSourceAccount.getBalance(), newAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(newSourceBalance, newSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal oldDestinationBalance =
                    calculateBalance(oldDestinationAccount.getBalance(), oldAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(oldDestinationBalance, oldDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newDestinationBalance =
                    calculateBalance(oldDestinationBalance, newAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(newDestinationBalance, oldDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, oldSourceBalance);
            saveNewBalanceToDatabase(newSourceAccount, newSourceBalance);
            saveNewBalanceToDatabase(oldDestinationAccount, newDestinationBalance);
        }
        if (!isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                !isDestinationAccountChanged) {
            Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
            if (oldSourceAccount == null) {
                return;
            }
            Account newSourceAccount = getAccountFromDatabase(newAccountSourceNumber, transactionId);
            if (newSourceAccount == null) {
                return;
            }
            Account newDestinationAccount = getAccountFromDatabase(newAccountDestinationNumber, transactionId);
            if (newDestinationAccount == null) {
                return;
            }
            BigDecimal oldSourceBalance =
                    calculateBalance(oldSourceAccount.getBalance(), oldAmount.negate(), oldTransactionType);
            hasProblems = hasBalanceProblems(oldSourceBalance, oldSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newSourceBalance =
                    calculateBalance(newSourceAccount.getBalance(), newAmount, TransactionType.WITHDRAWAL);
            hasProblems = hasBalanceProblems(newSourceBalance, newSourceAccount, transactionId);
            if (hasProblems) {
                return;
            }
            BigDecimal newDestinationBalance =
                    calculateBalance(newDestinationAccount.getBalance(), newAmount, TransactionType.DEPOSIT);
            hasProblems = hasBalanceProblems(newDestinationBalance, newDestinationAccount, transactionId);
            if (hasProblems) {
                return;
            }
            saveNewBalanceToDatabase(oldSourceAccount, oldSourceBalance);
            saveNewBalanceToDatabase(newSourceAccount, newSourceBalance);
            saveNewBalanceToDatabase(newDestinationAccount, newDestinationBalance);
        }
        publisher.publishTransactionApprovedEvent(transactionId);
    }

    private boolean isFundsTransferSuccessful(Account sourceAccount, Account destinationAccount,
                                              BigDecimal amount, Long transactionId) {
        // Removing money from the source account:
        BigDecimal newSourceBalance =
                calculateBalance(sourceAccount.getBalance(), amount, TransactionType.WITHDRAWAL);
        boolean hasProblems = hasBalanceProblems(newSourceBalance, sourceAccount, transactionId);
        if (hasProblems) {
            return false;
        }
        // Adding money to the destination account:
        BigDecimal newDestinationBalance =
                calculateBalance(destinationAccount.getBalance(), amount, TransactionType.DEPOSIT);
        // Making a transfer between two accounts by saving new balances
        saveNewBalanceToDatabase(sourceAccount, newSourceBalance);
        saveNewBalanceToDatabase(destinationAccount, newDestinationBalance);
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
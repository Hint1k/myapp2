package com.bank.accountservice.service;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.publisher.TransactionEventPublisher;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.service.strategy.Balance;
import com.bank.accountservice.service.strategy.TransactionContext;
import com.bank.accountservice.service.strategy.TransferToTransferSameAccountsStrategy;
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
        boolean isTransferMade;
        boolean isBalanceChanged;
        Account sourceAccount = getAccountFromDatabase(accountSourceNumber, transactionId);
        if (sourceAccount != null) {
            if (transactionType.equals(TransactionType.TRANSFER)) {
                Account destinationAccount = getAccountFromDatabase(accountDestinationNumber, transactionId);
                if (destinationAccount != null) {
                    isTransferMade = makeTransfer(sourceAccount, destinationAccount, amount, transactionId);
                    if (!isTransferMade) {
                        return;
                    }
                } else {
                    return;
                }
            } else {
                isBalanceChanged = changeBalance(sourceAccount, amount, transactionType, transactionId);
                if (!isBalanceChanged) {
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
        boolean isTransferReversed;
        boolean isTransferMade;
        boolean isBalanceReversed;
        boolean isBalanceChanged;

        boolean isSourceAccountChanged = !Objects.equals(oldAccountSourceNumber, newAccountSourceNumber);
        boolean isDestinationAccountChanged =
                !Objects.equals(oldAccountDestinationNumber, newAccountDestinationNumber);
        boolean isOldTransactionATransfer = Objects.equals(oldTransactionType, TransactionType.TRANSFER);
        boolean isNewTransactionATransfer = Objects.equals(newTransactionType, TransactionType.TRANSFER);

        Account oldSourceAccount = getAccountFromDatabase(oldAccountSourceNumber, transactionId);
        Account oldDestinationAccount = getAccountFromDatabase(oldAccountDestinationNumber, transactionId);
        Account newSourceAccount = getAccountFromDatabase(newAccountSourceNumber, transactionId);
        Account newDestinationAccount = getAccountFromDatabase(newAccountDestinationNumber, transactionId);

        if (isNullAccount(oldSourceAccount, oldAccountSourceNumber, transactionId)) {
            return;
        }

//        TransactionContext context = new TransactionContext();

        if (isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged &&
                !isDestinationAccountChanged) {
            if (isNullAccount(oldDestinationAccount, oldAccountDestinationNumber, transactionId)) {
                return;
            }
            isTransferReversed = reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId);
            if (!isTransferReversed) {
                return;
            }
            isTransferMade = makeTransfer(oldSourceAccount, oldDestinationAccount, newAmount, transactionId);
            if (!isTransferMade) {
                return;
            }
//            context.setStrategy(new TransferToTransferSameAccountsStrategy());
        }
        if (!isOldTransactionATransfer && !isNewTransactionATransfer && !isSourceAccountChanged) {
            isBalanceReversed = reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId);
            if (!isBalanceReversed) {
                return;
            }
            isBalanceChanged = changeBalance(oldSourceAccount, newAmount, newTransactionType, transactionId);
            if (!isBalanceChanged) {
                return;
            }
        }
        if (!isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged) {
            if (isNullAccount(newDestinationAccount, newAccountDestinationNumber, transactionId)) {
                return;
            }
            isBalanceReversed = reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId);
            if (!isBalanceReversed) {
                return;
            }
            isTransferMade = makeTransfer(oldSourceAccount, newDestinationAccount, newAmount, transactionId);
            if (!isTransferMade) {
                return;
            }
        }
        if (isOldTransactionATransfer && !isNewTransactionATransfer && !isSourceAccountChanged) {
            if (isNullAccount(oldDestinationAccount, oldAccountDestinationNumber, transactionId)) {
                return;
            }
            isTransferReversed = reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId);
            if (!isTransferReversed) {
                return;
            }
            isBalanceChanged = changeBalance(oldSourceAccount, newAmount, newTransactionType, transactionId);
            if (!isBalanceChanged) {
                return;
            }
        }
        if (isOldTransactionATransfer && isNewTransactionATransfer && !isSourceAccountChanged &&
                isDestinationAccountChanged) {
            if (isNullAccount(oldDestinationAccount, oldAccountDestinationNumber, transactionId) ||
                    isNullAccount(newDestinationAccount, newAccountDestinationNumber, transactionId)) {
                return;
            }
            isTransferReversed = reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId);
            if (!isTransferReversed) {
                return;
            }
            isTransferMade = makeTransfer(oldSourceAccount, newDestinationAccount, newAmount, transactionId);
            if (!isTransferMade) {
                return;
            }
        }
        if (isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                isDestinationAccountChanged) {
            if (isNullAccount(newSourceAccount, newAccountSourceNumber, transactionId) ||
                    isNullAccount(oldDestinationAccount, oldAccountDestinationNumber, transactionId) ||
                    isNullAccount(newDestinationAccount, newAccountDestinationNumber, transactionId)) {
                return;
            }
            isTransferReversed = reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId);
            if (!isTransferReversed) {
                return;
            }
            isTransferMade = makeTransfer(newSourceAccount, newDestinationAccount, newAmount, transactionId);
            if (!isTransferMade) {
                return;
            }
        }
        if (!isOldTransactionATransfer && !isNewTransactionATransfer && isSourceAccountChanged) {
            if (isNullAccount(newSourceAccount, newAccountSourceNumber, transactionId)) {
                return;
            }
            isBalanceReversed = reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId);
            if (!isBalanceReversed) {
                return;
            }
            isBalanceChanged = changeBalance(newSourceAccount, newAmount, newTransactionType, transactionId);
            if (!isBalanceChanged) {
                return;
            }
        }
        if (!isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged) {
            if (isNullAccount(newSourceAccount, newAccountSourceNumber, transactionId) ||
                    isNullAccount(newDestinationAccount, newAccountDestinationNumber, transactionId)) {
                return;
            }
            isBalanceReversed = reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId);
            if (!isBalanceReversed) {
                return;
            }
            isTransferMade = makeTransfer(newSourceAccount, newDestinationAccount, newAmount, transactionId);
            if (!isTransferMade) {
                return;
            }
        }
        if (isOldTransactionATransfer && !isNewTransactionATransfer && isSourceAccountChanged) {
            if (isNullAccount(newSourceAccount, newAccountSourceNumber, transactionId) ||
                    isNullAccount(oldDestinationAccount, oldAccountDestinationNumber, transactionId)) {
                return;
            }
            isTransferReversed = reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId);
            if (!isTransferReversed) {
                return;
            }
            isBalanceChanged = changeBalance(newSourceAccount, newAmount, newTransactionType, transactionId);
            if (!isBalanceChanged) {
                return;
            }
        }
        if (isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                !isDestinationAccountChanged) {
            if (isNullAccount(newSourceAccount, newAccountSourceNumber, transactionId) ||
                    isNullAccount(oldDestinationAccount, oldAccountDestinationNumber, transactionId)) {
                return;
            }
            isTransferReversed = reverseTransfer(oldSourceAccount, oldDestinationAccount, oldAmount, transactionId);
            if (!isTransferReversed) {
                return;
            }
            isTransferMade = makeTransfer(newSourceAccount, oldDestinationAccount, newAmount, transactionId);
            if (!isTransferMade) {
                return;
            }
        }
        if (!isOldTransactionATransfer && isNewTransactionATransfer && isSourceAccountChanged &&
                !isDestinationAccountChanged) {
            if (isNullAccount(newSourceAccount, newAccountSourceNumber, transactionId) ||
                    isNullAccount(newDestinationAccount, newAccountDestinationNumber, transactionId)) {
                return;
            }
            isBalanceReversed = reverseBalance(oldSourceAccount, oldAmount, oldTransactionType, transactionId);
            if (!isBalanceReversed) {
                return;
            }
            isTransferMade = makeTransfer(newSourceAccount, newDestinationAccount, newAmount, transactionId);
            if (!isTransferMade) {
                return;
            }
        }

//        context.executeStrategy(oldAccountSourceNumber, newAccountSourceNumber,
//                oldAccountDestinationNumber, newAccountDestinationNumber,
//                oldAmount, newAmount, oldTransactionType, newTransactionType,
//                transactionId);

        publisher.publishTransactionApprovedEvent(transactionId);
    }

    private boolean changeBalance(Account account, BigDecimal amount, TransactionType transactionType,
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

    private boolean reverseBalance(Account account, BigDecimal amount, TransactionType transactionType,
                                   Long transactionId) {
        boolean isBalanceChanged = changeBalance(account, amount.negate(), transactionType, transactionId);
        if (!isBalanceChanged) {
            return false;
        }
        return true;
    }

    private boolean makeTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount,
                                 Long transactionId) {
        boolean isBalanceChanged;
        isBalanceChanged = changeBalance(sourceAccount, amount, TransactionType.WITHDRAWAL, transactionId);
        if (!isBalanceChanged) {
            return false;
        }
        isBalanceChanged = changeBalance(destinationAccount, amount, TransactionType.DEPOSIT, transactionId);
        if (!isBalanceChanged) {
            return false;
        }
        return true;
    }

    private boolean reverseTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount,
                                    Long transactionId) {
        boolean isTransferMade = makeTransfer(destinationAccount, sourceAccount, amount, transactionId);
        if (!isTransferMade) {
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

    private boolean isNullAccount(Account account, Long accountNumber, Long transactionId) {
        if (Objects.isNull(account)) {
            log.error("Account with number: {} not found", accountNumber);
            // TODO add reason of failure to the event?
            publisher.publishTransactionFailedEvent(transactionId);
            return true;
        }
        return false;
    }

    private void saveBalanceToDatabase(Account account, BigDecimal newBalance) {
        account.setBalance(newBalance);
        accountRepository.save(account);
        log.info("Updated balance for account number: {}", account.getAccountNumber());
    }

    private Account getAccountFromDatabase(Long accountNumber, Long transactionId) {
        Account account = accountRepository.findAccountByItsNumber(accountNumber);
        if (account == null) {
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
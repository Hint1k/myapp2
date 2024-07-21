package com.bank.webservice.validation;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.TransactionType;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.Objects;

@Component
public class TransactionValidator {

    private final AccountCache cache;

    @Autowired
    public TransactionValidator(AccountCache cache) {
        this.cache = cache;
    }

    public void validateTransaction(Transaction transaction, BindingResult bindingResult) {
        // Validating source account number
        String sourceAccountNumberString;
        try {
            sourceAccountNumberString = transaction.getAccountSourceNumber().toString();
        } catch (IllegalArgumentException | NullPointerException | TypeMismatchException e) {
            bindingResult.rejectValue("accountSourceNumber", "error.transaction",
                    "Account number containing only digits is required for transaction");
            return;
        }
        long sourceAccountNumber = Long.parseLong(sourceAccountNumberString);
        if (sourceAccountNumber < 1) {
            bindingResult.rejectValue("accountSourceNumber", "error.transaction",
                    "Account number has to be 1 or greater");
            return;
        }
        Account sourceAccount = cache.getAccountFromCacheByAccountNumber(sourceAccountNumber);
        if (sourceAccount != null) {
            AccountStatus sourceAccountStatus = sourceAccount.getAccountStatus();
            if (!sourceAccountStatus.equals(AccountStatus.ACTIVE)) {
                bindingResult.rejectValue("accountSourceNumber", "error.transaction",
                        "This account is not active. Try another account");
                return;
            }
        } else {
            bindingResult.rejectValue("accountSourceNumber", "error.transaction",
                    "This account does not exist. Try another account");
            return;
        }

        // Validating destination account number if the transaction type is TRANSFER
        if (transaction.getTransactionType().equals(TransactionType.TRANSFER)) {
            String destinationAccountNumberString;
            try {
                destinationAccountNumberString = transaction.getAccountDestinationNumber().toString();
            } catch (IllegalArgumentException | NullPointerException | TypeMismatchException e) {
                bindingResult.rejectValue("accountDestinationNumber", "error.transaction",
                        "Account number containing only digits is required for transaction");
                return;
            }
            long destinationAccountNumber = Long.parseLong(destinationAccountNumberString);
            if (destinationAccountNumber < 1) {
                bindingResult.rejectValue("accountDestinationNumber", "error.transaction",
                        "Account number has to be 1 or greater");
                return;
            }
            if (Objects.equals(sourceAccountNumber, destinationAccountNumber)) {
                bindingResult.rejectValue("accountDestinationNumber", "error.transaction",
                        "Two account numbers must be different");
                return;
            }
            Account destinationAccount = cache.getAccountFromCacheByAccountNumber(destinationAccountNumber);
            if (destinationAccount != null) {
                AccountStatus destinationAccountStatus = destinationAccount.getAccountStatus();
                if (!destinationAccountStatus.equals(AccountStatus.ACTIVE)) {
                    bindingResult.rejectValue("accountDestinationNumber", "error.transaction",
                            "This account is not active. Try another account");
                }
            } else {
                bindingResult.rejectValue("accountDestinationNumber", "error.transaction",
                        "This account does not exist. Try another account");
            }
        }
    }
}
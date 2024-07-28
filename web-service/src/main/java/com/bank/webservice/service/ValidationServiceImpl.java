package com.bank.webservice.service;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.TransactionType;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Objects;

@Service
public class ValidationServiceImpl implements ValidationService {

    private final AccountCache accountCache;
    private final CustomerCache customerCache;

    @Autowired
    public ValidationServiceImpl(AccountCache accountCache, CustomerCache customerCache) {
        this.accountCache = accountCache;
        this.customerCache = customerCache;
    }

    @Override
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
        Account sourceAccount = accountCache.getAccountFromCacheByAccountNumber(sourceAccountNumber);
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
            Account destinationAccount = accountCache.getAccountFromCacheByAccountNumber(destinationAccountNumber);
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

    @Override
    public void validateCustomer(Customer newCustomer, BindingResult bindingResult) {
        List<Customer> customers = customerCache.getAllCustomersFromCache();
        boolean customerExists = customers.stream()
                .anyMatch(customer -> customer.getCustomerNumber().equals(newCustomer.getCustomerNumber()));
        if (customerExists) {
            bindingResult.rejectValue("customerNumber", "error.customer",
                    "Customer with the such number already exist.");
        }
        // TODO add validation for other customer fields later
    }

    @Override
    public void validateCustomerExists(Account newAccount, BindingResult bindingResult) {
        List<Customer> customers = customerCache.getAllCustomersFromCache();
        boolean customerExists = customers.stream()
                .anyMatch(customer -> customer.getCustomerNumber().equals(newAccount.getCustomerNumber()));
        if (!customerExists) {
            bindingResult.rejectValue("customerNumber", "error.account",
                    "Customer with the such number does not exist.");
        }
    }

    @Override
    public void validateAccountIsNotExist(Account newAccount, BindingResult bindingResult){
        List<Account> accounts = accountCache.getAllAccountsFromCache();
        boolean accountExists = accounts.stream()
                .anyMatch(account -> account.getAccountNumber().equals(newAccount.getAccountNumber()));
        if (accountExists) {
            bindingResult.rejectValue("accountNumber", "error.account",
                    "Account with the same number already exists.");
        }
    }

    @Override
    public void validateMultipleAccountsBelongToCustomer(Customer oldCustomer, BindingResult bindingResult) {
        // TODO validate later that accounts numbers are a valid numbers.
        List<Account> accounts = accountCache.getAllAccountsFromCache();
        List<Account> matchingAccounts = accounts.stream()
                .filter(account -> oldCustomer.getAccountNumbers().contains(account.getAccountNumber()))
                .toList();
        for (Account account : matchingAccounts) {
            bindingResult.rejectValue("accountNumbers", "error.customer",
                    "Account number " + account.getAccountNumber() + " already belong to this customer.");
        }
    }
}
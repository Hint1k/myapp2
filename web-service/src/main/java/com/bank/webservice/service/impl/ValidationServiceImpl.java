package com.bank.webservice.service.impl;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.service.ValidationService;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.TypeMismatchException;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final AccountCache accountCache;
    private final CustomerCache customerCache;

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
    public void validateAccountIsNotExist(Account newAccount, BindingResult bindingResult) {
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
        String accountNumbersString = oldCustomer.getAccountNumbers();

        // Checking if customer has no accounts, it is allowed to have none
        if (accountNumbersString == null || accountNumbersString.isEmpty()) {
            return;
        }

        // Normalizing user input of account numbers
        // Step 1: Remove all spaces
        accountNumbersString = accountNumbersString.replaceAll("\\s+", "");
        // Step 2: Replace multiple commas with a single comma
        accountNumbersString = accountNumbersString.replaceAll(",+", ",");
        // Step 3: Remove leading and trailing commas
        accountNumbersString = accountNumbersString.replaceAll("^,|,$", "");
        // Now accountNumbersString should be in the desired format
        oldCustomer.setAccountNumbers(accountNumbersString);
        // Checking if string contains only digits divided by comma
        List<Long> accountNumbersList = new ArrayList<>();

        String[] accountNumbersArray = accountNumbersString.split(",");
        Pattern digitPattern = Pattern.compile("^\\d+$");
        for (String accountNumber : accountNumbersArray) {
            if (!digitPattern.matcher(accountNumber).matches()) {
                bindingResult.rejectValue("accountNumbers", "error.customer",
                        "Account numbers must be digits divided by comma or space."
                                + "No any other symbol is allowed");
                break;
            } else {
                accountNumbersList.add(Long.parseLong(accountNumber));
            }
        }

        // Checking if accounts exist
        List<Account> accounts = accountCache.getAllAccountsFromCache();
        List<Long> nonExistingAccountNumbers = accountNumbersList.stream()
                .filter(number -> accounts.stream()
                        .noneMatch(account -> account.getAccountNumber().equals(number)))
                .toList();
        if (!nonExistingAccountNumbers.isEmpty()) {
            StringBuilder errorMessageBuilder = new StringBuilder("Following account numbers do not exist: ");
            for (Long nonExistingAccountNumber : nonExistingAccountNumbers) {
                errorMessageBuilder.append(nonExistingAccountNumber).append(", ");
            }
            String errorMessage = errorMessageBuilder.toString();
            errorMessage = errorMessage.substring(0, errorMessage.length() - 2);  // Remove last comma and space
            bindingResult.rejectValue("accountNumbers", "error.customer", errorMessage);
        }

        // Checking if account numbers already belong to another customer
        List<Account> matchingAccounts = accounts.stream()
                .filter(account -> accountNumbersList.contains(account.getAccountNumber()))
                .filter(account -> {
                    String customerNumber = String.valueOf(account.getCustomerNumber());
                    return customerNumber != null && !customerNumber.isEmpty()
                            && !customerNumber.equals(String.valueOf(oldCustomer.getCustomerNumber()));
                })
                .toList();
        if (!matchingAccounts.isEmpty()) {
            StringBuilder errorMessageBuilder =
                    new StringBuilder("Following account numbers already belong to another customer: ");
            boolean hasErrors = false;
            for (Account matchingAccount : matchingAccounts) {
                if (!matchingAccount.getCustomerNumber().equals(0L)) {
                    errorMessageBuilder.append(matchingAccount.getAccountNumber()).append(", ");
                    hasErrors = true;
                } // no errors if customer number = 0, since it means no customer assigned.
            }
            if (hasErrors) {
                String errorMessage = errorMessageBuilder.toString().trim();
                errorMessage = errorMessage.replaceAll(",$", ""); // removing last commas
                bindingResult.rejectValue("accountNumbers", "error.customer", errorMessage);
            }
        }
    }
}
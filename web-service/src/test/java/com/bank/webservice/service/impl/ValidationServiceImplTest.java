package com.bank.webservice.service.impl;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidationServiceImplTest {

    @Mock
    private AccountCache accountCache;

    @Mock
    private CustomerCache customerCache;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ValidationServiceImpl validationService;

    @Test
    public void testRejectInvalidSourceAccountNumber() {
        // Given: A transaction with a null source account number
        Transaction transaction = createTransactionWithSourceAndDestination(null, 2L);

        // When: Validating the transaction
        validationService.validateTransaction(transaction, bindingResult);

        // Then: Binding result should reject the value
        verify(bindingResult).rejectValue("accountSourceNumber", "error.transaction",
                "Account number containing only digits is required for transaction");
    }

    @Test
    public void testRejectInvalidSourceAccountNumberFormat() {
        // Given: A transaction with an invalid source account number format
        Transaction transaction = createTransactionWithSourceAndDestination(-1L, null);

        // When: Validating the transaction
        validationService.validateTransaction(transaction, bindingResult);

        // Then: Binding result should reject the value
        verify(bindingResult).rejectValue("accountSourceNumber", "error.transaction",
                "Account number has to be 1 or greater");
    }

    @Test
    public void testRejectNonExistentSourceAccount() {
        // Given: A valid source account number, but the account does not exist in cache
        Transaction transaction = createTransactionWithSourceAndDestination(1L, null);
        when(accountCache.getAccountFromCacheByAccountNumber(1L)).thenReturn(null);

        // When: Validating the transaction
        validationService.validateTransaction(transaction, bindingResult);

        // Then: Binding result should reject the value
        verify(bindingResult).rejectValue("accountSourceNumber", "error.transaction",
                "This account does not exist. Try another account");
    }

    @Test
    public void testRejectInactiveSourceAccount() {
        // Given: A source account that exists but is not active
        Account inactiveAccount = createAccountWithStatus(AccountStatus.INACTIVE);
        Transaction transaction = createTransactionWithSourceAndDestination(1L, null);

        when(accountCache.getAccountFromCacheByAccountNumber(1L)).thenReturn(inactiveAccount);

        // When: Validating the transaction
        validationService.validateTransaction(transaction, bindingResult);

        // Then: Binding result should reject the value
        verify(bindingResult).rejectValue("accountSourceNumber", "error.transaction",
                "This account is not active. Try another account");
    }

    @Test
    public void testRejectTransferWhenSourceAndDestinationAreSame() {
        // Given: A transfer transaction where source and destination account numbers are the same
        Transaction transaction = createTransactionWithSourceAndDestination(2L, 2L);

        // Mocking: The source and destination accounts exist and are active
        Account account = createAccountWithStatus(AccountStatus.ACTIVE);
        when(accountCache.getAccountFromCacheByAccountNumber(2L)).thenReturn(account);

        // When: Validating the transaction
        validationService.validateTransaction(transaction, bindingResult);

        // Then: Binding result should reject the destination account number
        verify(bindingResult).rejectValue("accountDestinationNumber", "error.transaction",
                "Two account numbers must be different");
        verifyNoMoreInteractions(bindingResult);
    }

    @Test
    public void testRejectNonExistentDestinationAccountForTransfer() {
        // Given: A transfer transaction with a non-existent destination account
        Transaction transaction = createTransactionWithSourceAndDestination(1L, 2L);
        when(accountCache.getAccountFromCacheByAccountNumber(1L))
                .thenReturn(createAccountWithStatus(AccountStatus.ACTIVE));
        when(accountCache.getAccountFromCacheByAccountNumber(2L)).thenReturn(null);

        // When: Validating the transaction
        validationService.validateTransaction(transaction, bindingResult);

        // Then: Binding result should reject the value
        verify(bindingResult).rejectValue("accountDestinationNumber", "error.transaction",
                "This account does not exist. Try another account");
    }

    @Test
    public void testRejectExistingCustomer() {
        // Given: A customer that already exists in cache
        Customer newCustomer = createCustomerWithMultipleAccounts();
        when(customerCache.getAllCustomersFromCache()).thenReturn(List.of(newCustomer));

        // When: Validating the customer
        validationService.validateCustomer(newCustomer, bindingResult);

        // Then: Binding result should reject the value
        verify(bindingResult).rejectValue("customerNumber", "error.customer",
                "Customer with the such number already exist.");
    }

    @Test
    public void testRejectNonExistentCustomerForAccount() {
        // Given: A new account with a customer number that does not exist in cache
        Account newAccount = createAccountWithNumberAndCustomer(1L, 2L);

        when(customerCache.getAllCustomersFromCache()).thenReturn(List.of());

        // When: Validating customer existence for account
        validationService.validateCustomerExists(newAccount, bindingResult);

        // Then: Binding result should reject the value
        verify(bindingResult).rejectValue("customerNumber", "error.account",
                "Customer with the such number does not exist.");
    }

    @Test
    public void testRejectDuplicateAccountNumber() {
        // Given: A new account with an account number that already exists
        Account newAccount = createAccountWithNumberAndCustomer(2L, 1L);
        Account existingAccount = createAccountWithNumberAndCustomer(2L, 1L);

        when(accountCache.getAllAccountsFromCache()).thenReturn(List.of(existingAccount));

        // When: Validating duplicate account numbers
        validationService.validateAccountIsNotExist(newAccount, bindingResult);

        // Then: Binding result should reject the value
        verify(bindingResult).rejectValue("accountNumber", "error.account",
                "Account with the same number already exists.");
    }

    @Test
    public void testValidCustomerNumber() {
        // Given: A new customer with a valid customer number
        Customer newCustomer = createCustomerWithMultipleAccounts();

        // Mocking: The customer does not exist in cache
        when(customerCache.getAllCustomersFromCache()).thenReturn(Collections.emptyList());

        // When: Validating the customer
        validationService.validateCustomer(newCustomer, bindingResult);

        // Then: No validation errors should occur
        verify(bindingResult, never()).rejectValue(eq("customerNumber"), anyString(), anyString());
    }

    @Test
    public void testRejectAccountsBelongingToAnotherCustomer() {
        // Given: A customer trying to add accounts that belong to another customer
        Customer oldCustomer = createCustomerWithMultipleAccounts();
        oldCustomer.setAccountNumbers("1,2");

        // Create accounts with different customers (simulating accounts belonging to other customers)
        Account account1 = createAccountWithNumberAndCustomer(1L, 2L);
        Account account2 = createAccountWithNumberAndCustomer(2L, 3L);

        when(accountCache.getAllAccountsFromCache()).thenReturn(List.of(account1, account2));

        // When: Validating multiple accounts belonging to another customer
        validationService.validateMultipleAccountsBelongToCustomer(oldCustomer, bindingResult);

        // Then: Binding result should reject the value
        verify(bindingResult).rejectValue("accountNumbers", "error.customer",
                "Following account numbers already belong to another customer: 1, 2");
    }

    @Test
    public void testRejectNonExistingAccountsForMultipleAccounts() {
        // Given: A customer trying to add accounts that do not exist
        Customer oldCustomer = createCustomerWithMultipleAccounts();
        oldCustomer.setAccountNumbers("2,1");

        when(accountCache.getAllAccountsFromCache()).thenReturn(Collections.emptyList());

        // When: Validating multiple accounts
        validationService.validateMultipleAccountsBelongToCustomer(oldCustomer, bindingResult);

        // Then: Binding result should reject the value
        verify(bindingResult).rejectValue("accountNumbers", "error.customer",
                "Following account numbers do not exist: 2, 1");
    }

    @Test
    public void testValidAccountNumbersForMultipleAccounts() {
        // Given: A customer adding valid account numbers
        Customer oldCustomer = createCustomerWithMultipleAccounts();
        oldCustomer.setAccountNumbers("1,2");

        Account account1 = createAccountWithNumberAndCustomer(1L, 1L);
        Account account2 = createAccountWithNumberAndCustomer(2L, 1L);

        when(accountCache.getAllAccountsFromCache()).thenReturn(List.of(account1, account2));

        // When: Validating multiple accounts belonging to the customer
        validationService.validateMultipleAccountsBelongToCustomer(oldCustomer, bindingResult);

        // Then: No binding result errors
        verify(bindingResult, never()).rejectValue(eq("accountNumbers"), anyString(), anyString());
    }

    @Test
    public void testAccountNumberNormalizationWithSpacesAndCommas() {
        // Given: A customer with account numbers containing extra spaces and commas
        Customer oldCustomer = createCustomerWithMultipleAccounts();
        oldCustomer.setAccountNumbers(",1, ,, 2, ");

        Account account1 = createAccountWithNumberAndCustomer(1L, 1L);
        Account account2 = createAccountWithNumberAndCustomer(2L, 1L);

        when(accountCache.getAllAccountsFromCache()).thenReturn(List.of(account1, account2));

        // When: Validating multiple accounts belonging to the customer
        validationService.validateMultipleAccountsBelongToCustomer(oldCustomer, bindingResult);

        // Then: Ensure the input was normalized correctly
        assert oldCustomer.getAccountNumbers().equals("1,2")
                : "Expected normalized account numbers to be '1,2' but got '"
                + oldCustomer.getAccountNumbers() + "'";

        // Ensure no validation errors occurred
        verify(bindingResult, never()).rejectValue(eq("accountNumbers"), anyString(), anyString());
    }

    // Helper methods
    private Transaction createTransactionWithSourceAndDestination(Long source, Long destination) {
        Transaction transaction = new Transaction();
        transaction.setAccountSourceNumber(source);
        transaction.setAccountDestinationNumber(destination);
        transaction.setTransactionType(TransactionType.TRANSFER);
        return transaction;
    }

    private Account createAccountWithStatus(AccountStatus status) {
        Account account = new Account();
        account.setAccountStatus(status);
        return account;
    }

    private Account createAccountWithNumberAndCustomer(Long accountNumber, Long customerNumber) {
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setCustomerNumber(customerNumber);
        return account;
    }

    private Customer createCustomerWithMultipleAccounts() {
        return new Customer(1L, "John Doe", "j.doe@example.com", "+10101010101",
                "123 Elm Street", "1");
    }
}
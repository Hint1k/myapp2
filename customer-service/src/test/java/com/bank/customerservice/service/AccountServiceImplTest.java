package com.bank.customerservice.service;

import com.bank.customerservice.entity.Customer;
import com.bank.customerservice.publisher.CustomerEventPublisher;
import com.bank.customerservice.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerEventPublisher customerEventPublisher;

    @Test
    public void testUpdateCustomerDueToAccountChange_AssignNewAccount() {
        // Given: A customer exists and a new account needs to be assigned
        Long customerNumber = 123L;
        String accountNumber = "ACC456";
        Customer customer = new Customer(customerNumber, "John Doe", "john@example.com",
                "1234567890", "123 Street", "");

        when(customerRepository.findCustomerByCustomerNumber(customerNumber)).thenReturn(customer);

        // When: Updating the customer due to an account change
        accountService.updateCustomerDueToAccountChange(customerNumber, accountNumber);

        // Then: Verify that the account number is assigned and customer is updated
        assertEquals(accountNumber, customer.getAccountNumbers());
        verify(customerRepository, times(1)).save(customer);
        verify(customerEventPublisher, times(1)).publishCustomerDetailsEvent(customer);
    }

    @Test
    public void testUpdateCustomerDueToAccountChange_RemoveAccount() {
        // Given: A customer exists with the given account and it needs to be removed
        String accountNumber = "ACC456";
        Customer customer = new Customer(123L, "John Doe", "john@example.com",
                "1234567890", "123 Street", accountNumber);

        when(customerRepository.findCustomerByAccountNumber(accountNumber)).thenReturn(customer);

        // When: Removing the account number from the customer
        accountService.updateCustomerDueToAccountChange(null, accountNumber);

        // Then: Verify that the account number is removed and customer is updated
        assertEquals("", customer.getAccountNumbers());
        verify(customerRepository, times(1)).save(customer);
        verify(customerEventPublisher, times(1)).publishCustomerDetailsEvent(customer);
    }

    @Test
    public void testUpdateCustomerDueToAccountChange_RemoveOneOfMultipleAccounts() {
        // Given: A customer has multiple accounts and one needs to be removed
        String accountNumber = "ACC456";
        Customer customer = new Customer(123L, "John Doe", "john@example.com",
                "1234567890", "123 Street", "ACC123,ACC456,ACC789");

        when(customerRepository.findCustomerByAccountNumber(accountNumber)).thenReturn(customer);

        // When: Removing one account from multiple accounts
        accountService.updateCustomerDueToAccountChange(null, accountNumber);

        // Then: Verify that only the specified account is removed
        assertEquals("ACC123,ACC789", customer.getAccountNumbers());
        verify(customerRepository, times(1)).save(customer);
        verify(customerEventPublisher, times(1)).publishCustomerDetailsEvent(customer);
    }

    @Test
    public void testUpdateCustomerDueToAccountChange_CustomerNotFound() {
        // Given: The customer does not exist
        Long customerNumber = 123L;
        String accountNumber = "ACC456";

        when(customerRepository.findCustomerByCustomerNumber(customerNumber)).thenReturn(null);

        // When & Then: Expect an EntityNotFoundException when assigning an account to a non-existent customer
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> accountService.updateCustomerDueToAccountChange(customerNumber, accountNumber));

        assertEquals("Customer with customer number " + customerNumber + " not found", exception.getMessage());
        verify(customerRepository, never()).save(any());
        verify(customerEventPublisher, never()).publishCustomerDetailsEvent(any());
    }
}
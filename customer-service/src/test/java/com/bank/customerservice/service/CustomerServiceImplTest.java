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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerEventPublisher customerEventPublisher;

    @Test
    public void testSaveCustomer() {
        // Given: A new customer object
        Customer customer = new Customer(123L, "John Doe", "john@example.com",
                "1234567890", "123 Street", "ACC123");

        // When: Saving the customer
        customerService.saveCustomer(customer);

        // Then: Verify that the customer is saved and an event is published
        verify(customerRepository, times(1)).save(customer);
        verify(customerEventPublisher, times(1)).publishCustomerCreatedEvent(customer);
    }

    @Test
    public void testUpdateCustomer() {
        // Given: An existing customer object
        Customer customer = new Customer(123L, "John Doe", "john@example.com",
                "1234567890", "123 Street", "ACC123");

        // When: Updating the customer
        customerService.updateCustomer(customer);

        // Then: Verify that the customer is updated and an event is published
        verify(customerRepository, times(1)).save(customer);
        verify(customerEventPublisher, times(1)).publishCustomerUpdatedEvent(customer);
    }

    @Test
    public void testDeleteCustomer_CustomerExists() {
        // Given: A customer exists in the repository
        Long customerId = 1L;
        Customer customer = new Customer(customerId, 123L, "John Doe",
                "john@example.com", "1234567890", "123 Street", "ACC123");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When: Deleting the customer
        customerService.deleteCustomer(customerId);

        // Then: Verify that the customer is deleted and an event is published
        verify(customerRepository, times(1)).deleteById(customerId);
        verify(customerEventPublisher, times(1))
                .publishCustomerDeletedEvent(customerId, customer.getCustomerNumber());
    }

    @Test
    public void testDeleteCustomer_CustomerNotFound() {
        // Given: The customer does not exist in the repository
        Long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then: Expect an EntityNotFoundException when trying to delete a non-existent customer
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> customerService.deleteCustomer(customerId));

        assertEquals("Customer with id " + customerId + " not found", exception.getMessage());
        verify(customerRepository, never()).deleteById(customerId);
        verify(customerEventPublisher, never()).publishCustomerDeletedEvent(any(), any());
    }

    @Test
    public void testFindAllCustomers() {
        // Given: A list of customers exists
        List<Customer> customers = List.of(
                new Customer(123L, "John Doe", "john@example.com",
                        "1234567890", "123 Street", "ACC123"),
                new Customer(124L, "Jane Doe", "jane@example.com",
                        "0987654321", "456 Avenue", "ACC456")
        );
        when(customerRepository.findAll()).thenReturn(customers);

        // When: Retrieving all customers
        List<Customer> result = customerService.findAllCustomers();

        // Then: Verify that all customers are retrieved and an event is published
        assertEquals(customers, result);
        verify(customerRepository, times(1)).findAll();
        verify(customerEventPublisher, times(1)).publishAllCustomersEvent(customers);
    }

    @Test
    public void testFindCustomerById_CustomerExists() {
        // Given: A customer exists in the repository
        Long customerId = 1L;
        Customer customer = new Customer(customerId, 123L, "John Doe", "john@example.com",
                "1234567890", "123 Street", "ACC123");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When: Retrieving the customer by ID
        Customer result = customerService.findCustomerById(customerId);

        // Then: Verify that the correct customer is retrieved and an event is published
        assertEquals(customer, result);
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerEventPublisher, times(1)).publishCustomerDetailsEvent(customer);
    }

    @Test
    public void testFindCustomerById_CustomerNotFound() {
        // Given: The customer does not exist in the repository
        Long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then: Expect an EntityNotFoundException when retrieving a non-existent customer
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> customerService.findCustomerById(customerId));

        assertEquals("Customer with id " + customerId + " not found", exception.getMessage());
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerEventPublisher, never()).publishCustomerDetailsEvent(any());
    }
}
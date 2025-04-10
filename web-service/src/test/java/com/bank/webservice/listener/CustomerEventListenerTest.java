package com.bank.webservice.listener;

import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.event.customer.*;
import com.bank.webservice.service.LatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerEventListenerTest {

    @Mock
    private LatchService latchService;

    @Mock
    private CustomerCache customerCache;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private CustomerEventListener customerEventListener;

    private CustomerCreatedEvent customerCreatedEvent;
    private CustomerUpdatedEvent customerUpdatedEvent;
    private CustomerDeletedEvent customerDeletedEvent;
    private AllCustomersEvent allCustomersEvent;
    private CustomerDetailsEvent customerDetailsEvent;

    @BeforeEach
    void setUp() {
        // Initialize test events
        customerCreatedEvent = new CustomerCreatedEvent(
                1L, 101L, "John Doe", "john.doe@example.com",
                "+12345678901", "123 Main St", "1,2,3"
        );

        customerUpdatedEvent = new CustomerUpdatedEvent(
                1L, 101L, "John Doe Updated", "john.doe.updated@example.com",
                "+12345678901", "456 Updated St", "1,2,3,4"
        );

        customerDeletedEvent = new CustomerDeletedEvent(1L,1L);

        allCustomersEvent = new AllCustomersEvent(List.of(
                new Customer(1L, 101L, "John Doe", "john.doe@example.com",
                        "+12345678901", "123 Main St", "1,2,3"),
                new Customer(2L, 102L, "Jane Smith", "jane.smith@example.com",
                        "+12345678902", "456 Elm St", "4,5,6")
        ));

        customerDetailsEvent = new CustomerDetailsEvent(
                1L, 101L, "John Doe", "john.doe@example.com",
                "+12345678901", "123 Main St", "1,2,3"
        );
    }

    @Test
    public void testHandleCustomerCreatedEvent() {
        // Mock the cache behavior (void method)
        doNothing().when(customerCache).addCustomerToCache(eq(1L), any(Customer.class));

        // Act
        customerEventListener.handleCustomerCreatedEvent(customerCreatedEvent, acknowledgment);

        // Assertions
        assertNotNull(customerCreatedEvent, "CustomerCreatedEvent should not be null");
        assertEquals(1L, customerCreatedEvent.getCustomerId(), "Customer ID should be 1");

        // Verify
        verify(customerCache, times(1)).addCustomerToCache(eq(1L), any(Customer.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testHandleCustomerUpdatedEvent() {
        // Mock the cache behavior (void method)
        doNothing().when(customerCache).updateCustomerInCache(eq(1L), any(Customer.class));

        // Act
        customerEventListener.handleCustomerUpdatedEvent(customerUpdatedEvent, acknowledgment);

        // Assertions
        assertNotNull(customerUpdatedEvent, "CustomerUpdatedEvent should not be null");
        assertEquals(1L, customerUpdatedEvent.getCustomerId(), "Customer ID should be 1");
        assertEquals("John Doe Updated", customerUpdatedEvent.getName(), "Name should be updated");

        // Verify
        verify(customerCache, times(1)).updateCustomerInCache(eq(1L), any(Customer.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testHandleCustomerDeletedEvent() {
        // Mock the cache behavior (void method)
        doNothing().when(customerCache).deleteCustomerFromCache(eq(1L));

        // Act
        customerEventListener.handleCustomerDeletedEvent(customerDeletedEvent, acknowledgment);

        // Assertions
        assertNotNull(customerDeletedEvent, "CustomerDeletedEvent should not be null");
        assertEquals(1L, customerDeletedEvent.getCustomerId(), "Customer ID should be 1");

        // Verify
        verify(customerCache, times(1)).deleteCustomerFromCache(eq(1L));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    public void testHandleAllCustomersEvent() {
        // Mock the latch
        when(latchService.getLatch()).thenReturn(new CountDownLatch(1));

        // Mock the cache behavior (void method)
        doNothing().when(customerCache).addAllCustomersToCache(eq(allCustomersEvent.getCustomers()));

        // Act
        customerEventListener.handleAllCustomersEvent(allCustomersEvent, acknowledgment);

        // Assertions
        assertNotNull(allCustomersEvent, "AllCustomersEvent should not be null");
        assertEquals(2, allCustomersEvent.getCustomers().size(),
                "There should be 2 customers in the event");

        // Verify
        verify(customerCache, times(1))
                .addAllCustomersToCache(eq(allCustomersEvent.getCustomers()));
        verify(acknowledgment, times(1)).acknowledge();
        verify(latchService, times(1)).getLatch();
    }

    @Test
    public void testHandleCustomerDetailsEvent() {
        // Mock the cache behavior (void method)
        doNothing().when(customerCache).addCustomerToCache(eq(1L), any(Customer.class));

        // Act
        customerEventListener.handleCustomerDetailsEvent(customerDetailsEvent, acknowledgment);

        // Assertions
        assertNotNull(customerDetailsEvent, "CustomerDetailsEvent should not be null");
        assertEquals(1L, customerDetailsEvent.getCustomerId(), "Customer ID should be 1");
        assertEquals("John Doe", customerDetailsEvent.getName(), "Name should be John Doe");

        // Verify
        verify(customerCache, times(1)).addCustomerToCache(eq(1L), any(Customer.class));
        verify(acknowledgment, times(1)).acknowledge();
    }
}
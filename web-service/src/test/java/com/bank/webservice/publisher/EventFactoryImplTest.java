package com.bank.webservice.publisher;

import com.bank.webservice.dto.Account;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.dto.User;
import com.bank.webservice.event.BaseEvent;
import com.bank.webservice.event.account.AccountCreatedEvent;
import com.bank.webservice.event.customer.CustomerCreatedEvent;
import com.bank.webservice.event.transaction.TransactionCreatedEvent;
import com.bank.webservice.event.user.UserCreatedEvent;
import com.bank.webservice.util.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EventFactoryImplTest {

    @InjectMocks
    private EventFactoryImpl eventFactory;

    @Test
    public void testCreateCustomerCreatedEvent() {
        // Given
        Customer customer = new Customer(null, 1L, "John Doe", "john@example.com",
                "1234567890", "123 Street", null);

        // When
        BaseEvent event = eventFactory.createEvent(customer, Operation.CREATE, Customer.class);

        // Then
        assertNotNull(event);
        assertInstanceOf(CustomerCreatedEvent.class, event);
        assertEquals(1L, ((CustomerCreatedEvent) event).getCustomerNumber());
    }

    @Test
    public void testCreateTransactionCreatedEvent() {
        // Given
        Transaction transaction = new Transaction(null, new BigDecimal(100), null,
                null, null, 123456L, 654321L);

        // When
        BaseEvent event = eventFactory.createEvent(transaction, Operation.CREATE, Transaction.class);

        // Then
        assertNotNull(event);
        assertInstanceOf(TransactionCreatedEvent.class, event);
        assertEquals(BigDecimal.valueOf(100), ((TransactionCreatedEvent) event).getAmount());
    }

    @Test
    public void testCreateUserCreatedEvent() {
        // Given
        User user = new User(1L, 1L, "John Doe", "password");

        // When
        BaseEvent event = eventFactory.createEvent(user, Operation.CREATE, User.class);

        // Then
        assertNotNull(event);
        assertInstanceOf(UserCreatedEvent.class, event);
        assertEquals("John Doe", ((UserCreatedEvent) event).getUsername());
    }

    @Test
    public void testCreateAccountCreatedEvent() {
        // Given
        Account account = new Account(123456L, null, null, null,
                null, null, 1L);

        // When
        BaseEvent event = eventFactory.createEvent(account, Operation.CREATE, Account.class);

        // Then
        assertNotNull(event);
        assertInstanceOf(AccountCreatedEvent.class, event);
        assertEquals(123456L, ((AccountCreatedEvent) event).getAccountNumber());
    }

    @Test
    public void testThrowExceptionForUnsupportedOperation() {
        // Given
        Account account = new Account(123456L, null, null, null,
                null, null, null, 1L);

        // When & Then: Assert that the exception is thrown and the message is as expected
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> eventFactory.createEvent(account, Operation.DELETE, Account.class));

        assertEquals("Unsupported entity type for DELETE: Account", exception.getMessage());
    }
}
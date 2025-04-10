package com.bank.webservice.publisher;

import com.bank.webservice.dto.Account;
import com.bank.webservice.event.BaseEvent;
import com.bank.webservice.event.account.*;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.AccountType;
import com.bank.webservice.util.Currency;
import com.bank.webservice.util.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GenericEventPublisherImplTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private EventFactory eventFactory;

    @InjectMocks
    private GenericEventPublisherImpl publisher;

    @Test
    public void testPublishCreatedEvent() {
        // Given
        Account account = createNewAccount();
        BaseEvent event = createBaseEvent(account);
        when(eventFactory.createEvent(account, Operation.CREATE, Account.class)).thenReturn(event);

        // When
        publisher.publishCreatedEvent(account);

        // Then
        verify(eventFactory, times(1)).createEvent(account, Operation.CREATE, Account.class);
        verify(kafkaTemplate, times(1)).send("account-creation-requested", event);
    }

    @Test
    public void testPublishUpdatedEvent() {
        // Given
        Account account = createNewAccount();
        Account updatedAccount = createUpdateAccount();
        BaseEvent event = createBaseEvent(updatedAccount);
        when(eventFactory.createEvent(account, Operation.UPDATE, Account.class)).thenReturn(event);

        // When
        publisher.publishUpdatedEvent(account);

        // Then
        verify(eventFactory, times(1)).createEvent(account, Operation.UPDATE, Account.class);
        verify(kafkaTemplate, times(1)).send("account-update-requested", event);
    }

    @Test
    public void testPublishDeletedEvent() {
        // Given
        Long accountId = 123456L;
        BaseEvent event = new AccountDeletedEvent();
        when(eventFactory.createEvent(accountId, Operation.DELETE, Account.class)).thenReturn(event);

        // When
        publisher.publishDeletedEvent(accountId, Account.class);

        // Then
        verify(eventFactory, times(1)).createEvent(accountId, Operation.DELETE, Account.class);
        verify(kafkaTemplate, times(1)).send("account-deletion-requested", event);
    }

    @Test
    public void testPublishDetailsEvent() {
        // Given
        Long accountId = 123456L;
        BaseEvent event = new AccountDetailsEvent();
        when(eventFactory.createEvent(accountId, Operation.DETAILS, Account.class)).thenReturn(event);

        // When
        publisher.publishDetailsEvent(accountId, Account.class);

        // Then
        verify(eventFactory, times(1)).createEvent(accountId, Operation.DETAILS, Account.class);
        verify(kafkaTemplate, times(1)).send("account-details-requested", event);
    }

    @Test
    public void testPublishAllEvent() {
        // Given
        BaseEvent event = new AllAccountsEvent();
        when(eventFactory.createEvent(null, Operation.ALL, Account.class)).thenReturn(event);

        // When
        publisher.publishAllEvent(Account.class);

        // Then
        verify(eventFactory, times(1)).createEvent(null, Operation.ALL, Account.class);
        verify(kafkaTemplate, times(1)).send("all-accounts-requested", event);
    }

    private Account createNewAccount() {
        return new Account(1L, new BigDecimal("100"), Currency.EUR,
                AccountType.CHECKING, AccountStatus.ACTIVE, LocalDate.now(), 1L);
    }

    private Account createUpdateAccount() {
        return new Account(2L, 2L, new BigDecimal("200"), Currency.USD,
                AccountType.SAVINGS, AccountStatus.ACTIVE, LocalDate.now(), 2L);
    }

    private BaseEvent createBaseEvent(Account account) {
        return new AccountCreatedEvent(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpenDate(),
                account.getCustomerNumber()
        );
    }
}
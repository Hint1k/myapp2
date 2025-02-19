package com.bank.webservice.publisher;

import com.bank.webservice.dto.*;
import com.bank.webservice.event.BaseEvent;
import com.bank.webservice.event.account.*;
import com.bank.webservice.event.customer.*;
import com.bank.webservice.event.transaction.*;
import com.bank.webservice.event.user.*;
import com.bank.webservice.util.Operation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

@Component // Class designated for creating event objects
public class EventFactoryImpl implements EventFactory {

    @Override
    public BaseEvent createEvent(Object entity, Operation operation, Class<?> entityType) {
        return switch (operation) {
            case CREATE, UPDATE -> {
                if (entity instanceof Account account) {
                    yield createOrUpdateAccountEvent(account, operation);
                } else if (entity instanceof Customer customer) {
                    yield createOrUpdateCustomerEvent(customer, operation);
                } else if (entity instanceof Transaction transaction) {
                    yield createOrUpdateTransactionEvent(transaction, operation);
                } else if (entity instanceof User user) {
                    yield createUserEvent(user, operation);
                }
                throw unsupportedEntityException(operation, entity);
            }
            case DELETE, DETAILS -> {
                if (entity instanceof Long id) {
                    yield createIdBasedEvent(id, operation, entityType);
                }
                throw unsupportedEntityException(operation, entity);
            }
            case ALL -> createAllEvent(entityType);
        };
    }

    private BaseEvent createIdBasedEvent(Long id, Operation operation, Class<?> entityType) {
        return switch (operation) {
            case DELETE -> {
                if (entityType.equals(Account.class)) yield new AccountDeletedEvent(id, null);
                if (entityType.equals(Customer.class)) yield new CustomerDeletedEvent(id, null);
                if (entityType.equals(Transaction.class)) yield new TransactionDeletedEvent(id);
                throw unsupportedEntityTypeException(operation, entityType);
            }
            case DETAILS -> {
                if (entityType.equals(Account.class)) yield new AccountDetailsEvent(id,
                        null, null, null, null, null,
                        null, null);
                if (entityType.equals(Customer.class)) yield new CustomerDetailsEvent(id,
                        null, null, null, null, null, null);
                if (entityType.equals(Transaction.class))
                    yield new TransactionDetailsEvent(id, null, null, null,
                            null, null, null);
                throw unsupportedEntityTypeException(operation, entityType);
            }
            default -> throw unsupportedOperationException(operation);
        };
    }

    private BaseEvent createAllEvent(Class<?> entityType) {
        return switch (Objects.requireNonNull(entityType).getSimpleName()) {
            case "Account" -> new AllAccountsEvent(new ArrayList<>());
            case "Customer" -> new AllCustomersEvent(new ArrayList<>());
            case "Transaction" -> new AllTransactionsEvent(new ArrayList<>());
            case "User" -> new AllUsersEvent(new ArrayList<>());
            default -> throw unsupportedEntityTypeException(Operation.ALL, entityType);
        };
    }

    private BaseEvent createOrUpdateAccountEvent(Account account, Operation operation) {
        return switch (operation) {
            case CREATE -> new AccountCreatedEvent(account.getAccountNumber(), account.getBalance(),
                    account.getCurrency(), account.getAccountType(), account.getAccountStatus(),
                    account.getOpenDate(), account.getCustomerNumber());
            case UPDATE -> new AccountUpdatedEvent(account.getAccountId(), account.getAccountNumber(),
                    account.getBalance(), account.getCurrency(), account.getAccountType(),
                    account.getAccountStatus(), account.getOpenDate(), account.getCustomerNumber());
            default -> throw unsupportedOperationException(operation);
        };
    }

    private BaseEvent createOrUpdateCustomerEvent(Customer customer, Operation operation) {
        return switch (operation) {
            case CREATE -> new CustomerCreatedEvent(customer.getCustomerNumber(), customer.getName(),
                    customer.getEmail(), customer.getPhone(), customer.getAddress(), customer.getAccountNumbers());
            case UPDATE -> new CustomerUpdatedEvent(customer.getCustomerId(), customer.getCustomerNumber(),
                    customer.getName(), customer.getEmail(), customer.getPhone(), customer.getAddress(),
                    customer.getAccountNumbers());
            default -> throw unsupportedOperationException(operation);
        };
    }

    private BaseEvent createOrUpdateTransactionEvent(Transaction transaction, Operation operation) {
        return switch (operation) {
            case CREATE -> new TransactionCreatedEvent(transaction.getAmount(), transaction.getTransactionTime(),
                    transaction.getTransactionType(), transaction.getTransactionStatus(),
                    transaction.getAccountSourceNumber(), transaction.getAccountDestinationNumber());
            case UPDATE -> new TransactionUpdatedEvent(transaction.getTransactionId(), transaction.getAmount(),
                    transaction.getTransactionTime(), transaction.getTransactionType(),
                    transaction.getTransactionStatus(), transaction.getAccountSourceNumber(),
                    transaction.getAccountDestinationNumber());
            default -> throw unsupportedOperationException(operation);
        };
    }

    private BaseEvent createUserEvent(User user, Operation operation) {
        return switch (operation) {
            case CREATE -> new UserCreatedEvent(user.getUserId(), user.getCustomerNumber(),
                    user.getUsername(), user.getPassword());
            case UPDATE -> // User update events are not supported yet but might be implemented in the future
                    throw unsupportedOperationException(operation);
            default -> throw unsupportedOperationException(operation);
        };
    }

    // Helper method for unsupported entity type exceptions
    private IllegalArgumentException unsupportedEntityTypeException(Operation operation, Class<?> entityType) {
        return new IllegalArgumentException("Unsupported entity type for " + operation + ": "
                + entityType.getSimpleName());
    }

    // Helper method for unsupported operation exceptions
    private IllegalArgumentException unsupportedOperationException(Operation operation) {
        return new IllegalArgumentException("Unsupported operation: " + operation);
    }

    // Helper method for unsupported entity (Object) exceptions
    private IllegalArgumentException unsupportedEntityException(Operation operation, Object entity) {
        return new IllegalArgumentException("Unsupported entity type for " + operation + ": "
                + (entity != null ? entity.getClass().getSimpleName() : "null"));
    }
}
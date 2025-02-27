package com.bank.gatewayservice.service;

import com.bank.gatewayservice.cache.AccountCache;
import com.bank.gatewayservice.cache.CustomerCache;
import com.bank.gatewayservice.cache.TransactionCache;
import com.bank.gatewayservice.dto.Account;
import com.bank.gatewayservice.dto.Customer;
import com.bank.gatewayservice.dto.Transaction;
import com.bank.gatewayservice.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.List;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AccessServiceImplTest {

    @Mock
    private CustomerCache customerCache;

    @Mock
    private AccountCache accountCache;

    @Mock
    private TransactionCache transactionCache;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AccessServiceImpl accessService;

    private User user;
    private Customer customer;
    private Transaction transaction;
    private Account account;
    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        // Initialize test data
        user = new User();
        user.setUserId(1L);
        user.setUsername("testUser");
        user.setCustomerNumber(101L);

        customer = new Customer();
        customer.setCustomerId(101L);
        customer.setCustomerNumber(101L);

        transaction = new Transaction();
        transaction.setTransactionId(101L);
        transaction.setAccountSourceNumber(101L);
        transaction.setAccountDestinationNumber(102L);

        account = new Account();
        account.setAccountId(101L);
        account.setCustomerNumber(101L);

        sourceAccount = new Account();
        sourceAccount.setAccountId(101L);
        sourceAccount.setCustomerNumber(101L); // Mismatch

        destinationAccount = new Account();
        destinationAccount.setAccountId(102L);
        destinationAccount.setCustomerNumber(101L); // Mismatch
    }

    @Test
    public void testValidateRoleAccess_CustomerOwnership_Success() {
        try {
            // Arrange
            String requestURI = "/api/customers/101";
            List<String> roles = List.of("ROLE_USER");

            when(customerCache.getCustomerFromCache(101L)).thenReturn(customer);

            // Act
            accessService.validateRoleAccess(requestURI, roles, response, user);

            // Verify
            verify(response, never()).sendError(anyInt(), anyString());
        } catch (Exception e) {
            log.error("testValidateRoleAccess_CustomerOwnership_Success() failed: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testValidateRoleAccess_CustomerOwnership_Failure() {
        try {
        // Arrange
        String requestURI = "/api/customers/102";
        List<String> roles = List.of("ROLE_USER");

        // Mock customer cache behavior
        Customer customer = new Customer();
        customer.setCustomerId(102L);
        customer.setCustomerNumber(102L);
        when(customerCache.getCustomerFromCache(102L)).thenReturn(customer);

        // Act
        accessService.validateRoleAccess(requestURI, roles, response, user);

        // Verify
        verify(response, times(1))
                .sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Customer mismatch");
    } catch (IOException e) {
            log.error("testValidateRoleAccess_CustomerOwnership_Failure() failed: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testValidateRoleAccess_AccountOwnership_Success() {
        try {
            // Arrange
            String requestURI = "/api/accounts/101";
            List<String> roles = List.of("ROLE_USER");

            when(accountCache.getAccountFromCache(101L)).thenReturn(account);

            // Act
            accessService.validateRoleAccess(requestURI, roles, response, user);

            // Verify
            verify(response, never()).sendError(anyInt(), anyString());
        } catch (Exception e) {
            log.error("testValidateRoleAccess_AccountOwnership_Success() failed: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testValidateRoleAccess_AccountOwnership_Failure() {
        try {
            // Arrange
            String requestURI = "/api/accounts/101";
            List<String> roles = List.of("ROLE_USER");

            // Mock a wrong user
            user.setCustomerNumber(102L);

            when(accountCache.getAccountFromCache(101L)).thenReturn(account);

            // Act
            accessService.validateRoleAccess(requestURI, roles, response, user);

            // Verify
            verify(response, times(1))
                    .sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Account mismatch");
        } catch (Exception e) {
            log.error("testValidateRoleAccess_AccountOwnership_Failure() failed: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testValidateRoleAccess_TransactionOwnership_Success() {
        try {
            // Arrange
            String requestURI = "/api/transactions/101";
            List<String> roles = List.of("ROLE_USER");

            when(transactionCache.getTransactionFromCache(101L)).thenReturn(transaction);
            when(accountCache.getAccountFromCache(101L)).thenReturn(sourceAccount);
            when(accountCache.getAccountFromCache(102L)).thenReturn(destinationAccount);

            // Act
            accessService.validateRoleAccess(requestURI, roles, response, user);

            // Verify
            verify(response, never()).sendError(anyInt(), anyString());
        } catch (Exception e) {
            log.error("testValidateRoleAccess_TransactionOwnership_Success() failed: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testValidateRoleAccess_TransactionOwnership_Failure() {
        try {
            // Arrange
            String requestURI = "/api/transactions/101";
            List<String> roles = List.of("ROLE_USER");

            // mocking a mismatch
            sourceAccount.setCustomerNumber(102L);
            destinationAccount.setCustomerNumber(103L);

            when(transactionCache.getTransactionFromCache(101L)).thenReturn(transaction);
            when(accountCache.getAccountFromCache(101L)).thenReturn(sourceAccount);
            when(accountCache.getAccountFromCache(102L)).thenReturn(destinationAccount);

            // Act
            accessService.validateRoleAccess(requestURI, roles, response, user);

            // Verify
            verify(response, times(1))
                    .sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied - Transaction mismatch");
        } catch (Exception e) {
            log.error("testValidateRoleAccess_TransactionOwnership_Failure() failed: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }
}
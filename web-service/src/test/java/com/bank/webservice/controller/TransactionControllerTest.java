package com.bank.webservice.controller;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.cache.TransactionCache;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.publisher.GenericEventPublisher;
import com.bank.webservice.service.FilterService;
import com.bank.webservice.service.LatchService;
import com.bank.webservice.service.RoleService;
import com.bank.webservice.service.ValidationService;
import com.bank.webservice.testConfig.TestLatchConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
@WithMockUser
@Slf4j
@AutoConfigureMockMvc
@Import(TestLatchConfig.class) // to make Latch instance the same in the controller and in the test
public class TransactionControllerTest {

    @MockBean
    private GenericEventPublisher publisher;

    @MockBean
    private TransactionCache cache;

    @MockBean
    private AccountCache accountCache;

    @MockBean
    private ValidationService validationService;

    @MockBean
    private RoleService role;

    @MockBean
    private FilterService filterService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LatchService latchService;

    @BeforeEach
    void setUp() {
        latchService.getLatch().countDown();
    }

    @Test
    public void testShowNewTransactionForm() {
        try {
            mockMvc.perform(get("/api/transactions/new-transaction"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("transaction/new-transaction"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testShowNewTransactionForm() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShowUpdateTransactionForm() {
        when(cache.getTransactionFromCache(1L)).thenReturn(new Transaction());

        try {
            mockMvc.perform(put("/api/transactions/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("transaction/transaction-update"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testShowUpdateTransactionForm() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(cache, times(1)).getTransactionFromCache(1L);
    }

    @Test
    public void testCreateTransaction() {
        doNothing().when(publisher).publishCreatedEvent(any(Transaction.class));

        try {
            mockMvc.perform(post("/api/transactions")
                            .param("amount", "100")
                            .param("transactionTime", LocalDateTime.now().toString())
                            .param("transactionType", "TRANSFER")
                            .param("transactionStatus", "APPROVED")
                            .param("accountSourceNumber", "1")
                            .param("accountDestinationNumber", "2")
                            .param("customerNumber", "3")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testCreateTransaction() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(publisher, times(1)).publishCreatedEvent(any(Transaction.class));
    }

    @Test
    public void testUpdateTransaction() {
        doNothing().when(publisher).publishUpdatedEvent(any(Transaction.class));

        try {
            mockMvc.perform(post("/api/transactions/transaction")
                            .param("amount", "100")
                            .param("transactionTime", LocalDateTime.now().toString())
                            .param("transactionType", "TRANSFER")
                            .param("transactionStatus", "APPROVED")
                            .param("accountSourceNumber", "1")
                            .param("accountDestinationNumber", "2")
                            .param("customerNumber", "3")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testUpdateTransaction() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(publisher, times(1)).publishUpdatedEvent(any(Transaction.class));
    }

    @Test
    public void testDeleteTransaction() {
        doNothing().when(publisher).publishDeletedEvent(1L, Transaction.class);

        try {
            mockMvc.perform(delete("/api/transactions/1")
                            .with(csrf())
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testDeleteTransaction() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(publisher, times(1)).publishDeletedEvent(1L, Transaction.class);
    }

    @Test
    public void testGetTransaction() {
        doNothing().when(publisher).publishDetailsEvent(1L, Transaction.class);
        when(cache.getTransactionFromCache(1L)).thenReturn(new Transaction());

        try {
            mockMvc.perform(get("/api/transactions/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("transaction/transaction-details"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testGetTransaction() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(cache, times(1)).getTransactionFromCache(1L);
        verify(publisher, times(1)).publishDetailsEvent(1L, Transaction.class);
    }

    @Test
    public void testGetAllTransactions() {
        try {
            List<Transaction> transactions = createTestTransactions();
            doNothing().when(publisher).publishAllEvent(Transaction.class);
            when(cache.getAllTransactionsFromCache()).thenReturn(transactions);

            mockMvc.perform(get("/api/transactions/all-transactions"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("transaction/all-transactions"))
                    .andDo(print());

            verify(cache, times(1)).getAllTransactionsFromCache();
            verify(publisher, times(1)).publishAllEvent(Transaction.class);
        } catch (Exception e) {
            log.error("testGetAllTransactions() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testGetAllTransactions_ForMultipleAccountsByCustomerNumber() {
        List<Transaction> transactions = createTestTransactions();
        List<Long> accountNumbers = getAccountNumbers();
        doNothing().when(publisher).publishAllEvent(Transaction.class);
        when(accountCache.getAccountNumbersFromCacheByCustomerNumber(5L)).thenReturn(accountNumbers);
        when(cache.getTransactionsForMultipleAccountsFromCache(accountNumbers)).thenReturn(transactions);

        try {
            mockMvc.perform(get("/api/transactions/all-transactions")
                    .requestAttr("customerNumber", "5"))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            log.error("testGetAllTransactionsForMultipleAccountsByCustomerNumber() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(cache, times(1)).getTransactionsForMultipleAccountsFromCache(accountNumbers);
        verify(accountCache, times(1)).getAccountNumbersFromCacheByCustomerNumber(5L);
        verify(publisher, times(1)).publishAllEvent(Transaction.class);
    }

    @Test
    public void testGetTransactionsByAccountNumber() {
        List<Transaction> transactions = createTestTransactions();
        doNothing().when(publisher).publishAllEvent(Transaction.class);
        when(cache.getTransactionsForAccountFromCache(12345L)).thenReturn(transactions);

        try {
            mockMvc.perform(get("/api/transactions/all-transactions/12345"))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            log.error("testGetTransactionsByAccountNumber() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(cache, times(1)).getTransactionsForAccountFromCache(12345L);
        verify(publisher, times(1)).publishAllEvent(Transaction.class);
    }

    private List<Transaction> createTestTransactions() {
        Transaction transaction1 = new Transaction();
        transaction1.setTransactionId(1L);
        Transaction transaction2 = new Transaction();
        transaction2.setTransactionId(2L);
        Transaction transaction3 = new Transaction();
        transaction3.setTransactionId(3L);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        return transactions;
    }

    private List<Long> getAccountNumbers(){
        List<Long> accountNumbers = new ArrayList<>();
        accountNumbers.add(4L);
        accountNumbers.add(5L);
        accountNumbers.add(6L);
        return accountNumbers;
    }
}
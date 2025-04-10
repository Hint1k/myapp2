package com.bank.webservice.controller;

import com.bank.webservice.publisher.GenericEventPublisher;
import com.bank.webservice.testConfig.TestLatchConfig;
import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.service.*;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = AccountController.class)
@WithMockUser
@Slf4j
@AutoConfigureMockMvc
@Import(TestLatchConfig.class) // to make Latch instance the same in the controller and in the test
public class AccountControllerTest {

    @MockBean
    private GenericEventPublisher publisher;

    @MockBean
    private AccountCache cache;

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
    public void testShowNewAccountForm() {
        try {
            mockMvc.perform(get("/api/accounts/new-account"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("account/new-account"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testShowNewAccountForm() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShowUpdateAccountForm() {
        try {
            when(cache.getAccountFromCache(1L)).thenReturn(new Account());

            mockMvc.perform(put("/api/accounts/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("account/account-update"))
                    .andDo(print());


            verify(cache, times(1)).getAccountFromCache(1L);
        } catch (Exception e) {
            log.error("testShowUpdateAccountForm() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testCreateAccount() {
        doNothing().when(publisher).publishCreatedEvent(any(Account.class));

        try {
            mockMvc.perform(post("/api/accounts")
                            .param("accountNumber", "123456789")
                            .param("balance", "0")
                            .param("currency", "RUB")
                            .param("accountType", "CREDIT")
                            .param("accountStatus", "ACTIVE")
                            .param("openDate", LocalDate.now().toString())
                            .param("customerNumber", "987654321")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testCreateAccount() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }

        verify(publisher, times(1)).publishCreatedEvent(any(Account.class));
    }

    @Test
    public void testUpdateAccount() {
        try {
            doNothing().when(publisher).publishUpdatedEvent(any(Account.class));

            mockMvc.perform(post("/api/accounts/account")
                            .param("accountNumber", "123456789")
                            .param("balance", "1")
                            .param("currency", "RUB")
                            .param("accountType", "CREDIT")
                            .param("accountStatus", "ACTIVE")
                            .param("openDate", LocalDate.now().toString())
                            .param("customerNumber", "987654321")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"))
                    .andDo(print());

            verify(publisher, times(1)).publishUpdatedEvent(any(Account.class));
        } catch (Exception e) {
            log.error("testUpdateAccount() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testDeleteAccount() {
        try {
            doNothing().when(publisher).publishDeletedEvent(1L, Account.class);

            mockMvc.perform(delete("/api/accounts/1")
                            .with(csrf())
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"))
                    .andDo(print());

            verify(publisher, times(1)).publishDeletedEvent(1L, Account.class);
        } catch (Exception e) {
            log.error("testDeleteAccount() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testGetAccount() {
        try {
            doNothing().when(publisher).publishDetailsEvent(1L, Account.class);
            when(cache.getAccountFromCache(1L)).thenReturn(new Account());

            mockMvc.perform(get("/api/accounts/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("account/account-details"))
                    .andDo(print());

            verify(cache, times(1)).getAccountFromCache(1L);
            verify(publisher, times(1)).publishDetailsEvent(1L, Account.class);
        } catch (Exception e) {
            log.error("testGetAccount() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testGetAllAccounts() {
        try {
            List<Account> accounts = createTestAccounts();
            doNothing().when(publisher).publishAllEvent(Account.class);
            when(cache.getAllAccountsFromCache()).thenReturn(accounts);

            mockMvc.perform(get("/api/accounts/all-accounts"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("account/all-accounts"))
                    .andDo(print());

            verify(cache, times(1)).getAllAccountsFromCache();
            verify(publisher, times(1)).publishAllEvent(Account.class);
        } catch (Exception e) {
            log.error("testGetAllAccounts() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    @Test
    public void testGetAccountsByCustomerNumber() {
        try {
            List<Account> accounts = createTestAccounts();
            doNothing().when(publisher).publishAllEvent(Account.class);
            when(cache.getAccountsFromCacheByCustomerNumber(12345L)).thenReturn(accounts);

            mockMvc.perform(get("/api/accounts/all-accounts/12345"))
                    .andExpect(status().isOk())
                    .andDo(print());

            verify(cache, times(1)).getAccountsFromCacheByCustomerNumber(12345L);
            verify(publisher, times(1)).publishAllEvent(Account.class);
        } catch (Exception e) {
            log.error("testGetAccountsByCustomerNumber() fails: {}", e.getMessage());
            fail("Test failed due to exception: {}" + e.getMessage());
        }
    }

    private List<Account> createTestAccounts() {
        Account account1 = new Account();
        account1.setAccountId(1L);
        Account account2 = new Account();
        account2.setAccountId(2L);
        Account account3 = new Account();
        account3.setAccountId(3L);

        List<Account> accounts = new ArrayList<>();
        accounts.add(account1);
        accounts.add(account2);
        accounts.add(account3);
        return accounts;
    }
}
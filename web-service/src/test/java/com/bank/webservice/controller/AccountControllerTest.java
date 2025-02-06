package com.bank.webservice.controller;

import com.bank.webservice.testConfig.TestLatchConfig;
import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.publisher.AccountEventPublisher;
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
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountController.class)
@WithMockUser
@Slf4j
@AutoConfigureMockMvc
@Import(TestLatchConfig.class) // to make Latch instance the same in the controller and in the test
public class AccountControllerTest {

    @MockBean
    private AccountEventPublisher publisher;

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
    void setUp(WebApplicationContext wac) {
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
        when(cache.getAccountFromCache(1L)).thenReturn(new Account());

        try {
            mockMvc.perform(put("/api/accounts/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("account/account-update"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testShowUpdateAccountForm() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(cache, times(1)).getAccountFromCache(1L);
    }

    @Test
    public void testCreateAccount() {
        doNothing().when(publisher).publishAccountCreatedEvent(any(Account.class));

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
            throw new RuntimeException(e);
        }

        verify(publisher, times(1)).publishAccountCreatedEvent(any(Account.class));
    }

    @Test
    public void testUpdateAccount() {
        doNothing().when(publisher).publishAccountUpdatedEvent(any(Account.class));

        try {
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
        } catch (Exception e) {
            log.error("testUpdateAccount() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(publisher, times(1)).publishAccountUpdatedEvent(any(Account.class));
    }

    @Test
    public void testDeleteAccount() {
        doNothing().when(publisher).publishAccountDeletedEvent(1L);

        try {
            mockMvc.perform(delete("/api/accounts/1")
                            .with(csrf())
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testDeleteAccount() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(publisher, times(1)).publishAccountDeletedEvent(1L);
    }

    @Test
    public void testGetAccount() {
        doNothing().when(publisher).publishAccountDeletedEvent(1L);
        when(cache.getAccountFromCache(1L)).thenReturn(new Account());

        try {
            mockMvc.perform(get("/api/accounts/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("account/account-details"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testGetAccount() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(cache, times(1)).getAccountFromCache(1L);
        verify(publisher, times(1)).publishAccountDetailsEvent(1L);
    }

    @Test
    public void testGetAllAccounts() throws Exception {
        List<Account> accounts = createTestAccounts();
        when(cache.getAllAccountsFromCache()).thenReturn(accounts);

        mockMvc.perform(get("/api/accounts/all-accounts"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/all-accounts"))
                .andDo(print());

        verify(cache, times(1)).getAllAccountsFromCache();
    }

    @Test
    public void testGetAccountsByCustomerNumber() {
        List<Account> accounts = createTestAccounts();
        when(cache.getAccountsFromCacheByCustomerNumber(12345L)).thenReturn(accounts);

        try {
            mockMvc.perform(get("/api/accounts/all-accounts/12345"))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            log.error("testGetAccountsByCustomerNumber() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(cache, times(1)).getAccountsFromCacheByCustomerNumber(12345L);
    }

    private List<Account> createTestAccounts(){
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
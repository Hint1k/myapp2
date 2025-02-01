package com.bank.webservice.controller;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.controller.AccountController;
import com.bank.webservice.dto.Account;
import com.bank.webservice.publisher.AccountEventPublisher;
import com.bank.webservice.service.LatchService;
import com.bank.webservice.service.RoleService;
import com.bank.webservice.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private LatchService latch;

    @Mock
    private AccountEventPublisher publisher;

    @Mock
    private AccountCache cache;

    @Mock
    private ValidationService validator;

    @Mock
    private RoleService role;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    void createAccount_Success() {
        Account account = new Account();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = accountController.createAccount(account, bindingResult);
        assertEquals("redirect:/home", result);
        verify(publisher).publishAccountCreatedEvent(account);
    }

    @Test
    void deleteAccount_Success() {
        Long accountId = 1L;

        String result = accountController.deleteAccount(accountId);
        assertEquals("redirect:/home", result);
        verify(publisher).publishAccountDeletedEvent(accountId);
    }
}
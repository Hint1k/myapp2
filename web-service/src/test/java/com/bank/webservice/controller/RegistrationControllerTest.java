package com.bank.webservice.controller;

import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.cache.UserCache;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.dto.User;
import com.bank.webservice.publisher.GenericPublisher;
import com.bank.webservice.service.FilterService;
import com.bank.webservice.service.LatchService;
import com.bank.webservice.testConfig.TestLatchConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RegistrationController.class)
@Slf4j
@AutoConfigureMockMvc
@Import(TestLatchConfig.class) // to make Latch instance the same in the controller and in the test
public class RegistrationControllerTest {

    @MockBean
    private GenericPublisher publisher;

    @MockBean
    private CustomerCache customerCache;

    @MockBean
    private UserCache userCache;

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
    public void testShowRegistrationPage() {
        doNothing().when(publisher).publishAllEvent(Customer.class);
        doNothing().when(publisher).publishAllEvent(User.class);

        // Mock customers
        List<Customer> mockCustomers = new ArrayList<>();
        mockCustomers.add(new Customer(1L, "John Doe", "test1@test.com",
                "+10101010101", "10 Downing street, London, UK", "1" ));
        mockCustomers.add(new Customer(2L, "Jane Doe", "test2@test.com",
                "+20202020202", "11 Downing street, London, UK", "2" ));

        // Mock users
        List<User> mockUsers = new ArrayList<>();
        mockUsers.add(new User(1L, "user1", "123" ));

        when(customerCache.getAllCustomersFromCache()).thenReturn(mockCustomers);
        when(userCache.getAllUsersFromCache()).thenReturn(mockUsers);

        try {
            mockMvc.perform(get("/register"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("registration-form"))
                    .andExpect(model().attributeExists("customerNumbers"))
                    .andExpect(model().attribute("customerNumbers", List.of(2L)))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testShowRegistrationPage() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(customerCache, times(1)).getAllCustomersFromCache();
        verify(userCache, times(1)).getAllUsersFromCache();
        verify(publisher, times(1)).publishAllEvent(Customer.class);
        verify(publisher, times(1)).publishAllEvent(User.class);
    }

    @Test
    public void testRegisterUser() {
        doNothing().when(publisher).publishCreatedEvent(any(User.class));

        try {
            mockMvc.perform(post("/register")
                            .param("username", "user")
                            .param("password", "123")
                            .param("customerNumber", "321")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("registration-successful"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testRegisterUser() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(publisher, times(1)).publishCreatedEvent(any(User.class));
    }
}
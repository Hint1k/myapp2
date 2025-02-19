package com.bank.webservice.controller;

import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.publisher.GenericPublisher;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerController.class)
@WithMockUser
@Slf4j
@AutoConfigureMockMvc
@Import(TestLatchConfig.class) // to make Latch instance the same in the controller and in the test
public class CustomerControllerTest {

    @MockBean
    private GenericPublisher publisher;

    @MockBean
    private CustomerCache cache;

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
    public void testShowNewCustomerForm() {
        try {
            mockMvc.perform(get("/api/customers/new-customer"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("customer/new-customer"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testShowNewCustomerForm() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testShowUpdateCustomerForm() {
        when(cache.getCustomerFromCache(1L)).thenReturn(new Customer());

        try {
            mockMvc.perform(put("/api/customers/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("customer/customer-update"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testShowUpdateCustomerForm() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(cache, times(1)).getCustomerFromCache(1L);
    }

    @Test
    public void testCreateCustomer() {
        doNothing().when(publisher).publishCreatedEvent(any(Customer.class));

        try {
            mockMvc.perform(post("/api/customers")
                            .param("customerNumber", "1")
                            .param("name", "John Doe")
                            .param("email", "test@test.com")
                            .param("phone", "+10101010101")
                            .param("CustomerStatus", "ACTIVE")
                            .param("address", "10 Downing street, London, UK")
                            .param("accountNumbers", "1")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testCreateCustomer() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(publisher, times(1)).publishCreatedEvent(any(Customer.class));
    }

    @Test
    public void testUpdateCustomer() {
        doNothing().when(publisher).publishUpdatedEvent(any(Customer.class));

        try {
            mockMvc.perform(post("/api/customers/customer")
                            .param("customerNumber", "1")
                            .param("name", "John Doe")
                            .param("email", "test@test.com")
                            .param("phone", "+10101010101")
                            .param("CustomerStatus", "ACTIVE")
                            .param("address", "London, Downing street 10")
                            .param("accountNumbers", "1")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testUpdateCustomer() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(publisher, times(1)).publishUpdatedEvent(any(Customer.class));
    }

    @Test
    public void testDeleteCustomer() {
        doNothing().when(publisher).publishDeletedEvent(1L, Customer.class);

        try {
            mockMvc.perform(delete("/api/customers/1")
                            .with(csrf())
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testDeleteCustomer() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(publisher, times(1)).publishDeletedEvent(1L, Customer.class);
    }

    @Test
    public void testGetCustomer() {
        doNothing().when(publisher).publishDetailsEvent(1L, Customer.class);
        when(cache.getCustomerFromCache(1L)).thenReturn(new Customer());

        try {
            mockMvc.perform(get("/api/customers/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("customer/customer-details"))
                    .andDo(print());
        } catch (Exception e) {
            log.error("testGetCustomer() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(cache, times(1)).getCustomerFromCache(1L);
        verify(publisher, times(1)).publishDetailsEvent(1L, Customer.class);
    }

    @Test
    public void testGetAllCustomers() throws Exception {
        List<Customer> Customers = createTestCustomers();
        doNothing().when(publisher).publishAllEvent(Customer.class);
        when(cache.getAllCustomersFromCache()).thenReturn(Customers);

        mockMvc.perform(get("/api/customers/all-customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/all-customers"))
                .andDo(print());

        verify(cache, times(1)).getAllCustomersFromCache();
        verify(publisher, times(1)).publishAllEvent(Customer.class);
    }

    @Test
    public void testGetCustomersByCustomerNumber() {
        doNothing().when(publisher).publishAllEvent(Customer.class);
        when(cache.getCustomerFromCacheByCustomerNumber(2L)).thenReturn(new Customer());

        try {
            mockMvc.perform(get("/api/customers/all-customers/2"))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            log.error("testGetCustomersByCustomerNumber() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        verify(cache, times(1)).getCustomerFromCacheByCustomerNumber(2L);
        verify(publisher, times(1)).publishAllEvent(Customer.class);
    }

    private List<Customer> createTestCustomers() {
        Customer customer1 = new Customer();
        customer1.setCustomerId(1L);
        customer1.setAccountNumbers("1,2,3");
        Customer customer2 = new Customer();
        customer2.setCustomerId(2L);
        customer2.setAccountNumbers("4,5,6");
        Customer customer3 = new Customer();
        customer3.setCustomerId(3L);
        customer3.setAccountNumbers("7,8,9");
        List<Customer> customers = new ArrayList<>();
        customers.add(customer1);
        customers.add(customer2);
        customers.add(customer3);
        return customers;
    }
}
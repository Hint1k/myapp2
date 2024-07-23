package com.bank.webservice.controller;

import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.publisher.CustomerEventPublisher;
import com.bank.webservice.service.LatchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
@RequestMapping("/api")
public class CustomerController {

    private final LatchService latch;
    private final CustomerEventPublisher publisher;
    private final CustomerCache cache;
    private static final int MAX_RESPONSE_TIME = 3; // seconds

    @Autowired
    public CustomerController(LatchService latch, CustomerEventPublisher publisher, CustomerCache cache) {
        this.latch = latch;
        this.publisher = publisher;
        this.cache = cache;
    }

    // Cutting off the spaces entered by user to avoid errors
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/customers/new-customer")
    private String showNewCustomerForm(Model model) {
        Customer customer = new Customer();
        model.addAttribute("customer", customer);
        return "new-customer";
    }

    @PostMapping("/customers")
    public String createCustomer(@Valid @ModelAttribute("customer") Customer newCustomer, BindingResult bindingResult) {
        List<Customer> customers = cache.getAllCustomersFromCache();
        boolean customerExists = customers.stream()
                .anyMatch(customer -> customer.getCustomerNumber().equals(newCustomer.getCustomerNumber()));
        if (customerExists) {
            bindingResult.rejectValue("customerNumber", "error.customer",
                    "customer with the same number already exists.");
        }
        if (bindingResult.hasErrors()) {
            log.error("customer saving failed due to validation errors: {}",
                    bindingResult.getAllErrors());
            return "new-customer";
        }
        publisher.publishCustomerCreatedEvent(newCustomer);
        return "redirect:/index";
    }

    @PutMapping("/customers/{customerId}")
    public String showUpdateCustomerForm(@PathVariable("customerId") Long customerId, Model model) {
        Customer customer = cache.getCustomerFromCache(customerId);
        model.addAttribute("customer", customer);
        return "customer-update";
    }

    @PostMapping("/customers/customer")
    public String updatecustomer(@ModelAttribute("customer") Customer customer) {
        publisher.publishCustomerUpdatedEvent(customer);
        return "redirect:/index";
    }

    @DeleteMapping("/customers/{customerId}")
    public String deleteCustomer(@PathVariable("customerId") Long customerId) {
        publisher.publishCustomerDeletedEvent(customerId);
        return "redirect:/index";
    }

    @GetMapping("/customers/{customerId}")
    public String getCustomer(@PathVariable("customerId") Long customerId, Model model) {
        publisher.publishCustomerDetailsEvent(customerId);
        Customer customer = cache.getCustomerFromCache(customerId);
        if (customer != null) {
            model.addAttribute("customer", customer);
            return "customer-details";
        } else {
            model.addAttribute("customerId", customerId);
            return "loading-customers";
        }
    }

    @GetMapping("/customers/all-customers")
    public String getAllCustomers(Model model) {
        publisher.publishAllCustomersEvent();
        CountDownLatch latch = new CountDownLatch(1);
        this.latch.setLatch(latch);
        try {
            boolean latchResult = latch.await(MAX_RESPONSE_TIME, TimeUnit.SECONDS);
            if (latchResult) {
                List<Customer> customers = cache.getAllCustomersFromCache();
                if (customers != null && !customers.isEmpty()) {
                    customers.sort(Comparator.comparing(Customer::getCustomerId));
                    model.addAttribute("customers", customers);
                    return "all-customers";
                } else { // returns empty table when no customers in database
                    model.addAttribute("customers", new ArrayList<>());
                    return "all-customers";
                }
            } else {
                String errorMessage = "The service is busy, please try again later.";
                model.addAttribute("errorMessage", errorMessage);
                log.error("Timeout waiting for customers: {}", errorMessage);
                return "error";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "loading-customers";
        } finally {
            this.latch.resetLatch();
        }
    }
}
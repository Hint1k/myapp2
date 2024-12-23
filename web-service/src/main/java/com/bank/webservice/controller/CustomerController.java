package com.bank.webservice.controller;

import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.publisher.CustomerEventPublisher;
import com.bank.webservice.service.LatchService;
import com.bank.webservice.service.RoleService;
import com.bank.webservice.service.ValidationService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/api")
public class CustomerController {

    private final LatchService latch;
    private final CustomerEventPublisher publisher;
    private final CustomerCache cache;
    private final ValidationService validator;
    private final RoleService role;
    private static final int MAX_RESPONSE_TIME = 3; // seconds

    @Autowired
    public CustomerController(LatchService latch, CustomerEventPublisher publisher,
                              CustomerCache cache, ValidationService validator, RoleService role) {
        this.latch = latch;
        this.publisher = publisher;
        this.cache = cache;
        this.validator = validator;
        this.role = role;
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
        return "customer/new-customer";
    }

    @PostMapping("/customers")
    public String createCustomer(@Valid @ModelAttribute("customer") Customer newCustomer,
                                 BindingResult bindingResult) {
        validator.validateCustomer(newCustomer, bindingResult);
        if (bindingResult.hasErrors()) {
            log.error("customer saving failed due to validation errors: {}", bindingResult.getAllErrors());
            return "customer/new-customer";
        }
        publisher.publishCustomerCreatedEvent(newCustomer);
        return "redirect:/home";
    }

    @PutMapping("/customers/{customerId}")
    public String showUpdateCustomerForm(@PathVariable("customerId") Long customerId, Model model) {
        Customer customer = cache.getCustomerFromCache(customerId);
        model.addAttribute("customer", customer);
        return "customer/customer-update";
    }

    @PostMapping("/customers/customer")
    public String updateCustomer(@ModelAttribute("customer") Customer oldCustomer, BindingResult bindingResult) {
        validator.validateMultipleAccountsBelongToCustomer(oldCustomer, bindingResult);
        if (bindingResult.hasErrors()) {
            log.error("customer update failed due to validation errors: {}", bindingResult.getAllErrors());
            return "customer/customer-update";
        }
        publisher.publishCustomerUpdatedEvent(oldCustomer);
        return "redirect:/home";
    }

    @DeleteMapping("/customers/{customerId}")
    public String deleteCustomer(@PathVariable("customerId") Long customerId) {
        publisher.publishCustomerDeletedEvent(customerId);
        return "redirect:/home";
    }

    @GetMapping("/customers/{customerId}")
    public String getCustomer(@PathVariable("customerId") Long customerId, Model model) {
        publisher.publishCustomerDetailsEvent(customerId);
        Customer customer = cache.getCustomerFromCache(customerId);
        if (customer != null) {
            model.addAttribute("customer", customer);
            return "customer/customer-details";
        } else {
            model.addAttribute("customerId", customerId);
            return "customer/loading-customers";
        }
    }

    @GetMapping("/customers/all-customers")
    public String getAllCustomers(Model model, HttpServletRequest request) {
        // Check if the customer number is present in the request attribute (set by FilterServiceImpl)
        String customerNumber = (String) request.getAttribute("customerNumber");

        if (customerNumber != null) {
            log.info("Filtering customers for customer number: {}", customerNumber);
            // Delegate to getCustomerByCustomerNumber to handle customer-specific filtering
            return getCustomerByCustomerNumber(Long.parseLong(customerNumber), model, request);
        }

        // If no customer number, retrieve all customers
        return handleCustomersRetrieval(model, request, null);
    }

    @GetMapping("/customers/all-customers/{customerNumber}")
    public String getCustomerByCustomerNumber(@PathVariable("customerNumber") Long customerNumber, Model model,
                                              HttpServletRequest request) {
        return handleCustomersRetrieval(model, request, customerNumber);
    }

    private String handleCustomersRetrieval(Model model, HttpServletRequest request, Long customerNumber) {
        publisher.publishAllCustomersEvent();
        CountDownLatch latch = new CountDownLatch(1);
        this.latch.setLatch(latch);
        try {
            boolean latchResult = latch.await(MAX_RESPONSE_TIME, TimeUnit.SECONDS);
            if (latchResult) {
                List<Customer> customers;
                if (customerNumber == null) {
                    customers = cache.getAllCustomersFromCache();
                } else {
                    Customer customer = cache.getCustomerFromCache(customerNumber);
                    customers = new ArrayList<>();
                    customers.add(customer);
                }
                if (customers != null && !customers.isEmpty()) {
                    sortCustomers(customers);
                    model.addAttribute("customers", customers);
                } else { // returns empty table when no customers in database
                    model.addAttribute("customers", new ArrayList<>());
                }
                role.addRoleToModel(request, model);
                return "customer/all-customers";
            } else {
                String errorMessage = "The service is busy, please try again later.";
                model.addAttribute("errorMessage", errorMessage);
                log.error("Timeout waiting for customers: {}", errorMessage);
                return "error";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "customer/loading-customers";
        } finally {
            this.latch.resetLatch();
        }
    }

    private void sortCustomers(List<Customer> customers) {
        // Sort customers by ID
        customers.sort(Comparator.comparing(Customer::getCustomerId));

        // Sort account numbers within each customer in ascending order
        customers.forEach(customer -> customer.setAccountNumbers(
                Arrays.stream(customer.getAccountNumbers().split(","))
                        .map(Long::parseLong)
                        .sorted(Long::compareTo)
                        .map(Object::toString)
                        .collect(Collectors.joining(","))
        ));
    }
}
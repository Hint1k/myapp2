package com.bank.webservice.controller;

import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.dto.User;
import com.bank.webservice.publisher.CustomerEventPublisher;
import com.bank.webservice.publisher.UserEventPublisher;
import com.bank.webservice.service.LatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class RegistrationController {

    private final CustomerEventPublisher customerPublisher;
    private final UserEventPublisher userPublisher;
    private final CustomerCache customerCache;
    private final LatchService latch;
    private static final int MAX_RESPONSE_TIME = 3; // seconds

    @Autowired
    public RegistrationController(CustomerEventPublisher customerPublisher, UserEventPublisher userPublisher,
                                  CustomerCache customerCache, LatchService latch) {
        this.customerPublisher = customerPublisher;
        this.userPublisher = userPublisher;
        this.customerCache = customerCache;
        this.latch = latch;
    }

    // Cutting off the spaces entered by user to avoid errors
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        customerPublisher.publishAllCustomersEvent();
        CountDownLatch latch = new CountDownLatch(1);
        this.latch.setLatch(latch);
        try {
            boolean latchResult = latch.await(MAX_RESPONSE_TIME, TimeUnit.SECONDS);
            if (latchResult) {
                List<Customer> customers = customerCache.getAllCustomersFromCache();
                if (customers != null && !customers.isEmpty()) {
                    sortCustomers(customers);
                    model.addAttribute("customers", customers);
                } else {
                    String errorMessage = "You have to register as a customer first";
                    model.addAttribute("errorMessage", errorMessage);
                    log.error("The customer database is empty: {}", errorMessage);
                    return "error";
                }
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
        return "registration-form";
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

    @PostMapping("/register")
    public String registerUser(@RequestParam Map<String, String> registrationData, Model model) {
        String username = registrationData.get("username");
        String password = registrationData.get("password");
        String customerNumber = registrationData.get("customerNumber");

        List<Customer> customerList = customerCache.getAllCustomersFromCache();
        // Check if the customer number exists in the customer list
        // TODO check String to Long conversion of customer number here
        Optional<Customer> matchingCustomer = customerList.stream()
                .filter(customer -> customer.getCustomerNumber().toString().equals(customerNumber))
                .findFirst();

        if (matchingCustomer.isEmpty()) {
            String errorMessage = "Invalid customer number. Please register as a customer first.";
            model.addAttribute("errorMessage", errorMessage);
            log.error("Customer number {} not found in the customer list.", customerNumber);
            return "error";
        }

        Customer customer = matchingCustomer.get();
        log.info("Customer found: {}", customer);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setCustomerNumber(customer.getCustomerNumber());
        user.setFirstName(customer.getName().split(" ")[0]);
        user.setLastName(customer.getName().split(" ")[1]);
        userPublisher.publishUserRegisteredEvent(user);

        log.info("User registered successfully: {}", username);
        return "registration-successful";
    }
}
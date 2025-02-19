package com.bank.webservice.controller;

import com.bank.webservice.cache.CustomerCache;
import com.bank.webservice.cache.UserCache;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.dto.User;
import com.bank.webservice.publisher.GenericEventPublisher;
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

    private final GenericEventPublisher publisher;
    private final CustomerCache customerCache;
    private final UserCache userCache;
    private final LatchService latch;
    private static final int MAX_RESPONSE_TIME = 3; // seconds

    @Autowired
    public RegistrationController(GenericEventPublisher publisher, CustomerCache customerCache, UserCache userCache,
                                  LatchService latch) {
        this.publisher = publisher;
        this.customerCache = customerCache;
        this.userCache = userCache;
        this.latch = latch;
    }

    // Cutting off the spaces entered by user to avoid errors
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        publisher.publishAllEvent(Customer.class);
        publisher.publishAllEvent(User.class);
        if (latch.getLatch() == null) {
            latch.setLatch(new CountDownLatch(1));
        }
        CountDownLatch latch = this.latch.getLatch();
        try {
            boolean latchResult = latch.await(MAX_RESPONSE_TIME, TimeUnit.SECONDS);
            if (latchResult) {
                List<Customer> customers = customerCache.getAllCustomersFromCache();
                List<User> users = userCache.getAllUsersFromCache();
                List<Long> freeCustomerNumbers = getFreeCustomerNumbers(customers, users);
                if (freeCustomerNumbers.isEmpty()) {
                    model.addAttribute("errorMessage", "There is no unregistered customers." +
                            " Log in as admin and create a new customer to proceed.");
                    return "error";
                }
                model.addAttribute("customerNumbers", freeCustomerNumbers);
            } else {
                model.addAttribute("errorMessage",
                        "The service is busy, please try again later.");
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

    private List<Long> getFreeCustomerNumbers(List<Customer> customers, List<User> users) {
        List<Long> userCustomerNumbers = users.stream()
                .map(User::getCustomerNumber)
                .toList();
        // Filtering customer numbers that are not in the userCustomerNumbers list
        return customers.stream()
                .map(Customer::getCustomerNumber)
                .filter(customerNumber -> !userCustomerNumbers.contains(customerNumber))
                .collect(Collectors.toList());
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam Map<String, String> registrationData) {
        String username = registrationData.get("username");
        String password = registrationData.get("password");
        Long customerNumber = Long.parseLong(registrationData.get("customerNumber"));
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setCustomerNumber(customerNumber);
        publisher.publishCreatedEvent(user);
        return "registration-successful";
    }
}
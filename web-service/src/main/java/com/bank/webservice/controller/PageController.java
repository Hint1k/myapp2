package com.bank.webservice.controller;

import com.bank.webservice.dto.Account;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.publisher.GenericPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class PageController {

    private final GenericPublisher publisher;

    @Autowired
    public PageController(GenericPublisher publisher) {
        this.publisher = publisher;

    }

    @GetMapping("/admin/index")
    public String showAdminIndexPage() {
        //TODO make a separate page for admins
        return "home";
    }

    @GetMapping("/manager/index")
    public String showManagerIndexPage() {
        //TODO make a separate page for managers
        return "home";
    }

    @GetMapping("/user/index")
    public String showUserIndexPage() {
        //TODO make a separate page for users
        return "home";
    }

    @GetMapping("/error")
    public String showErrorPage() {
        return "error";
    }

    @GetMapping("/index")
    public String showLoginPage() {
        // Preload the cache data to avoid empty lists in some cases
        publisher.publishAllEvent(Account.class);
        publisher.publishAllEvent(Transaction.class);
        publisher.publishAllEvent(Customer.class);
        return "index";
    }

    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }

    @GetMapping("/access-denied")
    public String showAccessDeniedPage() {
        return "access-denied";
    }
}
package com.bank.webservice.controller;

import com.bank.webservice.publisher.AccountEventPublisher;
import com.bank.webservice.publisher.CustomerEventPublisher;
import com.bank.webservice.publisher.TransactionEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    AccountEventPublisher accountPublisher;
    TransactionEventPublisher transactionPublisher;
    CustomerEventPublisher customerPublisher;

    @Autowired
    public PageController(AccountEventPublisher accountPublisher, TransactionEventPublisher transactionPublisher,
                          CustomerEventPublisher customerPublisher) {
        this.accountPublisher = accountPublisher;
        this.transactionPublisher = transactionPublisher;
        this.customerPublisher = customerPublisher;
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
        accountPublisher.publishAllAccountsEvent();
        transactionPublisher.publishAllTransactionsEvent();
        customerPublisher.publishAllCustomersEvent();

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
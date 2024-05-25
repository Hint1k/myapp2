package com.demo.AccountService.controller;

import com.demo.AccountService.entity.Account;
import com.demo.AccountService.util.AccountStatus;
import com.demo.AccountService.util.AccountType;
import com.demo.AccountService.util.Currency;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class AccountController {

    @GetMapping("/account")
    public String showAccountPage(Model model) {
        Account account = getAccountDetails();
        model.addAttribute("account", account);
        return "account";
    }

    // Temp method to retrieve account details - replace later with access to database
    private Account getAccountDetails() {
        return new Account(1L, 1234567890L, BigDecimal.valueOf(1000.00),
                Currency.USD, AccountType.SAVINGS, AccountStatus.ACTIVE,
                LocalDate.now(), null, 1L);
    }
}
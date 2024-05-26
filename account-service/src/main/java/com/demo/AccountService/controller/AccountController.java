package com.demo.AccountService.controller;

import com.demo.AccountService.entity.Account;
import com.demo.AccountService.entity.TransactionHistory;
import com.demo.AccountService.service.AccountService;
import com.demo.AccountService.service.TransactionHistoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionHistoryService transactionHistoryService;

    @GetMapping("/accounts/account")
    private String getAccountDetails(Model model) {
        Long accountId = 1L; // <<<<<<===================================== temp code
        Account account = accountService.findAccountById(accountId);
        model.addAttribute("account", account);
        return "current-account";
    }

    @GetMapping("/accounts/new")
    private String addNewAccount(Model model) {
        model.addAttribute("account", new Account());
        List<TransactionHistory> transactions = transactionHistoryService.findAll();
        model.addAttribute("transactions", transactions);
        return "new-account";
    }

    @PostMapping("/accounts")
    public String saveNewAccount(
//            @Valid
                                 @ModelAttribute("account") Account account,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            //bindingResult.getAllErrors().forEach(error -> System.out.println(error.toString()));
            return "new-account";
        }
        accountService.saveAccount(account);
        return "redirect:/index";
    }
}
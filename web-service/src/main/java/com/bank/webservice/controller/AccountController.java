package com.bank.webservice.controller;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.publisher.AccountEventPublisher;
import com.bank.webservice.service.LatchService;
import com.bank.webservice.service.ValidationService;
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
public class AccountController {

    private final LatchService latch;
    private final AccountEventPublisher publisher;
    private final AccountCache cache;
    private final ValidationService validator;
    private static final int MAX_RESPONSE_TIME = 3; // seconds

    @Autowired
    public AccountController(LatchService latch, AccountEventPublisher publisher,
                             AccountCache cache, ValidationService validator) {
        this.latch = latch;
        this.publisher = publisher;
        this.cache = cache;
        this.validator = validator;
    }

    // Cutting off the spaces entered by user to avoid errors
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/accounts/new-account")
    private String showNewAccountForm(Model model) {
        Account account = new Account();
        model.addAttribute("account", account);
        return "account/new-account";
    }

    @PostMapping("/accounts")
    public String createAccount(@Valid @ModelAttribute("account") Account newAccount, BindingResult bindingResult) {
        validator.validateAccountIsNotExist(newAccount, bindingResult);
        validator.validateCustomerExists(newAccount, bindingResult);
        if (bindingResult.hasErrors()) {
            log.error("Account saving failed due to validation errors: {}", bindingResult.getAllErrors());
            return "account/new-account";
        }
        publisher.publishAccountCreatedEvent(newAccount);
        return "redirect:/index";
    }

    @PutMapping("/accounts/{accountId}")
    public String showUpdateAccountForm(@PathVariable("accountId") Long accountId, Model model) {
        Account account = cache.getAccountFromCache(accountId);
        model.addAttribute("account", account);
        return "account/account-update";
    }

    @PostMapping("/accounts/account")
    public String updateAccount(@ModelAttribute("account") Account newAccount, BindingResult bindingResult) {
        validator.validateCustomerExists(newAccount, bindingResult);
        if (bindingResult.hasErrors()) {
            log.error("Account update failed due to validation errors: {}", bindingResult.getAllErrors());
            return "account/account-update";
        }
        publisher.publishAccountUpdatedEvent(newAccount);
        return "redirect:/index";
    }

    @DeleteMapping("/accounts/{accountId}")
    public String deleteAccount(@PathVariable("accountId") Long accountId) {
        publisher.publishAccountDeletedEvent(accountId);
        return "redirect:/index";
    }

    @GetMapping("/accounts/{accountId}")
    public String getAccount(@PathVariable("accountId") Long accountId, Model model) {
        publisher.publishAccountDetailsEvent(accountId);
        Account account = cache.getAccountFromCache(accountId);
        if (account != null) {
            model.addAttribute("account", account);
            return "account/account-details";
        } else {
            model.addAttribute("accountId", accountId);
            return "account/loading-accounts";
        }
    }

    @GetMapping("/accounts/all-accounts")
    public String getAllAccounts(Model model) {
        return handleAccountsRetrieval(model, null);
    }

    @GetMapping("/accounts/all-accounts/{customerNumber}")
    public String getAccountsByCustomerNumber(@PathVariable("customerNumber") Long customerNumber, Model model) {
        return handleAccountsRetrieval(model, customerNumber);
    }

    private String handleAccountsRetrieval(Model model, Long customerNumber) {
        publisher.publishAllAccountsEvent();
        CountDownLatch latch = new CountDownLatch(1);
        this.latch.setLatch(latch);
        try {
            boolean latchResult = latch.await(MAX_RESPONSE_TIME, TimeUnit.SECONDS);
            if (latchResult) {
                List<Account> accounts;
                if (customerNumber == null) {
                    accounts = cache.getAllAccountsFromCache();
                } else {
                    accounts = cache.getAccountsFromCacheByCustomerNumber(customerNumber);
                }
                if (accounts != null && !accounts.isEmpty()) {
                    accounts.sort(Comparator.comparing(Account::getAccountId));
                    model.addAttribute("accounts", accounts);
                } else {
                    model.addAttribute("accounts", new ArrayList<>());
                }
                return "account/all-accounts";
            } else {
                String errorMessage = "The service is busy, please try again later.";
                model.addAttribute("errorMessage", errorMessage);
                log.error("Timeout waiting for transactions: {}", errorMessage);
                return "error";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "account/loading-accounts";
        } finally {
            this.latch.resetLatch();
        }
    }
}
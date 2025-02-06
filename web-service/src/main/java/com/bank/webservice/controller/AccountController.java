package com.bank.webservice.controller;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.publisher.AccountEventPublisher;
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
    private final RoleService role;
    private static final int MAX_RESPONSE_TIME = 3; // seconds

    @Autowired
    public AccountController(LatchService latch, AccountEventPublisher publisher,
                             AccountCache cache, ValidationService validator, RoleService role) {
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
        return "redirect:/home";
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
        return "redirect:/home";
    }

    @DeleteMapping("/accounts/{accountId}")
    public String deleteAccount(@PathVariable("accountId") Long accountId) {
        publisher.publishAccountDeletedEvent(accountId);
        return "redirect:/home";
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
    public String getAllAccounts(Model model, HttpServletRequest request) {
        // Check if the customer number is present in the request attribute (set by FilterServiceImpl)
        String customerNumber = (String) request.getAttribute("customerNumber");

        if (customerNumber != null) {
            log.info("Filtering accounts for customer number: {}", customerNumber);
            // Delegate to getAccountsByCustomerNumber to handle customer-specific filtering
            return getAccountsByCustomerNumber(Long.parseLong(customerNumber), model, request);
        }

        // If no customer number, retrieve all accounts
        return handleAccountsRetrieval(model, request, null);
    }

    @GetMapping("/accounts/all-accounts/{customerNumber}")
    public String getAccountsByCustomerNumber(@PathVariable("customerNumber") Long customerNumber, Model model,
                                              HttpServletRequest request) {
        return handleAccountsRetrieval(model, request, customerNumber);
    }

    private String handleAccountsRetrieval(Model model, HttpServletRequest request, Long customerNumber) {
        publisher.publishAllAccountsEvent();
        if (latch.getLatch() == null) {
            latch.setLatch(new CountDownLatch(1));
        }
        CountDownLatch latch = this.latch.getLatch();
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
                role.addRoleToModel(request, model);
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
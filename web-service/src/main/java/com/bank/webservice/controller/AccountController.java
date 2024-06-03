package com.bank.webservice.controller;

import com.bank.webservice.cache.AccountCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.publisher.AccountEventPublisher;
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
import java.util.List;

@Controller
@RequestMapping("/api")
@Slf4j
public class AccountController {

    @Autowired
    private AccountEventPublisher publisher;

    @Autowired
    private AccountCache cache;

    // cutting off the spaces entered by user to avoid errors
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor
                = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/accounts/{accountId}")
    public String getAccountById(@PathVariable("accountId") Long accountId, Model model) {
        Account account = new Account();
        account.setAccountId(accountId);
        publisher.publishAccountDetailsEvent(account);
        account = cache.getFromCacheById(accountId);
        if (account != null) {
            model.addAttribute("account", account);
            return "account-details";
        } else {
            model.addAttribute("accountId", accountId);
            return "loading-page";
        }
    }

    @GetMapping("/accounts/new-account")
    private String showCreateAccountForm(Model model) {
        Account account = new Account();
        model.addAttribute("account", account);
        return "new-account";
    }

    @PostMapping("/accounts")
    public String createAccount(@Valid @ModelAttribute("account") Account account, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Account saving failed due to validation errors: {}",
                    bindingResult.getAllErrors());
            return "new-account";
        }
        publisher.publishAccountCreatedEvent(account);
        return "redirect:/index";
    }

    @GetMapping("/accounts/all-accounts")
    public String getAllAccounts(Model model) {
        List<Account> accounts = new ArrayList<>();
        publisher.publishAllAccountsEvent(accounts);
        accounts = cache.getAllAccountsFromCache();
        if (accounts != null && !accounts.isEmpty()) {
            model.addAttribute("accounts", accounts);
            return "all-accounts";
        } else {
            return "loading-page";
        }
    }

    @DeleteMapping("/accounts/{accountId}")
    public String deleteAccountById(@PathVariable("accountId") Long accountId) {
        publisher.publishAccountDeletedEvent(accountId);
        return "redirect:/index";
    }

    @PutMapping("/accounts/{accountId}")
    public String showUpdateAccountForm(@PathVariable("accountId") Long accountId, Model model) {
        Account account = cache.getFromCacheById(accountId);
        model.addAttribute("account", account);
        return "account-update";
    }

    @PostMapping("/accounts/account")
    public String updateAccountById(@Valid @ModelAttribute("account") Account account, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Account update failed due to validation errors: {}",
                    bindingResult.getAllErrors());
            return "account-update";
        }
        publisher.publishAccountUpdatedEvent(account);
        return "redirect:/index";
    }
}
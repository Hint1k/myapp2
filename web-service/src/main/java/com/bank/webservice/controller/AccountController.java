package com.bank.webservice.controller;

import com.bank.webservice.cache.AccountCreatedCache;
import com.bank.webservice.cache.AccountDetailsCache;
import com.bank.webservice.cache.AllAccountsCache;
import com.bank.webservice.dto.Account;
import com.bank.webservice.publisher.AccountEventPublisher;
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

    @Autowired // TODO make it just one cache field
    private AccountDetailsCache cache1;

    @Autowired
    private AllAccountsCache cache2;

    @Autowired
    private AccountCreatedCache cache3;

    // cutting off the spaces entered by user to avoid errors
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor
                = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/accounts/account")
    public String getAccountById(@RequestParam("accountId")
                                 Long accountId,
                                 Model model) {
        Account account = new Account();
        account.setId(accountId);
        publisher.publishAccountDetailsRequestedEvent(account);

        account = cache1.getAccountDetails(accountId);
        if (account != null) {
            model.addAttribute("account", account);
            return "account-details";
        } else {
            model.addAttribute("accountId", accountId);
            return "loading-page";
        }
    }

    @GetMapping("/accounts/new")
    private String addAccount(Model model) {
        model.addAttribute("account", new Account());
        return "new-account";
    }

    @PostMapping("/accounts")
    public String createAccount(
//          @Valid // temp turned off
            @ModelAttribute("account") Account account,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Account saving failed due to validation errors: {}",
                    bindingResult.getAllErrors());
            return "new-account";
        }
        publisher.publishAccountCreatedEvent(account);
        cache3.getAccount(account.getAccountNumber());
        return "redirect:/index";
    }

    @GetMapping("/accounts/all")
    public String getAllAccounts(Model model) {
        List<Account> accounts = new ArrayList<>();
        publisher.publishAllAccountsEvent(accounts);
        accounts = cache2.getAllAccounts();
        if (accounts != null && !accounts.isEmpty()) {
            model.addAttribute("accounts", accounts);
            return "all-accounts";
        } else {
            return "loading-page";
        }
    }
}
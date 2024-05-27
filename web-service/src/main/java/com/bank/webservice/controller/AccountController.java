package com.bank.webservice.controller;

import com.bank.webservice.dto.AccountDTO;
import com.bank.webservice.publisher.AccountEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api")
@Slf4j
public class AccountController {

    @Autowired
    private AccountEventPublisher publisher;

    private ThreadLocal<AccountDTO> accountDTO = new ThreadLocal<>(); // Thread-safe storage

    //temp solution with tight coupling
    public void setAccountDetails(AccountDTO account) {
        accountDTO.set(account);
    }

    @GetMapping("/accounts/account")
    public String getAccountDetails(@RequestParam("accountId")
                                    Long accountId,
                                    Model model) {
        publisher.publishAccountDetailsRequestedEvent(accountId);
        AccountDTO account = accountDTO.get();
        if (account != null) {
            model.addAttribute("account", account);
            return "current-account";
        } else {
            return "loading-page";
        }
    }

    @GetMapping("/accounts/new")
    private String addAccount(Model model) {
        model.addAttribute("account", new AccountDTO());
        return "new-account";
    }

    @PostMapping("/accounts")
    public String createAccount(
//          @Valid // temp turned off
            @ModelAttribute("account") AccountDTO account,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Account saving failed due to validation errors: {}",
                    bindingResult.getAllErrors());
            return "new-account";
        }
        publisher.publishAccountCreatedEvent(account);
        return "redirect:/index";
    }
}
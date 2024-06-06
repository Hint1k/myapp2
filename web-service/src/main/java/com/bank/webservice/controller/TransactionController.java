package com.bank.webservice.controller;

import com.bank.webservice.cache.TransactionCache;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.publisher.TransactionEventPublisher;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequestMapping("/api")
public class TransactionController {

    @Autowired
    private TransactionEventPublisher publisher;

    @Autowired
    private TransactionCache cache;

    // cutting off the spaces entered by user to avoid errors
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor
                = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/transactions/new-transaction")
    private String showNewTransactionForm(Model model) {
        Transaction transaction = new Transaction();
        model.addAttribute("transaction", transaction);
        return "new-transaction";
    }

    @PostMapping("/transactions")
    public String createTransaction(@Valid @ModelAttribute("transaction") Transaction transaction,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Transaction submission failed due to validation errors: {}",
                    bindingResult.getAllErrors());
            return "new-transaction";
        }
        publisher.publishTransactionCreatedEvent(transaction);
        return "redirect:/index";
    }
}
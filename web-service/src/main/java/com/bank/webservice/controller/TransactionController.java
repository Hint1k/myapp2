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

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/api")
public class TransactionController {

    private final TransactionEventPublisher publisher;

    private final TransactionCache cache;

    @Autowired
    public TransactionController(TransactionEventPublisher publisher, TransactionCache cache) {
        this.publisher = publisher;
        this.cache = cache;
    }

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

    @GetMapping("/transactions/all-transactions")
    public String getAllTransactions(Model model) {
        List<Transaction> transactions = new ArrayList<>();
        publisher.publishAllTransactionsEvent(transactions);
        transactions = cache.getAllTransactionsFromCache();
        if (transactions != null && !transactions.isEmpty()) {
            model.addAttribute("transactions", transactions);
            return "all-transactions";
        } else {
            return "loading-transactions";
        }
    }

    @DeleteMapping("/transactions/{transactionId}")
    public String deleteTransactionById(@PathVariable("transactionId") Long transactionId) {
        publisher.publishTransactionDeletedEvent(transactionId);
        return "redirect:/index";
    }

    @PutMapping("/transactions/{transactionId}")
    public String showUpdateTransactionForm(@PathVariable("transactionId") Long transactionId, Model model) {
        Transaction transaction = cache.getFromCacheById(transactionId);
        model.addAttribute("transaction", transaction);
        return "transaction-update";
    }

    @PostMapping("/transactions/transaction")
    public String updateTransactionById(@Valid @ModelAttribute("transaction") Transaction transaction,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Transaction update failed due to validation errors: {}",
                    bindingResult.getAllErrors());
            return "transaction-update";
        }
        publisher.publishTransactionUpdatedEvent(transaction);
        return "redirect:/index";
    }

    @GetMapping("/transactions/{transactionId}")
    public String getTransactionById(@PathVariable("transactionId") Long transactionId, Model model) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        publisher.publishTransactionDetailsEvent(transaction);
        transaction = cache.getFromCacheById(transactionId);
        if (transaction != null) {
            model.addAttribute("transaction", transaction);
            return "transaction-details";
        } else {
            model.addAttribute("transactionId", transactionId);
            return "loading-transactions";
        }
    }
}
package com.bank.webservice.controller;

import com.bank.webservice.cache.TransactionCache;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.publisher.TransactionEventPublisher;
import com.bank.webservice.service.LatchService;
import com.bank.webservice.service.TransactionValidationService;
import com.bank.webservice.util.TransactionStatus;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

@Controller
@Slf4j
@RequestMapping("/api")
public class TransactionController {

    private final TransactionEventPublisher publisher;
    private final TransactionCache cache;
    private final LatchService latch;
    private final TransactionValidationService validator;
    private static final int MAX_RESPONSE_TIME = 3; // seconds

    @Autowired
    public TransactionController(TransactionEventPublisher publisher, TransactionCache cache, LatchService latch,
                                 TransactionValidationService validator) {
        this.publisher = publisher;
        this.cache = cache;
        this.latch = latch;
        this.validator = validator;
    }

    // Cutting off the spaces entered by user to avoid errors
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
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
        validator.validateTransaction(transaction, bindingResult);
        if (bindingResult.hasErrors()) {
            log.error("Transaction submission failed due to validation errors: {}", bindingResult.getAllErrors());
            return "new-transaction";
        }
        publisher.publishTransactionCreatedEvent(transaction);
        return "redirect:/index";
    }

    @PutMapping("/transactions/{transactionId}")
    public String showUpdateTransactionForm(@PathVariable("transactionId") Long transactionId, Model model) {
        Transaction transaction = cache.getTransactionFromCache(transactionId);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        model.addAttribute("transaction", transaction);
        return "transaction-update";
    }

    @PostMapping("/transactions/transaction")
    public String updateTransaction(@Valid @ModelAttribute("transaction") Transaction transaction,
                                    BindingResult bindingResult) {
        validator.validateTransaction(transaction, bindingResult);
        if (bindingResult.hasErrors()) {
            log.error("Transaction update failed due to validation errors: {}", bindingResult.getAllErrors());
            return "transaction-update";
        }
        publisher.publishTransactionUpdatedEvent(transaction);
        return "redirect:/index";
    }

    @DeleteMapping("/transactions/{transactionId}")
    public String deleteTransaction(@PathVariable("transactionId") Long transactionId) {
        publisher.publishTransactionDeletedEvent(transactionId);
        return "redirect:/index";
    }

    @GetMapping("/transactions/{transactionId}")
    public String getTransaction(@PathVariable("transactionId") Long transactionId, Model model) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        publisher.publishTransactionDetailsEvent(transaction);
        transaction = cache.getTransactionFromCache(transactionId);
        if (transaction != null) {
            model.addAttribute("transaction", transaction);
            return "transaction-details";
        } else {
            model.addAttribute("transactionId", transactionId);
            return "loading-transactions";
        }
    }

    @GetMapping("/transactions/all-transactions")
    public String getAllTransactions(Model model) {
        return handleTransactionsRetrieval(model, null);
    }

    @GetMapping("/transactions/all-transactions/{accountNumber}")
    public String getTransactionsByAccountNumber(@PathVariable("accountNumber") Long accountNumber, Model model) {
        return handleTransactionsRetrieval(model, accountNumber);
    }

    private String handleTransactionsRetrieval(Model model, Long accountNumber) {
        publisher.publishAllTransactionsEvent();
        CountDownLatch latch = new CountDownLatch(1);
        this.latch.setLatch(latch);
        try {
            boolean latchResult = latch.await(MAX_RESPONSE_TIME, TimeUnit.SECONDS);
            if (latchResult) {
                List<Transaction> transactions;
                if (accountNumber == null) {
                    transactions = cache.getAllTransactionsFromCache();
                } else {
                    transactions = cache.getAccountTransactionsFromCache(accountNumber);
                }
                if (transactions != null && !transactions.isEmpty()) {
                    transactions.sort(Comparator.comparing(Transaction::getTransactionId));
                    model.addAttribute("transactions", transactions);
                } else {
                    model.addAttribute("transactions", new ArrayList<>());
                }
                return "all-transactions";
            } else {
                String errorMessage = "The service is busy, please try again later.";
                model.addAttribute("errorMessage", errorMessage);
                log.error("Timeout waiting for transactions: {}", errorMessage);
                return "error";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "loading-transactions";
        } finally {
            this.latch.resetLatch();
        }
    }
}
package com.bank.webservice.controller;

import com.bank.webservice.cache.TransactionCache;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.publisher.TransactionEventPublisher;
import com.bank.webservice.service.LatchService;
import com.bank.webservice.service.RoleService;
import com.bank.webservice.service.ValidationService;
import com.bank.webservice.util.TransactionStatus;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

@Controller
@Slf4j
@RequestMapping("/api")
public class TransactionController {

    private final TransactionEventPublisher publisher;
    private final TransactionCache cache;
    private final LatchService latch;
    private final ValidationService validator;
    private final RoleService role;
    private static final int MAX_RESPONSE_TIME = 3; // seconds

    @Autowired
    public TransactionController(TransactionEventPublisher publisher, TransactionCache cache, LatchService latch,
                                 ValidationService validator, RoleService role) {
        this.publisher = publisher;
        this.cache = cache;
        this.latch = latch;
        this.validator = validator;
        this.role = role;
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
        return "transaction/new-transaction";
    }

    @PostMapping("/transactions")
    public String createTransaction(@Valid @ModelAttribute("transaction") Transaction transaction,
                                    BindingResult bindingResult) {
        validator.validateTransaction(transaction, bindingResult);
        if (bindingResult.hasErrors()) {
            log.error("Transaction submission failed due to validation errors: {}", bindingResult.getAllErrors());
            return "transaction/new-transaction";
        }
        publisher.publishTransactionCreatedEvent(transaction);
        return "redirect:/home";
    }

    @PutMapping("/transactions/{transactionId}")
    public String showUpdateTransactionForm(@PathVariable("transactionId") Long transactionId, Model model) {
        Transaction transaction = cache.getTransactionFromCache(transactionId);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        model.addAttribute("transaction", transaction);
        return "transaction/transaction-update";
    }

    @PostMapping("/transactions/transaction")
    public String updateTransaction(@Valid @ModelAttribute("transaction") Transaction transaction,
                                    BindingResult bindingResult) {
        validator.validateTransaction(transaction, bindingResult);
        if (bindingResult.hasErrors()) {
            log.error("Transaction update failed due to validation errors: {}", bindingResult.getAllErrors());
            return "transaction/transaction-update";
        }
        publisher.publishTransactionUpdatedEvent(transaction);
        return "redirect:/home";
    }

    @DeleteMapping("/transactions/{transactionId}")
    public String deleteTransaction(@PathVariable("transactionId") Long transactionId) {
        publisher.publishTransactionDeletedEvent(transactionId);
        return "redirect:/home";
    }

    @GetMapping("/transactions/{transactionId}")
    public String getTransaction(@PathVariable("transactionId") Long transactionId, Model model) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        publisher.publishTransactionDetailsEvent(transaction);
        transaction = cache.getTransactionFromCache(transactionId);
        if (transaction != null) {
            model.addAttribute("transaction", transaction);
            return "transaction/transaction-details";
        } else {
            model.addAttribute("transactionId", transactionId);
            return "transaction/loading-transactions";
        }
    }

    @GetMapping("/transactions/all-transactions")
    public String getAllTransactions(Model model, HttpServletRequest request) {
        // Check if the customer number is present in the request attribute (set by FilterServiceImpl)
        String customerNumber = (String) request.getAttribute("customerNumber");

        if (customerNumber != null) {
            log.info("Filtering transactions for customer number: {}", customerNumber);
            // Delegate to getAccountsByCustomerNumber to handle customer-specific filtering
            return getTransactionsByAccountNumber(Long.parseLong(customerNumber), model, request);
        }

        // If no customer number, retrieve all transactions
        return handleTransactionsRetrieval(model, request, null);
    }

    @GetMapping("/transactions/all-transactions/{accountNumber}")
    public String getTransactionsByAccountNumber(@PathVariable("accountNumber") Long accountNumber, Model model,
                                                 HttpServletRequest request) {
        return handleTransactionsRetrieval(model, request, accountNumber);
    }

    private String handleTransactionsRetrieval(Model model, HttpServletRequest request, Long accountNumber) {
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
                role.addRoleToModel(request, model);
                return "transaction/all-transactions";
            } else {
                String errorMessage = "The service is busy, please try again later.";
                model.addAttribute("errorMessage", errorMessage);
                log.error("Timeout waiting for transactions: {}", errorMessage);
                return "error";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "transaction/loading-transactions";
        } finally {
            this.latch.resetLatch();
        }
    }
}
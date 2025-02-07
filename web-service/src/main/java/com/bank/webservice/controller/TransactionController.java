package com.bank.webservice.controller;

import com.bank.webservice.cache.AccountCache;
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
    private final TransactionCache transactionCache;
    private final AccountCache accountCache;
    private final LatchService latch;
    private final ValidationService validator;
    private final RoleService role;
    private static final int MAX_RESPONSE_TIME = 3; // seconds

    @Autowired
    public TransactionController(TransactionEventPublisher publisher, AccountCache accountCache, LatchService latch,
                                 ValidationService validator, TransactionCache transactionCache, RoleService role) {
        this.publisher = publisher;
        this.transactionCache = transactionCache;
        this.accountCache = accountCache;
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
        Transaction transaction = transactionCache.getTransactionFromCache(transactionId);
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
        publisher.publishTransactionDetailsEvent(transactionId);
        Transaction transaction = transactionCache.getTransactionFromCache(transactionId);
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
        Long customerNumber = null;
        String customerNumberString = (String) request.getAttribute("customerNumber");

        if (customerNumberString != null) {
            try {
                customerNumber = Long.parseLong(customerNumberString);
            } catch (NumberFormatException e) {
                log.warn("Invalid customer number format: {}", customerNumberString);
            }
        }
        return handleTransactionsRetrieval(model, request, null, customerNumber);
    }

    @GetMapping("/transactions/all-transactions/{accountNumber}")
    public String getTransactionsByAccountNumber(@PathVariable("accountNumber") Long accountNumber, Model model,
                                                 HttpServletRequest request) {
        return handleTransactionsRetrieval(model, request, accountNumber, null);
    }

    private String handleTransactionsRetrieval(Model model, HttpServletRequest request, Long accountNumber,
                                               Long customerNumber) {
        publisher.publishAllTransactionsEvent();
        if (latch.getLatch() == null) {
            latch.setLatch(new CountDownLatch(1));
        }
        CountDownLatch latch = this.latch.getLatch();
        try {
            boolean latchResult = latch.await(MAX_RESPONSE_TIME, TimeUnit.SECONDS);
            if (latchResult) {
                List<Transaction> transactions = new ArrayList<>();
                if (customerNumber == null) {
                    if (accountNumber == null) {
                        // Case 1: All transactions (admin or manager request)
                        transactions = transactionCache.getAllTransactionsFromCache();
                    } else {
                        // Case 2: Transactions for a specific account
                        transactions = transactionCache.getTransactionsForAccountFromCache(accountNumber);
                    }
                } else {
                    // Case 3: Transactions for all accounts belonging to the customer
                    List<Long> accountNumbers =
                            accountCache.getAccountNumbersFromCacheByCustomerNumber(customerNumber);
                    if (accountNumbers == null || accountNumbers.isEmpty()) {
                        log.warn("No accounts found for customer number: {}", customerNumber);
                    } else {
                        transactions = transactionCache.getTransactionsForMultipleAccountsFromCache(accountNumbers);
                    }
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
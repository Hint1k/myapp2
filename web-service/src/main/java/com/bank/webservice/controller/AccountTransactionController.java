package com.bank.webservice.controller;

import com.bank.webservice.cache.AccountTransactionCache;
import com.bank.webservice.dto.Transaction;
import com.bank.webservice.publisher.AccountTransactionEventPublisher;
import com.bank.webservice.service.LatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
@RequestMapping("/api")
public class AccountTransactionController {

    private static final int MAX_RESPONSE_TIME = 3; // seconds
    private final LatchService latchService;
    private final AccountTransactionEventPublisher publisher;
    private final AccountTransactionCache cache;

    @Autowired
    public AccountTransactionController(LatchService latchService, AccountTransactionEventPublisher publisher,
                                        AccountTransactionCache cache) {
        this.latchService = latchService;
        this.publisher = publisher;
        this.cache = cache;
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public String getAccountTransactions(@PathVariable("accountId") Long accountId, Model model) {
        List<Transaction> transactions = new ArrayList<>();
        publisher.publishAccountTransactionEvent(accountId, transactions);

        CountDownLatch latch = new CountDownLatch(1);
        latchService.setLatch(latch);

        try {
            boolean latchResult = latch.await(MAX_RESPONSE_TIME, TimeUnit.SECONDS);
            if (latchResult) {
                transactions = cache.getTransactionsFromCacheByAccountId(accountId);
                if (transactions != null && !transactions.isEmpty()) {
                    model.addAttribute("transactions", transactions);
                    return "all-transactions";
                } else {
                    model.addAttribute("transactions", new ArrayList<>());
                    return "all-transactions";
                }
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
            latchService.resetLatch();
        }
    }
}
package com.bank.accountservice.service.impl;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.publisher.AccountEventPublisher;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.service.CustomerService;
import com.bank.accountservice.util.AccountStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final AccountRepository repository;
    private final AccountEventPublisher publisher;

    @Override
    @Transactional
    public void updateAccountDueToCustomerChange(Long customerNumber, String accountNumbers) {
        removeCustomerNumberFromAccount(customerNumber);
        if (accountNumbers != null && !accountNumbers.isEmpty()) {
            assignCustomerNumberToAccount(customerNumber, accountNumbers);
        }
    }

    private void removeCustomerNumberFromAccount(Long customerNumber) {
        Account account = repository.findAccountByCustomerNumber(customerNumber);
        if (account != null) {
            /* "0L" = no customer assigned. It is needed for customer-service module of the project.
            It helps to ignore unrelated account updates. */
            account.setCustomerNumber(0L);
            account.setAccountStatus(AccountStatus.INACTIVE); // status changed since no customer assigned
            updateAccount(account);
            publisher.publishAccountUpdatedEvent(account);
        }
    }

    private void assignCustomerNumberToAccount(Long customerNumber, String accountNumbers) {
        String[] accountNumbersArray = accountNumbers.split(","); // string format is set in web-service
        List<Long> accuntNumbersList = new ArrayList<>();
        for (String accountNumber : accountNumbersArray) {
            accuntNumbersList.add(Long.parseLong(accountNumber)); // long type is checked in web-service
        }
        for (Long accountNumber : accuntNumbersList) {
            Account account = repository.findAccountByAccountNumber(accountNumber);
            if (account != null) {
                account.setCustomerNumber(customerNumber);
                updateAccount(account);
                publisher.publishAccountDetailsEvent(account);
            } else {
                log.error("Account with account number: {} not found", accountNumber);
                throw new EntityNotFoundException("Account with account number " + accountNumber + " not found");
            }
        }
    }

    private void updateAccount(Account account) {
        repository.save(account);
        log.info("Account with id: {} updated due to customer change", account.getAccountId());
    }
}
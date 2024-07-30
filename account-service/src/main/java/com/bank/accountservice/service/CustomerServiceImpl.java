package com.bank.accountservice.service;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.publisher.AccountEventPublisher;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.util.AccountStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final AccountRepository repository;
    private final AccountEventPublisher publisher;

    @Autowired
    public CustomerServiceImpl(AccountRepository repository, AccountEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

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
            account.setCustomerNumber(0L); // 0 means no customer assigned to this account
            account.setAccountStatus(AccountStatus.INACTIVE); // status changed since no customer assigned
            updateAccount(account);
            publisher.publishSuspendTransactionEvent(account);
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
                // TODO return message to the web-service
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
package com.bank.customerservice.service;

public interface AccountService {

    void updateCustomerDueToAccountChange(Long customerNumber, String accountNumber);
}
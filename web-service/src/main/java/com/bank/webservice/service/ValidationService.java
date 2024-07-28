package com.bank.webservice.service;

import com.bank.webservice.dto.Account;
import com.bank.webservice.dto.Customer;
import com.bank.webservice.dto.Transaction;
import org.springframework.validation.BindingResult;

public interface ValidationService {

    void validateTransaction(Transaction transaction, BindingResult bindingResult);

    void validateCustomer(Customer newCustomer, BindingResult bindingResult);

    void validateCustomerExists(Account newAccount, BindingResult bindingResult);

    void validateAccountIsNotExist(Account newAccount, BindingResult bindingResult);

    void validateMultipleAccountsBelongToCustomer(Customer oldCustomer, BindingResult bindingResult);
}
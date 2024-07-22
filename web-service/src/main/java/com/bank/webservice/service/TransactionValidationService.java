package com.bank.webservice.service;

import com.bank.webservice.dto.Transaction;
import org.springframework.validation.BindingResult;

public interface TransactionValidationService {

    void validateTransaction(Transaction transaction, BindingResult bindingResult);
}
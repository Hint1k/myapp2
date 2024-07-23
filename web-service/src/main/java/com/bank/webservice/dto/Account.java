package com.bank.webservice.dto;

import com.bank.webservice.util.Currency;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.AccountType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import static java.lang.Integer.MAX_VALUE;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {

    private Long accountId;

    @NotNull(message = "Account number is required")
    @Min(value = 1)
    @Digits(integer = MAX_VALUE, fraction = 0, message = "Only whole numbers are allowed")
    private Long accountNumber;

    @NotNull(message = "Balance is required")
    @Min(value = 0)
    @Digits(integer = MAX_VALUE, fraction = 2, message = "Only 2 decimal places are allowed")
    private BigDecimal balance;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Account status is required")
    private AccountStatus accountStatus;

    @NotNull(message = "Open date is required")
    private LocalDate openDate;

    @NotNull(message = "Customer ID is required")
    @Min(value = 1)
    @Digits(integer = MAX_VALUE, fraction = 0, message = "Only whole numbers are allowed")
    private Long customerId;

    {
        // sets account open date = current date
        this.openDate = LocalDate.now();
        // sets initial balance
        this.balance = BigDecimal.ZERO;
    }

    // no account id
    public Account(Long accountNumber, BigDecimal balance,
                   Currency currency, AccountType accountType,
                   AccountStatus accountStatus, LocalDate openDate,
                   Long customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.openDate = openDate;
        this.customerId = customerId;
    }
}
package com.bank.accountservice.event.account;

import com.bank.accountservice.util.AccountStatus;
import com.bank.accountservice.util.AccountType;
import com.bank.accountservice.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdatedEvent {

    // TODO remove the fields that cannot be updated later
    private Long accountId;  // this field can't be removed
    private Long accountNumber;
    private BigDecimal balance; // this field can be updated
    private Currency currency; // this field can be updated
    private AccountType accountType;
    private AccountStatus accountStatus; // this field can be updated
    private LocalDate openDate;
    private Long customerNumber;
}
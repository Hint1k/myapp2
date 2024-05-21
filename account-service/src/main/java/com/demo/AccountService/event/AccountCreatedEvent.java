package com.demo.AccountService.event;

import com.demo.AccountService.util.AccountStatus;
import com.demo.AccountService.util.AccountType;
import com.demo.AccountService.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data // getters, setters, hashcode, equals, toString
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreatedEvent {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private Currency currency;
    private AccountType accountType;
    private AccountStatus status;
    private LocalDate openDate;
    private Long userId;
}
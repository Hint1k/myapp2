package com.bank.accountservice.event;

import com.bank.accountservice.util.AccountStatus;
import com.bank.accountservice.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data // getters, setters, hashcode, equals, toString
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdatedEvent {

    private Long accountId;
    private BigDecimal balance;
    private Currency currency;
    // only these 3 fields are allowed to be updated
    private AccountStatus accountStatus;
}
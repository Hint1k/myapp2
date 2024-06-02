package com.bank.webservice.event;

import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data // getters, setters, hashcode, equals, toString
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdatedEvent {

    private Long accountId;
    // only these 3 fields are allowed to be updated
    private BigDecimal balance;
    private Currency currency;
    private AccountStatus accountStatus;
}
package com.bank.webservice.event.account;

import com.bank.webservice.event.BaseEvent;
import com.bank.webservice.util.AccountStatus;
import com.bank.webservice.util.AccountType;
import com.bank.webservice.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdatedEvent extends BaseEvent  implements AccountEvent {

    private Long accountId;  // this field can't be removed
    private Long accountNumber;
    private BigDecimal balance;
    private Currency currency; // this field can be updated
    private AccountType accountType; // this field can be updated
    private AccountStatus accountStatus; // this field can be updated
    private LocalDate openDate;
    private Long customerNumber;
}
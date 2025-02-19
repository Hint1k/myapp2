package com.bank.webservice.event.account;

import com.bank.webservice.dto.Account;
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
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllAccountsEvent extends BaseEvent {

    private List<Account> accounts;
}
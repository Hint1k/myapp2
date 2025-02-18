package com.bank.webservice.event.account;

import com.bank.webservice.dto.Account;
import com.bank.webservice.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllAccountsEvent extends BaseEvent {
    private List<Account> accounts;
}
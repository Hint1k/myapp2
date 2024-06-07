package com.bank.webservice.event.account;

import com.bank.webservice.dto.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllAccountsEvent {
    List<Account> accounts;
}
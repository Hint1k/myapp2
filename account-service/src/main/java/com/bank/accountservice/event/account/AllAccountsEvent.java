package com.bank.accountservice.event.account;

import com.bank.accountservice.entity.Account;
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
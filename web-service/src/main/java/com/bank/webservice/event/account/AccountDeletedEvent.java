package com.bank.webservice.event.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletedEvent {
    private Long accountId;
    private Long accountNumber;
}
package com.bank.webservice.event.account;

import com.bank.webservice.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletedEvent extends BaseEvent {

    private Long accountId;
    private Long accountNumber;
}
package com.bank.webservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // getters, setters, hashcode, equals, toString
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletedEvent {
    private Long accountId;
}
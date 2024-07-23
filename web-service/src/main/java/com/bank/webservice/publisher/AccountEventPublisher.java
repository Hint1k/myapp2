package com.bank.webservice.publisher;

import com.bank.webservice.dto.Account;

public interface AccountEventPublisher {

    void publishAccountCreatedEvent(Account account);

    void publishAccountUpdatedEvent(Account account);

    void publishAccountDeletedEvent(Long accountId);

    void publishAllAccountsEvent();

    void publishAccountDetailsEvent(Long accountId);
}
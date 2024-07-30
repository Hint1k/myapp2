package com.bank.accountservice.publisher;

import com.bank.accountservice.entity.Account;

import java.util.List;

public interface AccountEventPublisher {

    void publishAccountCreatedEvent(Account account);

    void publishAccountUpdatedEvent(Account account);

    void publishAccountDeletedEvent(Long accountId, Long accountNumber);

    void publishAllAccountsEvent(List<Account> accounts);

    void publishAccountDetailsEvent(Account account);

    void publishSuspendTransactionEvent(Account account);
}
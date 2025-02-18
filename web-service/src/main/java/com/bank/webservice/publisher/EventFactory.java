package com.bank.webservice.publisher;

import com.bank.webservice.event.BaseEvent;
import com.bank.webservice.util.Operation;

public interface EventFactory {

    BaseEvent createEvent(Object entity, Operation operation, Class<?> entityType);
}
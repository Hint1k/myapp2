package com.bank.webservice.publisher;

public interface GenericPublisher {

    <T> void publishCreatedEvent(T event);

    <T> void publishUpdatedEvent(T event);

    <T> void publishDeletedEvent(Long id, Class<T> eventType);

    <T> void publishDetailsEvent(Long id, Class<T> eventType);

    <T> void publishAllEvent(Class<T> eventType);
}
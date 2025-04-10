package com.bank.webservice.publisher;

import com.bank.webservice.event.BaseEvent;
import com.bank.webservice.util.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenericEventPublisherImpl implements GenericEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventFactory eventFactory;

    @Override
    public <T> void publishCreatedEvent(T entity) {
        String topic = entity.getClass().getSimpleName().toLowerCase() + "-creation-requested";
        BaseEvent event = eventFactory.createEvent(entity, Operation.CREATE, entity.getClass());
        kafkaTemplate.send(topic, event);
        log.info("Published CREATED event for {} to topic {}", entity.getClass().getSimpleName(), topic);
    }

    @Override
    public <T> void publishUpdatedEvent(T entity) {
        String topic = entity.getClass().getSimpleName().toLowerCase() + "-update-requested";
        BaseEvent event = eventFactory.createEvent(entity, Operation.UPDATE, entity.getClass());
        kafkaTemplate.send(topic, event);
        log.info("Published UPDATED event for {} to topic {}", entity.getClass().getSimpleName(), topic);
    }

    @Override
    public <T> void publishDeletedEvent(Long id, Class<T> entityType) {
        String topic = entityType.getSimpleName().toLowerCase() + "-deletion-requested";
        BaseEvent event = eventFactory.createEvent(id, Operation.DELETE, entityType);
        kafkaTemplate.send(topic, event);
        log.info("Published DELETED event for {} ID {} to topic {}", entityType.getSimpleName(), id, topic);
    }

    @Override
    public <T> void publishDetailsEvent(Long id, Class<T> entityType) {
        String topic = entityType.getSimpleName().toLowerCase() + "-details-requested";
        BaseEvent event = eventFactory.createEvent(id, Operation.DETAILS, entityType);
        kafkaTemplate.send(topic, event);
        log.info("Published DETAILS event for {} ID {} to topic {}", entityType.getSimpleName(), id, topic);
    }

    @Override
    public <T> void publishAllEvent(Class<T> entityType) {
        String topic = "all-" + entityType.getSimpleName().toLowerCase() + "s-requested";
        BaseEvent event = eventFactory.createEvent(null, Operation.ALL, entityType);
        kafkaTemplate.send(topic, event);
        log.info("Published ALL event for {} to topic {}", entityType.getSimpleName(), topic);
    }
}
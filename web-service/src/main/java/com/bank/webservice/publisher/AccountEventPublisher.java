package com.bank.webservice.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.bank.webservice.dto.AccountDTO;

@Component
@Slf4j
public class AccountEventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void publishEvent(String topic, T message) {
        kafkaTemplate.send(topic, message);
        // add check later if the message was sent
        // add log for the check
    }

    public void publishAccountCreatedEvent(AccountDTO account) {
        kafkaTemplate.send("account-created", account);

    }

    public void publishAccountDetailsRequestedEvent(Long accountId) {
        kafkaTemplate.send("account-details-requested", accountId);
    }
}
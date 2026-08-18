package org.example.socialsvc.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.example.socialsvc.infrastructure.properties.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaProperties properties;

    public <T> void send(String endpointName, KafkaTemplate<String, T> kafkaTemplate, String key, T payload) {
        var endpoint = properties.topic(endpointName);

        kafkaTemplate.send(endpoint.topic(), key, payload);
    }

    public <T> void sendAfterCommit(String endpointName, KafkaTemplate<String, T> kafkaTemplate, String key, T payload) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            send(endpointName, kafkaTemplate, key, payload);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                send(endpointName, kafkaTemplate, key, payload);
            }
        });
    }
}

package org.example.socialsvc.application.subscription.kafka;

import lombok.RequiredArgsConstructor;
import org.example.socialsvc.application.subscription.event.NotificationRequestedEvent;
import org.example.socialsvc.infrastructure.kafka.KafkaService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationEventProducer {

    private static final String ENDPOINT = "notification-requested";

    private final KafkaService kafkaService;
    private final KafkaTemplate<String, NotificationRequestedEvent> kafkaTemplate;

    public void send(NotificationRequestedEvent event) {
        kafkaService.send(
                ENDPOINT,
                kafkaTemplate,
                event.recipientId().toString(),
                event
        );
    }

    public void sendAfterCommit(NotificationRequestedEvent event) {
        kafkaService.sendAfterCommit(
                ENDPOINT,
                kafkaTemplate,
                event.recipientId().toString(),
                event
        );
    }
}

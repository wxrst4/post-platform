package org.example.notificationsvc.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.example.notificationsvc.application.notification.PostNotificationHandler;
import org.example.notificationsvc.application.notification.events.NotificationRequestedEvent;
import org.example.notificationsvc.infrastructure.kafka.KafkaFactory;
import org.example.notificationsvc.infrastructure.properties.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaAdmin kafkaAdmin(KafkaFactory kafkaFactory) {
        var kafkaAdmin = new KafkaAdmin(kafkaFactory.adminProperties());

        kafkaAdmin.setFatalIfBrokerNotAvailable(false);
        return kafkaAdmin;
    }

    @Bean
    public KafkaAdmin.NewTopics kafkaTopics(KafkaProperties properties) {
        var topics = properties.topics().values().stream()
                .map(topic ->
                        TopicBuilder.name(topic.topic())
                                .partitions(topic.partitions() == null ? 1 : topic.partitions())
                                .replicas(1)
                                .build()
                )
                .toArray(NewTopic[]::new);

        return new KafkaAdmin.NewTopics(topics);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, NotificationRequestedEvent> notificationRequestedConsumer(
            KafkaFactory kafkaFactory, PostNotificationHandler handler
    ) {
        return kafkaFactory.consumer(
                "notification-requested",
                NotificationRequestedEvent.class,
                handler::handleNotificationRequested
        );
    }
}

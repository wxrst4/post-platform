package org.example.socialsvc.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.example.socialsvc.application.subscription.event.NotificationRequestedEvent;
import org.example.socialsvc.application.subscription.event.PostPublishedEvent;
import org.example.socialsvc.application.subscription.kafka.PostPublishedConsumerHandler;
import org.example.socialsvc.infrastructure.kafka.KafkaFactory;
import org.example.socialsvc.infrastructure.properties.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
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
    public ConcurrentMessageListenerContainer<String, PostPublishedEvent> postPublishedConsumer(
            KafkaFactory kafkaFactory,
            PostPublishedConsumerHandler consumer
    ) {
        return kafkaFactory.consumer(
                "post-published",
                PostPublishedEvent.class,
                consumer::consume
        );
    }

    @Bean
    public KafkaTemplate<String, NotificationRequestedEvent> notificationKafkaTemplate(
            KafkaFactory kafkaFactory
    ) {
        return kafkaFactory.template(
                "notification-requested"
        );
    }
}

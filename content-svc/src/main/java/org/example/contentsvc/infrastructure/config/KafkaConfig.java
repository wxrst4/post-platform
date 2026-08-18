package org.example.contentsvc.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.example.contentsvc.application.post.event.PostPublishedEvent;
import org.example.contentsvc.infrastructure.kafka.KafkaFactory;
import org.example.contentsvc.infrastructure.properties.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;

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
                                .partitions(topic.partition() == null ? 1 : topic.partition())
                                .replicas(1)
                                .build()
                )
                .toArray(NewTopic[]::new);

        return new KafkaAdmin.NewTopics(topics);
    }

    @Bean
    public KafkaTemplate<String, PostPublishedEvent> postPublishedKafkaTemplate(KafkaFactory kafkaFactory) {
        return kafkaFactory.template("post-published");
    }
}

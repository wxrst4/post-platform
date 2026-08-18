package org.example.notificationsvc.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(
        String bootstrapServers,
        Boolean consumerAutoStartup,
        Map<String, TopicProperties> topics
) {

    public record TopicProperties(
            String topic,
            Integer partitions,
            String groupId
    ) {
    }

    public boolean isConsumerAutoStartup() {
        return !Boolean.FALSE.equals(consumerAutoStartup);
    }

    public TopicProperties topic(String topicName) {
        var topic = topics == null ? null : topics.get(topicName);

        if (topic == null) throw new IllegalArgumentException("Kafka topic is not configured: " + topicName);

        return topic;
    }
}

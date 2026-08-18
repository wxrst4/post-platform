package org.example.contentsvc.infrastructure.properties;

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
            Integer partition
    ) {
    }

    public boolean isConsumerAutoStartup() {
        return !Boolean.FALSE.equals(consumerAutoStartup);
    }

    public TopicProperties topic(String name) {
        TopicProperties topic = topics == null ? null : topics.get(name);

        if (topic == null) {
            throw new IllegalArgumentException("Kafka topic is not configured: " + name);
        }
        return topic;
    }
}

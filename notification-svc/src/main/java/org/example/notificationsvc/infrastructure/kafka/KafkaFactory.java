package org.example.notificationsvc.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.notificationsvc.infrastructure.properties.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KafkaFactory {

    private final KafkaProperties properties;
    private final ObjectMapper objectMapper;

    public Map<String, Object> adminProperties() {
        return Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers()
        );
    }

    public <T> ConcurrentMessageListenerContainer<String, T> consumer(
            String name,
            Class<T> payloadType,
            MessageListener<String, T> listener
    ) {
        var endpoint = properties.topic(name);
        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProperties(name, endpoint),
                new StringDeserializer(),
                deserializer(payloadType)
        );

        var containerProperties = new ContainerProperties(endpoint.topic());

        containerProperties.setMessageListener(listener);
        if (endpoint.groupId() != null && !endpoint.groupId().isBlank()) {
            containerProperties.setGroupId(endpoint.groupId());
        }
        containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);

        var container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setBeanName(name + "-consumer");
        container.setAutoStartup(properties.isConsumerAutoStartup());
        return container;
    }

    private Map<String, Object> consumerProperties(String name, KafkaProperties.TopicProperties topicProperties) {
        var props = new HashMap<String, Object>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, name + "-consumer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        if (StringUtils.hasText(topicProperties.groupId())) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, topicProperties.groupId());
        }

        return props;
    }

    private <T> Deserializer<T> deserializer(Class<T> payloadType) {
        return new JacksonKafkaDeserializer<>(objectMapper, payloadType);
    }
}

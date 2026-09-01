package org.example.contentsvc.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.contentsvc.infrastructure.properties.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KafkaFactory {
    private final KafkaProperties properties;
    private final ObjectMapper objectMapper;

    public Map<String, Object> adminProperties() {
        return Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers());
    }

    public <T> KafkaTemplate<String, T> template(String name) {
        var producerFactory = new DefaultKafkaProducerFactory<String, T>(
                producerProperties(name),
                new StringSerializer(),
                serializer()
        );

        return new KafkaTemplate<>(producerFactory);
    }

    private Map<String, Object> producerProperties(String name) {
        var props = new HashMap<String, Object>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, name + "-producer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        return props;
    }

    private <T> Serializer<T> serializer() {
        return new JacksonKafkaSerializer<>(objectMapper);
    }
}
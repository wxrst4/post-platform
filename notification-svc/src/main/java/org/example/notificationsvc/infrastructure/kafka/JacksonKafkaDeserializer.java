package org.example.notificationsvc.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RequiredArgsConstructor
public class JacksonKafkaDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper mapper;
    private final Class<T> payloadType;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;

        try {
            return mapper.readValue(data, payloadType);
        } catch (Exception e) {
            throw new SerializationException("Can't deserialize JSON kafka payload for topic: " + topic, e);
        }
    }
}

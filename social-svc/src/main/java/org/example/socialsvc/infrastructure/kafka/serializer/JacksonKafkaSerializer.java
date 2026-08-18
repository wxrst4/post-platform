package org.example.socialsvc.infrastructure.kafka.serializer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RequiredArgsConstructor
public class JacksonKafkaSerializer<T> implements Serializer<T> {

    private final ObjectMapper mapper;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try {
            return mapper.writeValueAsBytes(data);
        } catch (Exception exception) {
            throw new SerializationException("Cannot serialize json kafka payload for topic " + topic, exception);
        }
    }
}

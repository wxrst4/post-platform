package org.example.notificationsvc.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer nonNullJsonMapperCustomizer() {
        return jsonMapperBuilder -> jsonMapperBuilder.changeDefaultPropertyInclusion(inclustion ->
                inclustion.withValueInclusion(JsonInclude.Include.NON_NULL));
    }
}

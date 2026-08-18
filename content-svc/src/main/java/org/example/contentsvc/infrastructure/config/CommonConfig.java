package org.example.contentsvc.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jsonwebtoken.security.Keys;
import org.example.contentsvc.infrastructure.properties.JwtProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class CommonConfig {

    @Bean
    public SecretKey secretKey(JwtProperties jwtProperties) {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public JsonMapperBuilderCustomizer nonNullJsonMapperCustomizer() {
        return builder -> builder.changeDefaultPropertyInclusion(inclusion ->
                inclusion.withValueInclusion(JsonInclude.Include.NON_NULL));
    }
}

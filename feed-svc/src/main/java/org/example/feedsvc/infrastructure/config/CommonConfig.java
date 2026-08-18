package org.example.feedsvc.infrastructure.config;

import io.jsonwebtoken.security.Keys;
import org.example.feedsvc.infrastructure.properties.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class CommonConfig {

    @Bean
    public SecretKey secretKey(JwtProperties properties) {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
}

package org.example.socialsvc.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jsonwebtoken.security.Keys;
import org.example.contentsvc.grpc.ContentServiceGrpc;
import org.example.socialsvc.infrastructure.properties.JwtProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
@ImportGrpcClients(target = "content", types = ContentServiceGrpc.ContentServiceBlockingStub.class)
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

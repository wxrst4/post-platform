package org.example.usersvc.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        Long expirationAccess,
        Long expirationRefresh
) {
}

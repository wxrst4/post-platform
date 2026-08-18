package org.example.contentsvc.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.s3")
public record S3Properties(
        String accessKey,
        String secretKey,
        String bucket,
        String regions,
        String endpoint,
        String publicEndpoint,
        Boolean pathStyleAccess,
        Duration downloadUrlTtl
) {
}

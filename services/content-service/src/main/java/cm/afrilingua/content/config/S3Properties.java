package cm.afrilingua.content.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.storage.s3")
public record S3Properties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucket,
        String region
) {}

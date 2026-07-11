package cm.afrilingua.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Mirrors auth-service's JwtProperties -- must share the same secret-key so
 * tokens signed by auth-service can be verified here. */
@ConfigurationProperties(prefix = "application.security.jwt")
public record JwtProperties(String secretKey) {}

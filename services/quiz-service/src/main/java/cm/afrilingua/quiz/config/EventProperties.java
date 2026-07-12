package cm.afrilingua.quiz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.events")
public record EventProperties(String exchange) {}

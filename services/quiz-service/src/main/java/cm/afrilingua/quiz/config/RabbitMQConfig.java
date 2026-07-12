package cm.afrilingua.quiz.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Declares the same topic exchange recommendation-service's consumer binds
 * to (afrilingua.events) -- this service is a producer only, it never
 * consumes anything itself. */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange eventsExchange(EventProperties eventProperties) {
        return new TopicExchange(eventProperties.exchange(), true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

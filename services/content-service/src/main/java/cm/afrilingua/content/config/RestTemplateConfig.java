package cm.afrilingua.content.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // Resolves service names (e.g. "RECOMMENDATION-SERVICE") via Eureka.
    // Only for calls to other services registered in this project's discovery server.
    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }

    // Plain RestTemplate for real external domains (e.g. apis.ntealan.net),
    // which must never go through Eureka's service resolution.
    @Bean
    public RestTemplate externalRestTemplate() {
        return new RestTemplate();
    }
}

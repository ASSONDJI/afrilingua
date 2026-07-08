package cm.afrilingua.auth;

import cm.afrilingua.auth.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Locale;

@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) {
        // Force English locale so validation messages and any locale-dependent
        // output stay consistent regardless of the host machine's system locale.
        Locale.setDefault(Locale.ENGLISH);
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}// CI trigger test

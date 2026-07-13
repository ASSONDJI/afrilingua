package cm.afrilingua.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Locale;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ContentServiceApplication {
    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        SpringApplication.run(ContentServiceApplication.class, args);
    }
}

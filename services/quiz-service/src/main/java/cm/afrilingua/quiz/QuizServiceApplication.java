package cm.afrilingua.quiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Locale;

@SpringBootApplication
@ConfigurationPropertiesScan
public class QuizServiceApplication {
    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        SpringApplication.run(QuizServiceApplication.class, args);
    }
}

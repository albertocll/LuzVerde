package es.albertocll.luzverde;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LuzVerdeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuzVerdeApplication.class, args);
    }
}

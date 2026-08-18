package org.example.contentsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@EnableKafka
@EnableWebSecurity
@SpringBootApplication
@ConfigurationPropertiesScan
public class ContentSvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentSvcApplication.class, args);
    }

}

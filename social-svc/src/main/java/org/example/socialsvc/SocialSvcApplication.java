package org.example.socialsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableKafka
@EnableWebSecurity
@SpringBootApplication
@ConfigurationPropertiesScan
public class SocialSvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialSvcApplication.class, args);
    }

}

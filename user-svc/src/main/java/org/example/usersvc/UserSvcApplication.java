package org.example.usersvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@SpringBootApplication
@ConfigurationPropertiesScan
public class UserSvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserSvcApplication.class, args);
    }

}

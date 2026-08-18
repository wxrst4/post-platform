package org.example.feedsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class FeedSvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedSvcApplication.class, args);
    }

}

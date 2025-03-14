package ru.vesolyydrug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FollowMeApp {

    public static void main(String[] args) {
        SpringApplication.run(FollowMeApp.class, args);
    }

    @Bean
    public Database database() {
        return new Database();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}

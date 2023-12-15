package com.app.find;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FindApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindApplication.class, args);
    }

}

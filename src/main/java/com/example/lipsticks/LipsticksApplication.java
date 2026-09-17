package com.example.lipsticks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LipsticksApplication {

    public static void main(String[] args) {
        SpringApplication.run(LipsticksApplication.class, args);
    }

}

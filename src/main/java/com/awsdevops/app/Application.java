package com.awsdevops.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class AppController {

    @GetMapping("/")
    public String home() {
        return "AWS DevOps Platform - Application Running!";
    }

    @GetMapping("/health")
    public String health() {
        return "UP";
    }
}

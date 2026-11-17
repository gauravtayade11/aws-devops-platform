package com.awsdevops.app;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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

    private final ObjectProvider<JdbcTemplate> jdbc;

    AppController(ObjectProvider<JdbcTemplate> jdbc) {
        this.jdbc = jdbc;
    }

    /** Verifies the app can reach RDS using the credentials loaded from Secrets Manager. */
    @GetMapping("/db-check")
    public ResponseEntity<Map<String, String>> dbCheck() {
        JdbcTemplate template = jdbc.getIfAvailable();
        if (template == null) {
            return ResponseEntity.status(503).body(Map.of("status", "DOWN", "error", "no datasource configured"));
        }
        try {
            String version = template.queryForObject("SELECT version()", String.class);
            String db = template.queryForObject("SELECT current_database()", String.class);
            return ResponseEntity.ok(Map.of("status", "UP", "database", db, "version", version));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of("status", "DOWN", "error", e.getClass().getSimpleName()));
        }
    }

    @GetMapping("/")
    public String home() {
        return "AWS DevOps Platform - Application Running!";
    }

    @GetMapping("/health")
    public String health() {
        return "UP";
    }
}

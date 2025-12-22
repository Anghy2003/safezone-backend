package com.ista.springboot.web.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync   // ✅ HABILITA @Async en todo el proyecto
@SpringBootApplication
public class SafeZoneApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafeZoneApplication.class, args);
    }
}

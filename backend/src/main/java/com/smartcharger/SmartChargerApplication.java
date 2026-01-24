package com.smartcharger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartChargerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartChargerApplication.class, args);
    }
}

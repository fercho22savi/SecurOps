package com.securops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecurOpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurOpsApplication.class, args);
    }
}

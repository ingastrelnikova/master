package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.client")
@EnableScheduling
public class NewPatientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewPatientServiceApplication.class, args);
    }
}

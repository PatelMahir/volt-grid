package com.voltgrid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VoltGridApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoltGridApplication.class, args);
    }
}

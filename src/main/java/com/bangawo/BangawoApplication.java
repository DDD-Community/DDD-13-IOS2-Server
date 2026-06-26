package com.bangawo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BangawoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BangawoApplication.class, args);
    }
}

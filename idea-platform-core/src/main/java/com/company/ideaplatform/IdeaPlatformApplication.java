package com.company.ideaplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IdeaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdeaPlatformApplication.class, args);
    }
}

package com.vibemyself;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VibeMyselfApplication {

    public static void main(String[] args) {
        SpringApplication.run(VibeMyselfApplication.class, args);
    }
}

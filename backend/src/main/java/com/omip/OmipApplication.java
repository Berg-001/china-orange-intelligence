package com.omip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OmipApplication {
    public static void main(String[] args) { SpringApplication.run(OmipApplication.class, args); }
}

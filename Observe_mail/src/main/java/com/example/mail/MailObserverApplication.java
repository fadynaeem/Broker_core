package com.example.mail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example.mail",
        "com.example.shared"
})
public class MailObserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailObserverApplication.class, args);
    }
}

package com.example.notification.mail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example.notification.mail",
        "com.example.notification.shared"
})
public class MailObserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailObserverApplication.class, args);
    }
}

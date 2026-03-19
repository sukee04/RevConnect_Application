package com.project.revconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class
RevConnectApplication {


    public static void main(String[] args) {
        SpringApplication.run(RevConnectApplication.class, args);
    }

}

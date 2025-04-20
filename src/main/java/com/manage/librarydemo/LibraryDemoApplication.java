package com.manage.librarydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibraryDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryDemoApplication.class, args);
    }

}

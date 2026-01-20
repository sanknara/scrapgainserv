package com.scrap.gain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ScrapgainservApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScrapgainservApplication.class, args);
    }

}

@RestController
class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello World!";
    }

    @GetMapping("/greet")
    public String greet() {
        return "Welcome to Spring Boot!";
    }
}

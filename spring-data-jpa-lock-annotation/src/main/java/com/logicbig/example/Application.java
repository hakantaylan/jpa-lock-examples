package com.logicbig.example;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ApplicationRunner runner(ExampleClient exampleClient) {
        return args -> {
            ExecutorService es = exampleClient.run();
            es.shutdown();
            es.awaitTermination(5, TimeUnit.MINUTES);
        };
    }
}

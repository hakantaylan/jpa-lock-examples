package com.example.lox;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;

import java.util.concurrent.locks.Lock;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//    @Bean
//    ApplicationRunner runner(JdbcLockRegistry lockRegistry){
//        return new ApplicationRunner() {
//            @Override
//            public void run(ApplicationArguments args) throws Exception {
//                Lock lock = lockRegistry.obtain("lock1");
//                if(lock.tryLock()) {
//
//                }
//            }
//        };
//    }
}

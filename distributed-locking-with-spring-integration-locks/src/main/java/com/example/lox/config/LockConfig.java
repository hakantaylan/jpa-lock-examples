package com.example.lox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;

import javax.sql.DataSource;

@Configuration
public class LockConfig {

    @Bean
    @Scope("prototype")
    DefaultLockRepository defaultLockRepository(DataSource dataSource) {
        DefaultLockRepository defaultLockRepository = new DefaultLockRepository(dataSource);
        defaultLockRepository.setTimeToLive(5_000);
        defaultLockRepository.afterPropertiesSet();
        return defaultLockRepository;
    }

    @Bean
    @Scope("prototype")
    JdbcLockRegistry jdbcLockRegistry(LockRepository repository) {
        return new JdbcLockRegistry(repository);
    }
}

package com.example.nplusone;

import com.example.nplusone.model.Book;
import com.example.nplusone.repository.BookRepository;
import com.example.nplusone.service.BookService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class SpringBootJpaH2Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootJpaH2Application.class, args);
    }

    @Bean
    CommandLineRunner runner(BookRepository repository, BookService service) {
        return args -> {

            Book book = new Book();
            book.setIsbn("123");
            repository.save(book);

            ExecutorService es = Executors.newFixedThreadPool(2);

            //user 1, reader
            es.execute(service::runUser1Transaction);

            //user 2, writer
            es.execute(service::runUser1Transaction);

        };
    }

}

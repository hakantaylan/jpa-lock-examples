package com.logicbig.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ExampleClient {
    @Autowired
    private ArticleRepository repo;
    @Autowired
    private Tasks tasks;

    public ExecutorService run() {
        //creating and persisting an Article
        Article article = new Article("test article");
        repo.save(article);

        ExecutorService es = Executors.newFixedThreadPool(2);

        //user 1, reader
        es.execute(tasks::runUser1Transaction);

        //user 2, writer
        es.execute(tasks::runUser2Transaction);

        return es;
    }

}
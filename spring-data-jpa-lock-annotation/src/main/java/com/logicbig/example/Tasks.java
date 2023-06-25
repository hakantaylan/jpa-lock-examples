package com.logicbig.example;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class Tasks {

    private final ArticleRepository repo;

    public Tasks(ArticleRepository repo) {
        this.repo = repo;
    }

    public void runUser1Transaction() {
        System.out.println(" -- user 1 reading Article entity --");
        long start = System.currentTimeMillis();
        Article article1 = null;
        try {
            article1 = repo.findArticleForRead(1L);
        } catch (Exception e) {
            System.err.println("User 1 got exception while acquiring the database lock:\n " + e);
            return;
        }
        System.out.println("user 1 got the lock, block time was: " + (System.currentTimeMillis() - start));
        //delay for 2 secs
        ThreadSleep(3000);
        System.out.println("User 1 read article: " + article1);
    }

    public void runUser2Transaction() {
        ThreadSleep(500);//let user1 acquire optimistic lock first
        System.out.println(" -- user 2 writing Article entity --");
        long start = System.currentTimeMillis();
        Article article2 = null;
        try {
            article2 = repo.findArticleForWrite(1L);
        } catch (Exception e) {
            System.err.println("User 2 got exception while acquiring the database lock:\n " + e);
            return;
        }
        System.out.println("user 2 got the lock, block time was: " + (System.currentTimeMillis() - start));
        article2.setContent("updated content by user 2.");
        repo.save(article2);
        System.out.println("User 2 updated article: " + article2);
    }

    private void ThreadSleep(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}

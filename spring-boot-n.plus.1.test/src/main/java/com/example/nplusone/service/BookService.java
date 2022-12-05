package com.example.nplusone.service;

import com.example.nplusone.model.Book;
import com.example.nplusone.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public void runUser1Transaction() {
        System.out.println(" -- user 1 reading Article entity --");
        long start = System.currentTimeMillis();
        Book book = null;
        try {
            book = bookRepository.findBookForRead(1L);
        } catch (Exception e) {
            System.err.println("User 1 got exception while acquiring the database lock:\n " + e);
            return;
        }
        System.out.println("user 1 got the lock, block time was: " + (System.currentTimeMillis() - start));
        //delay for 2 secs
        ThreadSleep(3000);
        System.out.println("User 1 read article: " + book);
    }

    public void runUser2Transaction() {
        ThreadSleep(500);//let user1 acquire optimistic lock first
        System.out.println(" -- user 2 writing Article entity --");
        long start = System.currentTimeMillis();
        Book book = null;
        try {
            book = bookRepository.findBookForWrite(1L);
        } catch (Exception e) {
            System.err.println("User 2 got exception while acquiring the database lock:\n " + e);
            return;
        }
        System.out.println("user 2 got the lock, block time was: " + (System.currentTimeMillis() - start));
        book.setIsbn("updated content by user 2.");
        ThreadSleep(3000);
        bookRepository.save(book);
        System.out.println("User 2 updated article: " + book);
    }

    private void ThreadSleep(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}

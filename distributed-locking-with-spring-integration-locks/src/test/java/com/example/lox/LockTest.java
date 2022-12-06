package com.example.lox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@SpringBootTest
@Import({TestConfig.class})
public class LockTest {

    @Autowired
    RetryTemplate retryTemplate;
    @Autowired
    ApplicationContext appContext;

    @Test
    public void testLockWithRetry() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable c1 = () -> {
            try {
                LockRegistry lockRegistry = appContext.getBean(LockRegistry.class);
                retryTemplate.execute(new RetryCallback<Void, Throwable>() {
                    @Override
                    public Void doWithRetry(RetryContext retryContext) throws Throwable {
                        Lock myLock = lockRegistry.obtain("myLock1");
                        try {
                            if (myLock.tryLock()) {
                                System.out.println(Thread.currentThread().getName() + " lock'ı aldı.");
                                Thread.sleep(2_000);
                            } else {
                                System.out.println("Yılmadım gene deniyorum. Deneme sayısı: " + retryContext.getRetryCount());
                                throw new RuntimeException("The number you have called can not be reached at the moment. Please try again later");
                            }
                        } finally {
                            myLock.unlock();
                        }
                        return null;
                    }
                });
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return null;
        };

        executorService.submit(c1);
        executorService.submit(c1);

        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    public void testTTLExpires() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable c1 = () -> {
            try {
                LockRegistry lockRegistry = appContext.getBean(LockRegistry.class);
                retryTemplate.execute(new RetryCallback<Void, Throwable>() {
                    @Override
                    public Void doWithRetry(RetryContext retryContext) throws Throwable {
                        Lock myLock = lockRegistry.obtain("myLock2");
                        try {
                            if (myLock.tryLock()) {
                                System.out.println(Thread.currentThread().getName() + " lock'ı aldı.");
                                Thread.sleep(20_000);
                            } else {
                                System.out.println("Yılmadım gene deniyorum. Deneme sayısı: " + retryContext.getRetryCount());
                                throw new RuntimeException("The number you have called can not be reached at the moment. Please try again later");
                            }
                        } finally {
                            myLock.unlock();
                        }
                        return null;
                    }
                });
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return null;
        };

        executorService.submit(c1);
        executorService.submit(c1);

        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }
}

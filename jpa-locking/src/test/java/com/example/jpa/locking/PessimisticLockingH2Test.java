package com.example.jpa.locking;

import com.example.jpa.locking.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.LockModeType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PessimisticLockingH2Test {
    @Autowired
    private TransactionalRunner txRunner;

    @Autowired
    private TestHelper helper;

    @AfterEach
    public void tearDown() {
        helper.reset();
    }

    @Test
    public void testExclusiveLockCanBeAcquired() {
        // given
        Product p = new Product("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        // when
        Product result = txRunner.doInTransaction(em -> {
            return em.find(Product.class, p.getId(), LockModeType.PESSIMISTIC_WRITE);
        });
        // then
        assertThat(result).isNotNull();
    }

    @Test
    public void testSharedLockCantBeAcquired() {
        // given
        Product p = new Product("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        // when
        Product result = txRunner.doInTransaction(em -> {
            return em.find(Product.class, p.getId(), LockModeType.PESSIMISTIC_READ);
        });
        // then
        assertThat(result).isNotNull();
    }
}

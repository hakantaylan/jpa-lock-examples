package com.example.jpa.locking;

import com.example.jpa.locking.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AtomicUpdateTest {
    @Autowired
    private TransactionalRunner txRunner;

    @Autowired
    private TestHelper helper;

    @AfterEach
    public void tearDown() {
        helper.reset();
    }

    @Test
    public void proveLostUpdate() {
        // given
        Product p = new Product("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        // when
        txRunner.doInTransaction(em1 -> {
            Product p1 = em1.find(Product.class, p.getId());
            txRunner.doInTransaction(em2 -> {
                Product p2 = em2.find(Product.class, p.getId());
                p2.setStock(p2.getStock() - 1);
            });
            p1.setStock(p1.getStock() - 1);
        });
        // then
        txRunner.doInTransaction(em -> {
            Product product = em.find(Product.class, p.getId());
            assertThat(product.getStock()).isEqualTo(4);
        });
    }

    @Test
    public void testAtomicUpdateWorks() {
        // given
        Product p = new Product("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        // when
        txRunner.doInTransaction(em1 -> {
            Query query = em1.createQuery("update Product p set p.stock = p.stock - 1");
            txRunner.doInTransaction(em2 -> {
                Query query2 = em1.createQuery("update Product p set p.stock = p.stock - 1");
                query2.executeUpdate();
            });
            query.executeUpdate();
        });
        // then
        txRunner.doInTransaction(em -> {
            Product product = em.find(Product.class, p.getId());
            assertThat(product.getStock()).isEqualTo(3);
        });
    }
}

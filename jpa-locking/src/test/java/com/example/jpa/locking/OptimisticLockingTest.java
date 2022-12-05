package com.example.jpa.locking;

import com.example.jpa.locking.model.VersionedProduct;
import com.example.jpa.locking.model.VersionlessProduct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OptimisticLockingTest {
    @Autowired
    private TransactionalRunner txRunner;

    @Autowired
    private TestHelper helper;

    @AfterEach
    public void tearDown() {
        helper.reset();
    }

    @Test
    public void testVersionedOptimisticLocking() {
        // given
        VersionedProduct p = new VersionedProduct("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            // when
            txRunner.doInTransaction(em1 -> {
                VersionedProduct p1 = em1.find(VersionedProduct.class, p.getId());
                txRunner.doInTransaction(em2 -> {
                    VersionedProduct p2 = em2.find(VersionedProduct.class, p.getId());
                    p2.setStock(p2.getStock() - 1);
                });
                p1.setStock(p1.getStock() - 1);
            });
        });

        // then exception thrown
    }

    @Test
    public void testVersionedOptimisticLockingWithoutOverlappingChanges() {
        // given
        VersionedProduct p = new VersionedProduct("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            // when
            txRunner.doInTransaction(em1 -> {
                VersionedProduct p1 = em1.find(VersionedProduct.class, p.getId());
                txRunner.doInTransaction(em2 -> {
                    VersionedProduct p2 = em2.find(VersionedProduct.class, p.getId());
                    p2.setName("Fancy Notebook");
                });
                p1.setStock(p1.getStock() - 1);
            });
        });
        // then exception thrown
    }

    @Test
    public void testVersionlessOptimisticLocking() {
        // given
        VersionlessProduct p = new VersionlessProduct("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            // when
            txRunner.doInTransaction(em1 -> {
                VersionlessProduct p1 = em1.find(VersionlessProduct.class, p.getId());
                txRunner.doInTransaction(em2 -> {
                    VersionlessProduct p2 = em2.find(VersionlessProduct.class, p.getId());
                    p2.setStock(p2.getStock() - 1);
                });
                p1.setStock(p1.getStock() - 1);
            });
        });
        // then exception thrown
    }

    @Test
    public void testVersionlessOptimisticLockingWithoutOverlappingChanges() {
        // given
        VersionlessProduct p = new VersionlessProduct("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        // when
        txRunner.doInTransaction(em1 -> {
            VersionlessProduct p1 = em1.find(VersionlessProduct.class, p.getId());
            txRunner.doInTransaction(em2 -> {
                VersionlessProduct p2 = em2.find(VersionlessProduct.class, p.getId());
                p2.setName("Fancy Notebook");
            });
            p1.setStock(p1.getStock() - 1);
        });
        // then
        txRunner.doInTransaction(em -> {
            VersionlessProduct product = em.find(VersionlessProduct.class, p.getId());
            assertThat(product.getName()).isEqualTo("Fancy Notebook");
            assertThat(product.getStock()).isEqualTo(4);
        });
    }
}

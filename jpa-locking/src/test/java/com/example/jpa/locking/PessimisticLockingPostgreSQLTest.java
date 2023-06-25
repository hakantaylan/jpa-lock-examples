package com.example.jpa.locking;

import com.example.jpa.locking.model.Product;
import com.google.common.collect.ImmutableMap;
//import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@AutoConfigureEmbeddedDatabase
@Testcontainers
public class PessimisticLockingPostgreSQLTest {
    @Autowired
    private TransactionalRunner txRunner;

    @Autowired
    private TestHelper helper;

    @Container
    private static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:15.3-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password");

    static {
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driverClassName", ()-> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @AfterEach
    public void tearDown() {
        helper.resetPostgres();
    }

    @AfterAll
    public static void closeUp(){
        postgreSQLContainer.stop();
    }

    @Test
    public void testSharedLockCanBeAcquired() {
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

    @Test
    public void testSharedLockCanBeAcquiredByMultipleReaders() {
        // given
        Product p = new Product("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        // when
        Product result = txRunner.doInTransaction(em1 -> {
            Product product1 = em1.find(Product.class, p.getId(), LockModeType.PESSIMISTIC_READ);
            txRunner.doInTransaction(em2 -> {
                Product product2 = em2.find(Product.class, p.getId(), LockModeType.PESSIMISTIC_READ);
                txRunner.doInTransaction(em3 -> {
                    return em3.find(Product.class, p.getId(), LockModeType.PESSIMISTIC_READ);
                });
                return product2;
            });
            return product1;

        });
        // then
        assertThat(result).isNotNull();
    }

    @Test
    public void testExclusiveLockCantBeAcquiredWhenSharedLockAlreadyPresent() {
        // given
        Product p = new Product("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });

        assertThrows(LockTimeoutException.class, () -> {
            // when
            Product result = txRunner.doInTransaction(em1 -> {
                Product product = em1.find(Product.class, p.getId(), LockModeType.PESSIMISTIC_READ);
                txRunner.doInTransaction(em2 -> {
                    return em2.find(Product.class, p.getId(), LockModeType.PESSIMISTIC_WRITE, ImmutableMap.of("javax.persistence.lock.timeout", 0));
                });
                return product;

            });
        });
        // then exception thrown
    }

    @Test
    public void testSharedLockCanBeAcquiredAfterFetching() {
        // given
        Product p = new Product("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        // when
        Product result = txRunner.doInTransaction(em -> {
            Product product = em.find(Product.class, p.getId());
            em.lock(product, LockModeType.PESSIMISTIC_READ);
            return product;
        });
        // then
        assertThat(result).isNotNull();
    }

    @Test
    public void testSharedLockCanBeAcquiredForQuery() {
        // given
        Product p = new Product("Notebook", 5);
        txRunner.doInTransaction(em -> {
            em.persist(p);
        });
        // when
        List<Product> result = txRunner.doInTransaction(em -> {
            return em.createQuery("FROM Product", Product.class).setLockMode(LockModeType.PESSIMISTIC_READ).getResultList();
        });
        // then
        assertThat(result).isNotEmpty();
    }
}

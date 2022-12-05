package com.example.jpa.locking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import javax.persistence.*;
import java.math.BigDecimal;

import static org.junit.Assert.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * LockModeOptimisticTest - Test to check LockMode.OPTIMISTIC
 *
 * @author Vlad Mihalcea
 */
@SpringBootTest(properties = {"logging.level.root=INFO"})
public class LockModeOptimisticTest extends AbstractTest {

    @Autowired
    TransactionalRunner txRunner;

    @BeforeEach
    public void init() {
        txRunner.doInTransaction(entityManager -> {
            Product p = new Product();
            p.setId(1L);
            p.setPrice(BigDecimal.TEN);
            p.setDescription("Product Description");
            entityManager.persist(p);
            OrderLine ol = new OrderLine(p);
            entityManager.persist(ol);
        });
    }

    @AfterEach
    public void afterEach() {
        txRunner.doInTransaction(entityManager -> {
            Query q = entityManager.createQuery("delete from OrderLine");
            q.executeUpdate();
            q = entityManager.createQuery("delete from testproduct");
            q.executeUpdate();
        });
    }

    @Test
    public void testImplicitOptimisticLocking() {
        assertDoesNotThrow(() -> {
            txRunner.doInTransaction(em -> {
                final Product product = em.find(Product.class, 1L);
                executeSync(() -> {
                    txRunner.doInTransaction(em2 -> {
                        Product _product = em2.find(Product.class, 1L);
                        assertNotSame(product, _product);
                        _product.setPrice(BigDecimal.valueOf(14.49));
                    });
                    return null;
                });
                OrderLine orderLine = new OrderLine(product);
                em.persist(orderLine);
            });
        });
    }

    @Test
    public void testExplicitOptimisticLocking() {
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            txRunner.doInTransaction(em -> {
                final Product product = em.find(Product.class, 1L, LockModeType.OPTIMISTIC);

                executeSync(() -> {
                    txRunner.doInTransaction(em2 -> {
                        Product _product = em2.find(Product.class, 1L);
                        assertNotSame(product, _product);
                        _product.setPrice(BigDecimal.valueOf(14.49));
                    });
                    return null;
                });

                OrderLine orderLine = new OrderLine(product);
                em.persist(orderLine);
            });
        });
    }

    @Entity(name = "testproduct")
    @Table(name = "testproduct")
    public static class Product {

        @Id
        private Long id;

        private String description;

        private BigDecimal price;

        @Version
        private int version;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }

    /**
     * OrderLine - Order Line
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "OrderLine")
    @Table(name = "order_line")
    public static class OrderLine {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @ManyToOne
        private Product product;

        private BigDecimal unitPrice;

        @Version
        private int version;

        public OrderLine() {

        }

        public OrderLine(Product product) {
            this.product = product;
            this.unitPrice = product.getPrice();
        }

        public Long getId() {
            return id;
        }

        public Product getProduct() {
            return product;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }
    }
}

package com.example.jpa.locking;

import com.example.jpa.locking.model.Product;
import com.example.jpa.locking.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LostUpdateTest2 {
    @Autowired
    private TransactionalRunner txRunner;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestHelper helper;

    @AfterEach
    public void tearDown() {
        helper.reset();
    }

    @Test
    public void testLostUpdateOccurs() {
        // given
        Product p = new Product("Notebook", 5);
        txRunner.doInTransaction(() -> {
            productRepository.save(p);
        });

        // when
        txRunner.doInTransaction(() -> {
//            Product p1 = em1.find(Product.class, p.getId());
            Product p1 = productRepository.findById(p.getId()).get();

            txRunner.doInTransaction(() -> {
                Product p2 =  productRepository.findById(p.getId()).get();
                p2.setStock(p2.getStock() - 1);
            });
            p1.setStock(p1.getStock() - 1);
        });
        // then
        txRunner.doInTransaction(() -> {
            Product product =  productRepository.findById(p.getId()).get();
            assertThat(product.getStock()).isEqualTo(4);
        });
    }
}

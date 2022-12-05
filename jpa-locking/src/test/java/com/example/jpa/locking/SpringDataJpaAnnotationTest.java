//package com.arnoldgalovics.blog.jpalocking;
//
//import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import javax.sql.DataSource;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@AutoConfigureEmbeddedDatabase
//public class SpringDataJpaAnnotationTest {
//
//    @Autowired
//    private TransactionalRunner txRunner;
//
//    @Autowired
//    private DataSource dataSource;
//
//
//    @Test
//    public void testEmbeddedDatabase() {
////		Product p = new Product("Notebook", 5);
////		txRunner.doInTransaction(em -> {
////			em.persist(p);
////		});
//        assertThat(dataSource).isNotNull();
//    }
//}
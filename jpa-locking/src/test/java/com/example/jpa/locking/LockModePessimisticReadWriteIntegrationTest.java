package com.example.jpa.locking;

import com.example.jpa.locking.model.MyEntity;
import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


/**
 * LockModePessimisticReadWriteIntegrationTest - Test to check LockMode.PESSIMISTIC_READ and LockMode.PESSIMISTIC_WRITE
 *
 * @author Vlad Mihalcea
 */
@SpringBootTest(properties = {"logging.level.root=INFO"})
public class LockModePessimisticReadWriteIntegrationTest extends AbstractTest {
    public static final int WAIT_MILLIS = 500;

    private interface LockRequestInterface {
        void lock(EntityManager entityManager, MyEntity myEntity);
    }

    private final CountDownLatch endLatch = new CountDownLatch(1);

    @Autowired
    TransactionalRunner txRunner;

    @BeforeEach
    public void init() {
        txRunner.doInTransaction(entityManager -> {
            MyEntity myEntity = new MyEntity();
            myEntity.setId(1L);
            myEntity.setDescription("USB Flash Drive");
            myEntity.setPrice(BigDecimal.valueOf(12.99));
            entityManager.persist(myEntity);
        });
    }

    @AfterEach
    public void afterEach() {
        txRunner.doInTransaction(entityManager -> {
            Query q = entityManager.createQuery("delete from MyEntity");
            q.executeUpdate();
        });
    }

    private void testPessimisticLocking(LockRequestInterface primaryLockRequestCallable, LockRequestInterface secondaryLockRequestCallable) {

        MyEntity entity = txRunner.doInTransaction(em -> {
            return em.find(MyEntity.class, 1L);
        });

        txRunner.doInTransaction(em -> {
            try {
//                MyEntity myEntity = em.find(MyEntity.class, 1L);
                primaryLockRequestCallable.lock(em, entity);
                executeAsync(
                        () -> {
                            sleep(10);
                            txRunner.doInTransaction(em2 -> {
//                                MyEntity data = em2.find(MyEntity.class, 1L);
                                secondaryLockRequestCallable.lock(em2, entity);
                            });
                        },
                        endLatch::countDown
                );
                sleep(WAIT_MILLIS);
            } catch (StaleObjectStateException e) {
                LOGGER.info("Optimistic locking failure: ", e);
            }
        });
        awaitOnLatch(endLatch);
    }

    @Test
    public void testPessimisticReadDoesNotBlockPessimisticRead() {
        LOGGER.debug("Test PESSIMISTIC_READ doesn't block PESSIMISTIC_READ");
        testPessimisticLocking(
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_READ);
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_READ);
//                    session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_READ acquired");
                },
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_READ);
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_READ);
//                    session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_READ acquired");
                }
        );
    }

    @Test
    public void testPessimisticReadBlocksUpdate() {
        LOGGER.info("Test PESSIMISTIC_READ blocks UPDATE");
        testPessimisticLocking(
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_READ);
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_READ);
//                    session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_READ acquired");
                },
                (entityManager, myEntity) -> {
                    myEntity.setDescription("USB Flash Memory Stick");
                    entityManager.flush();
                    LOGGER.info("Implicit lock acquired");
                }
        );
    }

    @Test
    public void testPessimisticReadWithPessimisticWriteNoWait() {
        LOGGER.info("Test PESSIMISTIC_READ blocks PESSIMISTIC_WRITE, NO WAIT fails fast");
        testPessimisticLocking(
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_READ);
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_READ);
//                    entityManager.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_READ acquired");
                },
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_WRITE, Map.of("javax.persistence.lock.timeout", 0));
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_WRITE, Map.of("javax.persistence.lock.timeout", 0));
//                    entityManager.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).setTimeOut(Session.LockRequest.PESSIMISTIC_NO_WAIT).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_WRITE acquired");
                }
        );
    }

    @Test
    public void testPessimisticReadWithPessimisticWriteTimeout() {
        LOGGER.info("Test PESSIMISTIC_READ blocks PESSIMISTIC_WRITE, NO WAIT fails fast");
        testPessimisticLocking(
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_READ);
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_READ);
//                    entityManager.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_READ acquired");
                },
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_WRITE, Map.of("javax.persistence.lock.timeout", 5));
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_WRITE, Map.of("javax.persistence.lock.timeout", 0));
//                    entityManager.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).setTimeOut(Session.LockRequest.PESSIMISTIC_NO_WAIT).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_WRITE acquired");
                }
        );
    }

    @Test
    public void testPessimisticWriteBlocksPessimisticRead() {
        LOGGER.info("Test PESSIMISTIC_WRITE blocks PESSIMISTIC_READ");
        testPessimisticLocking(
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_WRITE);
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_WRITE);
//                    entityManager.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_WRITE acquired");
                },
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_READ);
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_READ);
//                    entityManager.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_READ acquired");
                }
        );
    }

    @Test
    public void testPessimisticWriteBlocksPessimisticWrite() {
        LOGGER.info("Test PESSIMISTIC_WRITE blocks PESSIMISTIC_WRITE");
        testPessimisticLocking(
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_WRITE);
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_WRITE);
//                    entityManager.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_WRITE acquired");
                },
                (entityManager, myEntity) -> {
                    entityManager.find(MyEntity.class, myEntity.getId(), LockModeType.PESSIMISTIC_WRITE);
//                    entityManager.lock(myEntity, LockModeType.PESSIMISTIC_WRITE);
//                    entityManager.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).lock(myProduct);
                    LOGGER.info("PESSIMISTIC_WRITE acquired");
                }
        );
    }


}

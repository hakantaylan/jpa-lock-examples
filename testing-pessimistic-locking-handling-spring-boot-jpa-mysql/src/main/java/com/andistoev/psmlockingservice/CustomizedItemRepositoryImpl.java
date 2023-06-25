package com.andistoev.psmlockingservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class CustomizedItemRepositoryImpl implements CustomizedItemRepository {

    private final CustomizedItemRepositoryContext customizedItemRepositoryContext;

    private final EntityManager em;

    @Override
    public void setLockTimeout(long timeoutDurationInMs) {
        customizedItemRepositoryContext.setLockTimeout(timeoutDurationInMs);
    }

    @Override
    public long getLockTimeout() {
        return customizedItemRepositoryContext.getLockTimeout();
    }

    @Override
    public Item getItemAndObtainPessimisticWriteLockingOnItById(UUID id) {
        log.info("Trying to obtain pessimistic lock ...");

        Query query = em.createQuery("select item from Item item where item.id = :id");
        query.setParameter("id", id);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query = customizedItemRepositoryContext.setLockTimeoutIfRequired(query);
        Item item = (Item) query.getSingleResult();

        log.info("... pessimistic lock obtained ...");

        customizedItemRepositoryContext.insertArtificialDealyAtTheEndOfTheQueryForTestsOnly();

        log.info("... pessimistic lock released.");

        return item;
    }
}

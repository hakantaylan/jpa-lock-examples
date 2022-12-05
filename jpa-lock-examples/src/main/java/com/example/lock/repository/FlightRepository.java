package com.example.lock.repository;

import com.example.lock.entity.Flight;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Optional;

public interface FlightRepository extends CrudRepository<Flight, Long> {

        @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "10000")})
    Optional<Flight> findWithLockingById(Long id);

}

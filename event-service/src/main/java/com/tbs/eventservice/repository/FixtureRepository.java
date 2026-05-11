package com.tbs.eventservice.repository;

import com.tbs.eventservice.entity.Fixture;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** JPA repository for Fixture persistence and locking. */
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    // Acquires a DB-level pessimistic write lock — used inside the distributed lock to prevent split-brain
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Fixture f WHERE f.id = :id")
    Optional<Fixture> findByIdWithPessimisticLock(@Param("id") Long id);

    // Used by sync scheduler to restrict reconciliation to fixtures not yet played
    List<Fixture> findAllByCurrentScheduledStartTimeAfter(LocalDateTime threshold);
}

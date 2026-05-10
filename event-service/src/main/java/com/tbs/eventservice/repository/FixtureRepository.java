package com.tbs.eventservice.repository;

import com.tbs.eventservice.entity.Fixture;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** JPA repository for Fixture persistence and locked reads. */
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    // Pessimistic write lock — used inside the distributed lock for the double-check seat verification
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Fixture f WHERE f.id = :id")
    Optional<Fixture> findByIdWithLock(@Param("id") Long id);
}

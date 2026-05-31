package com.tbs.eventservice.repository;

import com.tbs.eventservice.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for Season persistence. */
@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
}

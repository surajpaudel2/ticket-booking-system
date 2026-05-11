package com.tbs.bookingservice.repository;

import com.tbs.bookingservice.entity.OutboxEvent;
import com.tbs.bookingservice.entity.enums.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** JPA repository for OutboxEvent persistence. */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // Used by outbox scheduler to fetch events ready for publishing, skipping permanently failed ones
    List<OutboxEvent> findAllByStatusAndRetryCountLessThan(OutboxEventStatus status, int maxRetry);
}

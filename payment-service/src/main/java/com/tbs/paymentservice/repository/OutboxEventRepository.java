package com.tbs.paymentservice.repository;

import com.tbs.paymentservice.entity.OutboxEvent;
import com.tbs.paymentservice.entity.enums.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // Used by OutboxScheduler to fetch events ready for publishing, skipping permanently failed ones
    List<OutboxEvent> findAllByStatusAndRetryCountLessThan(OutboxEventStatus status, int maxRetry);
}

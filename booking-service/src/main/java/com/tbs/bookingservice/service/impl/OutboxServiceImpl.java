package com.tbs.bookingservice.service.impl;

import com.tbs.bookingservice.entity.OutboxEvent;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.mapper.OutboxMapper;
import com.tbs.bookingservice.repository.BookingOutboxEventRepository;
import com.tbs.bookingservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final BookingOutboxEventRepository bookingOutboxEventRepository;
    private final OutboxMapper outboxMapper;

    @Override
    @Transactional
    public void saveEvent(OutboxEventType type, Object payload, Long bookingId, Long bookingAttemptId) {
        OutboxEvent event = outboxMapper.toPendingOutboxEvent(type, payload, bookingId, bookingAttemptId);
        bookingOutboxEventRepository.save(event);
        log.info("Saved outbox event type={} bookingId={} attemptId={}", type, bookingId, bookingAttemptId);
    }
}
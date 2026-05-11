package com.tbs.bookingservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.bookingservice.entity.OutboxEvent;
import com.tbs.bookingservice.entity.enums.OutboxEventStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.repository.OutboxEventRepository;
import com.tbs.bookingservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saves domain events to the outbox table. Events are later published to RabbitMQ
 * by the OutboxScheduler. Serializes payload to JSON using Jackson ObjectMapper.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    // Serializes payload and persists a PENDING outbox event linked to optional booking/attempt IDs
    @Override
    @Transactional
    public void saveEvent(OutboxEventType type, Object payload, Long bookingId, Long bookingAttemptId) {
        String json = serializePayload(payload);
        OutboxEvent event = OutboxEvent.builder()
                .eventType(type)
                .payload(json)
                .status(OutboxEventStatus.PENDING)
                .bookingId(bookingId)
                .bookingAttemptId(bookingAttemptId)
                .build();
        outboxEventRepository.save(event);
        log.info("Saved outbox event type={} bookingId={} attemptId={}", type, bookingId, bookingAttemptId);
    }

    // Wraps checked JsonProcessingException as RuntimeException to avoid polluting callers
    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize outbox payload", ex);
        }
    }
}

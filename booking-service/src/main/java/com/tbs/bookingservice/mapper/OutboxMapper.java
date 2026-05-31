package com.tbs.bookingservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tbs.bookingservice.entity.OutboxEvent;
import com.tbs.bookingservice.entity.enums.OutboxEventStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OutboxMapper {

    private final ObjectMapper objectMapper;

    public OutboxEvent toPendingOutboxEvent(OutboxEventType type, Object payload,
                                            Long bookingId, Long bookingAttemptId) {
        return OutboxEvent.builder()
                .eventType(type)
                .payload(serializePayload(payload))
                .status(OutboxEventStatus.PENDING)
                .bookingId(bookingId)
                .bookingAttemptId(bookingAttemptId)
                .build();
    }

    // Wraps checked JsonProcessingException as RuntimeException to avoid polluting callers
    private String serializePayload(Object payload) {
        return objectMapper.writeValueAsString(payload);
    }
}
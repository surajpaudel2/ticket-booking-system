package com.tbs.paymentservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tbs.paymentservice.entity.OutboxEvent;
import com.tbs.paymentservice.entity.enums.OutboxEventStatus;
import com.tbs.paymentservice.entity.enums.OutboxEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OutboxMapper {

    private final ObjectMapper objectMapper;

    public OutboxEvent toPendingOutboxEvent(OutboxEventType type, Object payload, Long paymentId) {
        return OutboxEvent.builder()
                .eventType(type)
                .payload(serializePayload(payload))
                .status(OutboxEventStatus.PENDING)
                .paymentId(paymentId)
                .build();
    }

    private String serializePayload(Object payload) {
            return objectMapper.writeValueAsString(payload);
    }
}
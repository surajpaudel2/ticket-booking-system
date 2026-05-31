package com.tbs.paymentservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.paymentservice.entity.OutboxEvent;
import com.tbs.paymentservice.entity.enums.OutboxEventStatus;
import com.tbs.paymentservice.messaging.payload.outbound.PaymentFailedEventPayload;
import com.tbs.paymentservice.messaging.payload.outbound.PaymentSucceededEventPayload;
import com.tbs.paymentservice.messaging.publisher.PaymentEventPublisher;
import com.tbs.paymentservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Polls PENDING outbox events every 30 seconds and publishes them to RabbitMQ.
 * Retries up to MAX_RETRY times; permanently failing events are left for manual investigation.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxScheduler {

    private static final int MAX_RETRY = 3;

    private final OutboxEventRepository outboxEventRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 30000)
    public void processOutboxEvents() {
        outboxEventRepository
                .findAllByStatusAndRetryCountLessThan(OutboxEventStatus.PENDING, MAX_RETRY)
                .forEach(this::publishEvent);
    }

    private void publishEvent(OutboxEvent event) {
        try {
            dispatch(event);
            markPublished(event);
        } catch (Exception ex) {
            log.error("Failed to publish outbox event id={} type={} attempt={}",
                    event.getId(), event.getEventType(), event.getRetryCount() + 1, ex);
            incrementRetry(event);
        }
    }

    private void dispatch(OutboxEvent event) throws Exception {
        switch (event.getEventType()) {
            case PAYMENT_SUCCEEDED -> paymentEventPublisher.publishPaymentSucceeded(
                    deserialize(event.getPayload(), PaymentSucceededEventPayload.class));
            case PAYMENT_FAILED -> paymentEventPublisher.publishPaymentFailed(
                    deserialize(event.getPayload(), PaymentFailedEventPayload.class));
        }
    }

    private void markPublished(OutboxEvent event) {
        event.setStatus(OutboxEventStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        outboxEventRepository.save(event);
        log.info("Published outbox event id={} type={}", event.getId(), event.getEventType());
    }

    private void incrementRetry(OutboxEvent event) {
        event.setRetryCount(event.getRetryCount() + 1);
        outboxEventRepository.save(event);
    }

    private <T> T deserialize(String json, Class<T> type) throws Exception {
        return objectMapper.readValue(json, type);
    }
}
package com.tbs.paymentservice.service.impl;

import com.tbs.paymentservice.entity.OutboxEvent;
import com.tbs.paymentservice.entity.enums.OutboxEventType;
import com.tbs.paymentservice.mapper.OutboxMapper;
import com.tbs.paymentservice.repository.OutboxEventRepository;
import com.tbs.paymentservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxMapper outboxMapper;

    @Override
    @Transactional
    public void saveEvent(OutboxEventType type, Object payload, Long paymentId) {
        OutboxEvent event = outboxMapper.toPendingOutboxEvent(type, payload, paymentId);
        outboxEventRepository.save(event);
        log.info("Saved outbox event type={} paymentId={}", type, paymentId);
    }
}
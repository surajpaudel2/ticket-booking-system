package com.tbs.paymentservice.service;

import com.tbs.paymentservice.entity.enums.OutboxEventType;

public interface OutboxService {
    void saveEvent(OutboxEventType type, Object payload, Long paymentId);
}

package com.tbs.paymentservice.entity.enums;

public enum OutboxEventType {

    // Published to payment.succeeded exchange — triggers booking confirmation and success notification.
    PAYMENT_SUCCEEDED,

    // Published to payment.failed exchange — triggers seat release and failure notification.
    PAYMENT_FAILED
}

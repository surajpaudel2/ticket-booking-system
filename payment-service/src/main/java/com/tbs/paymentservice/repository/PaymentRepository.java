package com.tbs.paymentservice.repository;

import com.tbs.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Used by the webhook handler to correlate Stripe events back to a local payment record
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
}

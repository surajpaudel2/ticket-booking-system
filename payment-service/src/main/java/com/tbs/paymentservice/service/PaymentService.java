package com.tbs.paymentservice.service;

import com.tbs.paymentservice.dto.request.InitiatePaymentRequest;
import com.tbs.paymentservice.dto.response.InitiatePaymentResponse;

public interface PaymentService {

    InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request);

    void handlePaymentSucceeded(String paymentIntentId);

    void handlePaymentFailed(String paymentIntentId, String failureReason);
}

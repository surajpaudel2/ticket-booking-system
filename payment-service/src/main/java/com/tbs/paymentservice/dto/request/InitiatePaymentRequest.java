package com.tbs.paymentservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for payment initiation — validated before entering the service layer. */
@Getter
@Setter
@NoArgsConstructor
public class InitiatePaymentRequest {

    @NotNull
    private Long bookingId;

    @NotNull
    private Double amount;

    @NotNull
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    private Long userId;
}

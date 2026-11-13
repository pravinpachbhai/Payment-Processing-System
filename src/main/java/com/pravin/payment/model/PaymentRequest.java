package com.pravin.payment.model;

import java.math.BigDecimal;

public record PaymentRequest(
        String paymentId,
        BigDecimal amount,
        String currency,
        String idempotencyKey
) {}

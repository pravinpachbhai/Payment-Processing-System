package com.pravin.payment.model;

public record PaymentResponse(
        String paymentId,
        String provider,
        String status,
        String providerTransactionId
) {}

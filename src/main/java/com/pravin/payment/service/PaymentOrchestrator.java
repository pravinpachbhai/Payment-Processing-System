package com.pravin.payment.service;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.pravin.payment.model.PaymentProvider;
import com.pravin.payment.model.PaymentRequest;
import com.pravin.payment.model.PaymentResponse;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class PaymentOrchestrator {

    private final RestClient restClient;
    private final CircuitBreakerRegistry circuitBreakers;
    private final RetryRegistry retries;
    private final BulkheadRegistry bulkheads;
    private final MeterRegistry meterRegistry;

    private final String applePayUrl;
    private final String googleUrl;
    private final String zelleUrl;

    public PaymentOrchestrator(
            RestClient paymentRestClient,
            CircuitBreakerRegistry circuitBreakers,
            RetryRegistry retries,
            BulkheadRegistry bulkheads,
            MeterRegistry meterRegistry,
            @Value("${payment.providers.apple-pay-url}") String applePayUrl,
            @Value("${payment.providers.google-pay-url}") String googleUrl,
            @Value("${payment.providers.zelle-pay-url}") String zelleUrl) {

        this.restClient = paymentRestClient;
        this.circuitBreakers = circuitBreakers;
        this.retries = retries;
        this.bulkheads = bulkheads;
        this.meterRegistry = meterRegistry;
        this.applePayUrl = applePayUrl;
        this.googleUrl = googleUrl;
        this.zelleUrl = zelleUrl;
    }

    public PaymentResponse process(PaymentProvider provider, PaymentRequest request) {

        String backend = provider.name();
        String url = baseUrl(provider);

        CircuitBreaker circuitBreaker = circuitBreakers.circuitBreaker(backend);
        Retry retry = retries.retry(backend);
        Bulkhead bulkhead = bulkheads.bulkhead(backend);

        Timer.Sample sample = Timer.start(meterRegistry);
        Timer timer = Timer.builder("payment.downstream.duration").tag("provider", backend).register(meterRegistry);

        try {
            Supplier<PaymentResponse> httpCall = () ->
                    restClient.post()
                            .uri(url + "/payments")
                            .header("Idempotency-Key", request.idempotencyKey())
                            .body(request)
                            .retrieve()
                            .body(PaymentResponse.class);

            Supplier<PaymentResponse> protectedCall = CircuitBreaker.decorateSupplier(circuitBreaker,
                                 Retry.decorateSupplier(retry, Bulkhead.decorateSupplier(bulkhead, httpCall)));

            return protectedCall.get();
        } finally {
            sample.stop(timer);
        }
    }

    private String baseUrl(PaymentProvider provider) {
        return switch (provider) {
            case APPLE_PAY -> applePayUrl;
            case GOOGLE_PAY -> googleUrl;
            case ZELLE_PAY -> zelleUrl;
        };
    }
}

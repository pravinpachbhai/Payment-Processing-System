package com.pravin.payment.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.core.IntervalFunction;
import org.springframework.web.client.HttpStatusCodeException;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(50)
                .minimumNumberOfCalls(20)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordException(ResilienceConfig::isInfrastructureFailure)
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3) // Includes the initial attempt
                .intervalFunction(IntervalFunction.ofExponentialBackoff(100L, 2.0, 1_000L))
                .retryOnException(ResilienceConfig::isRetryable)
                .build();

        return RetryRegistry.of(config);
    }

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(80)
                .maxWaitDuration(Duration.ZERO)
                .build();

        return BulkheadRegistry.of(config);
    }

    public static boolean isRetryable(Throwable throwable) {
        if (throwable instanceof HttpStatusCodeException ex) {
            int status = ex.getStatusCode().value();
            return status == 429 || status == 502 || status == 503 || status == 504;
        }
        return false;
    }

    public static boolean isInfrastructureFailure(Throwable throwable) {
        if (throwable instanceof HttpStatusCodeException ex) {
            return ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429;
        }
        return throwable instanceof org.springframework.web.client.ResourceAccessException;
    }
}

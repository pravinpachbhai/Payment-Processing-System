package com.pravin.payment.controller;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
            RejectedExecutionException.class,
            BulkheadFullException.class,
            CallNotPermittedException.class
    })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> overloaded(Exception exception) {
        return Map.of(
                "status", "REJECTED",
                "message", "Payment service is temporarily unavailable"
        );
    }
}

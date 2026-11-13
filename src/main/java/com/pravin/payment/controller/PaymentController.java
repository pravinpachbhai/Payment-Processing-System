package com.pravin.payment.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.web.bind.annotation.*;

import com.pravin.payment.model.PaymentProvider;
import com.pravin.payment.model.PaymentRequest;
import com.pravin.payment.model.PaymentResponse;
import com.pravin.payment.service.PaymentOrchestrator;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentOrchestrator orchestrator;
    private final ExecutorService paymentExecutor;

    public PaymentController(PaymentOrchestrator orchestrator, ExecutorService paymentExecutor) {
        this.orchestrator = orchestrator;
        this.paymentExecutor = paymentExecutor;
    }

    @PostMapping("/{provider}")
    public CompletableFuture<PaymentResponse> pay(@PathVariable PaymentProvider provider, @RequestBody PaymentRequest request) {
        //NOTE: RestClient is blocking. Execute it on a bounded worker pool, not on the servlet request thread.
        return CompletableFuture.supplyAsync(() -> orchestrator.process(provider, request), paymentExecutor);
    }
}

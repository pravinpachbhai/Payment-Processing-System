package com.pravin.payment.monitoring;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class ConnectionPoolMetrics {

    public ConnectionPoolMetrics(PoolingHttpClientConnectionManager manager, MeterRegistry registry) {

        Gauge.builder("payment.http.pool.leased", manager,
                m -> m.getTotalStats().getLeased())
                .description("Currently leased Apache HTTP connections")
                .register(registry);

        Gauge.builder("payment.http.pool.available", manager,
                m -> m.getTotalStats().getAvailable())
                .description("Available idle Apache HTTP connections")
                .register(registry);

        Gauge.builder("payment.http.pool.pending", manager,
                m -> m.getTotalStats().getPending())
                .description("Threads waiting to lease an HTTP connection")
                .register(registry);

        Gauge.builder("payment.http.pool.max", manager,
                m -> m.getTotalStats().getMax())
                .description("Maximum total Apache HTTP connections")
                .register(registry);
    }
}

# Spring Boot 4.0.5 Payment Orchestrator

Java 25:

- Spring MVC + `CompletableFuture`
- `ExecutorService` with bounded queue and rejection
- `RestClient` + Apache HttpClient 5 pooling
- Resilience4j core APIs: CircuitBreaker, Retry, Bulkhead
- Micrometer / Actuator / Prometheus
- Connection pool gauges

## Run

```bash
mvn clean spring-boot:run
```

## Example request

```bash
curl -X POST http://localhost:8080/api/payments/GOOGLE_PAY \
  -H 'Content-Type: application/json' \
  -d '{
    "paymentId": "p-10001",
    "amount": 19.95,
    "currency": "USD",
    "idempotencyKey": "idem-p-10001"
  }'
```

## Metrics

- `GET /actuator/health`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`



## Event-Driven System (Spring Boot + Kafka) — BrokerCore
BrokerCore is a multi-module system built with Spring Boot that owns the Apache Kafka producer and consumer stack end-to-end.                                     
It enforces single-responsibility layers, centralizes topic management, and uses a deterministic partition-key design to guarantee per-entity ordering while scaling horizontally and enabling resilient, scalable event-driven processing.
## Goals
**Decouple services at scale**: replace synchronous service-to-service calls with Kafka events so each module can be deployed, scaled, and failed independently, avoiding cascading outages.

**Guarantee ordering and reliability**: use deterministic partition keys, manual/explicit acknowledgments, and idempotent handlers to ensure per-entity ordering and prevent silent message loss under high load.

**Prove horizontal scalability**: validate that adding partitions or consumer instances increases throughput linearly by measuring consumer lag, throughput, and end-to-end latency; demonstrate the architecture meets real-world payment-volume growth.

**Scalable event-driven processing**: combine centralized topic management, schema governance, and observability to enable safe, repeatable scaling and operations.

## Services
**Payment Service — Producer / Double-Charge Protection**
 Validates incoming payment requests and blocks duplicate in-flight submissions using an in-memory Bloom filter.
 Publishes deduplicated payment messages to Kafka for durable downstream processing.

 **The problem (why double-charges happen)**                                                             
 Real-world clients or infra may resend the same payment request (same paymentKey) multiple times in quick succession:

 - Double-click / accidental resubmit

 - Client retry after a perceived timeout

 - Mobile/OS network retries

 **Solution O(1)**: 
 - In-memory Bloom filter (double-hashed over a shared BitSet) at the HTTP entrypoint to detect and block duplicate in-flight payment requests.
 - Optimized in-memory Bloom filter (double hashing + optimal size/hash tuning)
  **to minimize collision probability and significantly reduce false positives under high-throughput duplicate-check workloads**.
                                        
 **Notification & Observation**     
  Implements Observe_mail an **Observer-pattern** service that subscribes to the payment-confirmations Kafka topic and reacts immediately when a transaction completes.
  Automatically sends transactional email notifications to users containing the transaction ID and final status.
## Push / Pull Event Flow

```
                 POST /api/payment/process
                           │
                           ▼  
          HTTP → Bloom Filter (O(1) duplicate check) BloomFilterInterceptor → blocked? 
                           │ allowed → mark(key)
                           ▼  PUSH
      PaymentTransactionService → BaseProducer → KafkaTemplate partition = hash(userId) % N
                           │                
                           ▼
                 Kafka: payment-events topic
                           │
                           ▼  PULL (@KafkaListener)
         PaymentNotificationConsumer → Worker → StripePaymentAdapter → ProcessingResult
                                            │
                                            ├─ success → ACK + unmark(key) + PUSH → payment-confirmations
                                            └─ failure → no ACK → Kafka redelivers (retry topic)
                                                                   │
                                                   ┌───────────────┴───────────────┐
                                                   ▼  PULL                         ▼  PULL
                                       PaymentConfirmationListener            TransactionEventListener 
                                                   |                                       |
                                         (update DB → COMPLETED)            (Observe_mail → send email to user)
                                                   |                                       | 
                                          bloomFilter.unmark(key)                    send email to user
                                                   |
                                               bloom bit → 0 
```

### Design Patterns

| Pattern | Implementation |
|---|---|
| **Singleton** | Database connections, Kafka producer/consumer configurations, and core services (`@Service`, `@Component`) are all singletons — one shared instance is created once and reused across the entire application lifecycle, preventing redundant connections and resource waste. |
| **Observer** | `Observe_mail` subscribes to the confirmation topic and reacts to events without being called. The producer never knows the mail service exists. |
| **Adapter** | `StripePaymentAdapter` and `DeliveryAdapter` wrap external APIs behind a shared interface — the worker only depends on the interface, never the provider. |
| **Builder** | `PaymentEvent`, `PaymentRequestDTO`, and `PaymentResponseDTO` use Lombok `@Builder` for clean, readable object construction. |
| **Factory** | `KafkaProducerFactory` produces `ProducerFactory<String,String>` and `KafkaTemplate` as Spring beans callers receive a fully configured producer without knowing how it was built. `KafkaListenerContainerFactory` does the same for consumers. |
| **Template Method** | `BaseProducer` defines the invariant publish algorithm (`publishEvent` → `publishEventWithKey` → async send + logging); concrete producers such as `PaymentProducer` extend it and only override `producerName()` to identify themselves the retry and error-handling skeleton never changes. |
| **Publisher/Subscriber** | `EventPublisher` interface decouples the sender from every listener  `BaseProducer` publishes a `ProducerEvent` to a Kafka topic and has no knowledge of how many consumers, if any, are subscribed downstream. |


### Tech Stack

Java 21 · Spring Boot 3.2.2 · Apache Kafka (Confluent 7.5.0) · Spring Kafka (manual ACK) · Stripe API · Maven multi-module · Docker Compose


## System Architecture & Flow
![System Architecture](diagrams/System%20Architecture.png)
## Modules

| Module | Responsibility |
|---|---|
| `kafka_architecture` | Shared contract library — owns `PaymentEvent` schema, domain models, and `EventPublisher` interface; zero runtime logic, pure cross-module dependency |
| `producer/payment` | HTTP entry point — Bloom-filter deduplication, deterministic-key `PaymentEvent` publishing to Kafka, and confirmation-callback consumption |
| `consumer` | Kafka consumer layer — pulls messages from payment and retry topics, coordinates with the worker, publishes confirmations |
| `worker` | Business logic core — processes payment events through the adapter layer, returns structured `ProcessingResult` |
| `Observe_mail` | Notification observer — silently watches the confirmation topic and triggers email delivery |


## Run Locally

### Steps
**1. Clone**
```bash
git clone https://github.com/fadynaeem/Broker_core.git
cd tmp_broker
```
**2. Start Kafka and Zookeeper**
```bash
docker-compose up -d
```

**3. Build all modules**
```bash
mvn clean install
```

**4. Start all services**
```bash
start-Services.bat

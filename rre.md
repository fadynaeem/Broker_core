# Event-Driven System (Spring Boot + Kafka)

Async payment processing across decoupled microservices. Every payment is a Kafka event — no direct service-to-service calls.

---

## Modules

| Module | Role |
|---|---|
| `event_mangement` | Shared library — `ProducerEvent` interface, `PaymentEvent`, `EventPublisher` |
| `producer/payment` | REST entry point — creates payment, pushes event to Kafka, listens for confirmation |
| `consumer` | Pulls events from Kafka, delegates to worker, publishes confirmation |
| `worker` | Processes events via Stripe adapter, returns `ProcessingResult` |
| `Observe_mail` | Silent observer — watches confirmation topic, sends email |

---

## Push / Pull Event Flow

```
POST /payment
    │
    ▼  PUSH
KafkaEventPublisher → payment-notifications-payment
    │
    ▼  PULL (@KafkaListener)
PaymentNotificationConsumer → Worker → ProcessingResult
    │
    ├─ success → ACK + PUSH → payment-confirmations
    └─ failure → no ACK → Kafka redelivers (retry topic)
                              │
              ┌───────────────┴───────────────┐
              ▼  PULL                         ▼  PULL
  PaymentConfirmationListener          Observe_mail
  (update DB → COMPLETED,              (send email to user)
   push → mail topic)
```

**`event_mangement` — shared contract for both sides:**

| Type | Side | Role |
|---|---|---|
| `ProducerEvent` | PUSH | Interface every event must implement (`getEventId`, `getEventType`, `getCreatedAt`, `toJson`) |
| `PaymentEvent` | PUSH | Concrete event — holds `transactionId`, `userEmail`, `amount`, `currency` |
| `EventPublisher` | PUSH | Interface with `publishEvent(event, topic)` — decouples producer from Kafka |
| `KafkaEventPublisher` | PUSH | Implements `EventPublisher` — serializes event to JSON, calls `kafkaTemplate.send(topic, transactionId, json)` |

**PUSH** — producer only knows `EventPublisher`. `KafkaEventPublisher` handles serialization and delivery; a `CompletableFuture` callback logs the result without blocking.  
**PULL** — `@KafkaListener` in the consumer polls Kafka. Manual `acknowledgment.acknowledge()` commits the offset **only on success** — failure leaves the offset open so Kafka redelivers automatically.

---

## Key Design Patterns

| Pattern | Where |
|---|---|
| **Publisher/Subscriber** | `KafkaEventPublisher` — producer has no knowledge of consumers |
| **Observer** | `Observe_mail` — reacts to confirmations without being called |
| **Adapter** | `StripePaymentAdapter` — worker depends on interface, not Stripe directly |

---

## Tech Stack

Java 21 · Spring Boot 3.2.2 · Apache Kafka (Confluent 7.5.0) · Spring Kafka (manual ACK) · Stripe API · Maven multi-module · Docker Compose

---

## Run Locally

```bash
git clone https://github.com/fadynaeem/Broker_core.git
cd tmp_broker
docker-compose up -d       # Start Kafka + Zookeeper
mvn clean install          # Build all modules
start-Services.bat         # Start all services
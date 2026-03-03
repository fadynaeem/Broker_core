# BrokerCore — Event-Driven Payment System

A **Spring Boot** multi-module application that uses **Apache Kafka** as a message broker to manage payment event flow across fully decoupled microservices. The system is designed around real-world distributed architecture principles — where every payment is an event, every service has one job, and nothing is processed inline.

---

## Features

### Payment Processing
- Accept payment requests via a REST API endpoint.
- Publish payment events to Kafka topics for asynchronous processing.
- Support for multiple currencies and payment methods.
- Automatic transaction ID generation and status tracking.

### Event-Driven Flow
- Payments are never processed synchronously — every transaction is converted into a Kafka event.
- Dedicated retry topics ensure no payment is silently dropped on processing failure.
- Manual acknowledgment on all consumers — a message is only committed once successfully processed.

### Worker & Adapter System
- Pluggable payment adapters (e.g. `StripePaymentAdapter`) — swap providers with zero changes to the core logic.
- Delivery adapter layer abstracts the external API entirely from the processing pipeline.
- Processing results carry full context (success, failure, or error) back to the consumer.

### Notification & Observation
- `Observe_mail` independently watches the payment confirmation topic.
- Sends automated email alerts to users upon transaction completion.
- Operates with no coupling to any other service — purely event-driven.

### Reliability & Retry Mechanism
- Primary topic + retry topic per event type.
- Failed messages are re-queued without blocking the main consumer.
- Acknowledgment is withheld until the worker confirms successful delivery.

---

## Technical Overview

### Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.2 |
| Messaging | Apache Kafka (Confluent 7.5.0) |
| Coordination | Apache Zookeeper |
| Kafka Client | Spring Kafka (manual acknowledgment) |
| Payment Adapter | Stripe API |
| Build Tool | Maven (multi-module) |
| Containerization | Docker / Docker Compose |

### Design Principles

- **SOLID** — each class has one reason to change; dependencies flow inward through interfaces.
- **Event-Driven Architecture** — services communicate exclusively via Kafka topics, never direct calls.
- **Hexagonal Architecture** — the core processing logic (`worker`) is isolated from the transport layer through adapter interfaces.

### Design Patterns

| Pattern | Implementation |
|---|---|
| **Observer** | `Observe_mail` subscribes to the confirmation topic and reacts to events without being called. The producer never knows the mail service exists. |
| **Adapter** | `StripePaymentAdapter` and `DeliveryAdapter` wrap external APIs behind a shared interface — the worker only depends on the interface, never the provider. |
| **Builder** | `PaymentEvent`, `PaymentRequestDTO`, and `PaymentResponseDTO` use Lombok `@Builder` for clean, readable object construction. |
| **Factory** | `KafkaListenerContainerFactory` dynamically creates configured consumer containers — the consumer never manages its own connection setup. |
| **Singleton** | Database connections, Kafka producer/consumer configurations, and core services (`@Service`, `@Component`) are all singletons — one shared instance is created once and reused across the entire application lifecycle, preventing redundant connections and resource waste. |
| **Publisher/Subscriber** | `KafkaEventPublisher` implements `EventPublisher` — the producer sends events without knowing who, if anyone, is listening. |

---

## Architecture & Flow

```
Client
  │
  ▼
[producer/payment]  ──► Kafka: payment-events topic
                                    │
                                    ▼
                             [consumer]  ──► [worker]  ──► StripePaymentAdapter
                                    │
                         (success) ─┴─ Kafka: payment-confirmations topic
                                                │
                                       ┌────────┴────────┐
                                       ▼                 ▼
                              [producer/payment]    [Observe_mail]
                           (update DB status)    (send email alert)
```

**Step-by-step:**

1. `POST /api/payment/process` is received by the **producer** — it builds a `PaymentEvent` and publishes it to Kafka.
2. The **consumer** listens on the payment topic. When a message arrives, it is forwarded to the **worker**.
3. The **worker** deserializes the event and passes it to the `StripePaymentAdapter` for processing.
4. On success, the **consumer** publishes a confirmation event to the confirmations topic and acknowledges the message.
5. On failure, acknowledgment is withheld — the message is retried via the retry topic.
6. The **producer's** `PaymentConfirmationListener` hears the confirmation and updates the payment status in the database.
7. `Observe_mail` independently observes the same confirmation topic and sends an email notification to the user.

---

## Modules

| Module | Responsibility |
|---|---|
| `event_mangement` | Shared library — defines `PaymentEvent`, domain models, and the `EventPublisher` interface consumed by all services |
| `producer/payment` | REST API gateway — accepts payment requests, publishes events to Kafka, listens for confirmation callbacks |
| `consumer` | Kafka consumer layer — pulls messages from payment and retry topics, coordinates with the worker, publishes confirmations |
| `worker` | Business logic core — processes payment events through the adapter layer, returns structured `ProcessingResult` |
| `Observe_mail` | Notification observer — silently watches the confirmation topic and triggers email delivery |

---

## Project Structure

```
BrokerCore/
├── event_mangement/          # Shared events, models, publisher interface
│   └── src/main/java/com/example/shared/
│       ├── event/            # PaymentEvent, EventPublisher, KafkaEventPublisher
│       └── model/            # Channel, DeliveryResult, NotificationMessage
│
├── producer/payment/         # REST API + Kafka publisher + confirmation listener
│   └── src/main/java/com/example/payment/
│       ├── controller/       # PaymentController  →  POST /api/payment/process
│       ├── service/          # PaymentTransactionService
│       ├── listener/         # PaymentConfirmationListener
│       └── model/            # Payment, PaymentStatus
│
├── consumer/                 # Kafka consumers
│   └── src/main/java/com/example/consumer/Pull_Event/
│       ├── PaymentNotificationConsumer   # Listens: payment topic + retry topic
│       ├── EmailNotificationConsumer     # Listens: email topic
│       ├── PaymentConfirmationPublisher  # Publishes confirmation events
│       └── BaseNotificationConsumer      # Shared consumer base
│
├── worker/                   # Payment processor + adapters
│   └── src/main/java/com/example/worker/
│       ├── processor/        # PaymentProcessor, ProcessingResult
│       └── adapter/          # StripePaymentAdapter, DeliveryAdapter
│
├── Observe_mail/             # Email notification observer
│   └── src/main/java/com/example/mail/
│       ├── listener/         # TransactionEventListener  (Kafka consumer)
│       └── service/          # MailNotificationService, MailObservationService
│
├── docker-compose.yml        # Kafka + Zookeeper containers
├── start-Services.bat        # Startup script for all services
└── pom.xml                   # Parent POM (Java 21, Spring Boot 3.2.2)
```

---

## Run Locally

### Prerequisites
- Java 21
- Maven 3.8+
- Docker Desktop

### Steps

**1. Start Kafka and Zookeeper**
```bash
docker-compose up -d
```

**2. Build all modules**
```bash
mvn clean install
```

**3. Start all services**
```bash
start-Services.bat
```

**4. Send a payment request**
```http
POST http://localhost:8080/api/payment/process
Content-Type: application/json

{
  "userId": "user-123",
  "userEmail": "user@example.com",
  "amount": "99.99",
  "currency": "USD",
  "paymentMethod": "card",
  "description": "Order #456"
}
```

**Expected response:**
```json
{
  "transactionId": "txn-abc-123",
  "status": "queued",
  "userId": "user-123",
  "amount": "99.99",
  "currency": "USD",
  "paymentMethod": "card"
}
```

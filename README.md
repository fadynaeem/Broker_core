# Event-Driven System (Spring Boot + Kafka)

A Spring Boot multi-module app where every payment is an event, services are single-responsibility microservices, and nothing is processed inline.
All communication is asynchronous via Apache Kafka, enabling decoupling, resilience, and scalable event-driven processing. 

## Push / Pull Event Flow

```
POST / event
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
### Notification & Observation
- `Observe_mail` is a observer design pattern service.
- Subscribes to the `payment-confirmations` Kafka topic and reacts the moment a transaction completes.
- Sends automated email alerts to the user with transaction ID and final status.


### Design patterns
| Pattern | Implementation |
|---|---|
| **Singleton** | Database connections, Kafka producer/consumer configurations|
| **Observer** | `Observe_mail` subscribes to the confirmation topic and reacts to events without being called. The producer never knows the mail service exists. |
| **Adapter** | `StripePaymentAdapter` and `DeliveryAdapter` wrap external APIs behind a shared interface, the worker only depends on the interface, never the provider. |
| **Builder** | `PaymentEvent`, `PaymentRequestDTO`, and `PaymentResponseDTO` use Lombok `@Builder` for clean, readable object construction. |
| **Factory** | `KafkaListenerContainerFactory` dynamically creates configured consumer containers, the consumer never manages its own connection setup. |


### Tech Stack

Java 21 · Spring Boot 3.2.2 · Apache Kafka (Confluent 7.5.0) · Spring Kafka (manual ACK) · Stripe API · Maven multi-module · Docker Compose


## System Architecture & Flow
![System Architecture](diagrams/System%20Architecture.png)

## Modules

| Module | Responsibility |
|---|---|
| `event_mangement` | Shared library — defines `PaymentEvent`, domain models, and the `EventPublisher` interface consumed by all services |
| `producer/payment` | REST API gateway — accepts payment requests, publishes events to Kafka, listens for confirmation callbacks |
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

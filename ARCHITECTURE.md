# Notification System Architecture

## Services Overview

### 1. Producer Service (Email)
**Path:** `producer/`

#### Features:
- REST API for sending notifications
- Email notification handling
- Kafka message publishing

#### Key Components:
```
producer/
├── src/main/java/com/example/notification/producer/
│   ├── ProducerApplication.java          (Spring Boot Entry Point)
│   ├── config/
│   │   └── ProducerConfig.java          (ObjectMapper Bean)
│   ├── controller/
│   │   └── NotificationController.java  (REST Endpoints)
│   │       └── /api/addnotification/send-email (POST)
│   └── service/
│       └── NotificationProducerService.java (Business Logic)
│           ├── sendSimpleEmail()
│           └── sendNotification()
└── target/
    └── producer-1.0.0.jar

application.properties:
- server.port=8080 (default) or 8888
- spring.kafka.bootstrap-servers=localhost:9092
- kafka.email.topic=notifications-email
```

#### Controller Endpoints:
| Endpoint | Method | Params | Description |
|----------|--------|--------|-------------|
| `/api/addnotification/send-email` | POST | recipient, subject, message | Send email notification |
| `/api/addnotification/ok` | GET | - | Health check |

#### Service Methods:
- `sendSimpleEmail(recipient, subject, message)` → Returns messageId
- `sendNotification(channel, tenantId, recipient, subject, templateId, templateParams)` → Returns dedupeId

---

### 1b. Payment Producer Service
**Path:** `payment/`

#### Features:
- REST API for payment events
- Payment event publishing to Kafka
- Support for multiple payment statuses and methods

#### Key Components:
```
payment/
├── src/main/java/com/example/notification/payment/
│   ├── PaymentServiceApplication.java    (Spring Boot Entry Point)
│   ├── config/
│   │   └── PaymentConfig.java           (ObjectMapper Bean)
│   ├── controller/
│   │   └── PaymentController.java       (REST Endpoints)
│   │       ├── /api/payment/process (POST)
│   │       └── /api/payment/health (GET)
│   ├── service/
│   │   └── PaymentProducerService.java  (Business Logic)
│   │       ├── publishPaymentEvent()
│   │       └── publishPayment()
│   └── model/
│       ├── Payment.java                 (Payment DTO)
│       └── PaymentStatus.java           (Status Enum)
└── target/
    └── payment-1.0.0.jar

application.properties:
- server.port=9090
- spring.kafka.bootstrap-servers=localhost:9092
- kafka.payment.topic=payment-events
```

#### Controller Endpoints:
| Endpoint | Method | Params | Description |
|----------|--------|--------|-------------|
| `/api/payment/process` | POST | userId, amount, currency, paymentMethod, description | Process payment event |
| `/api/payment/health` | GET | - | Health check |

#### Service Methods:
- `publishPaymentEvent(userId, amount, currency, paymentMethod, description)` → Returns transactionId
- `publishPayment(userId, amount, currency, paymentMethod, status, description, failureReason)` → Returns transactionId

#### Model:
- `Payment`: Contains transactionId, userId, amount, currency, paymentMethod, status, etc.
- `PaymentStatus`: PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED

---

### 2. Consumer Service
**Path:** `consumer/`

#### Features:
- Kafka message consumption
- Email topic listener
- Notification processing delegation to Worker

#### Key Components:
```
consumer/
├── src/main/java/com/example/notification/consumer/
│   ├── ConsumerApplication.java
│   ├── config/
│   │   └── ConsumerConfig.java
│   ├── kafka/
│   │   ├── BaseNotificationConsumer.java (Abstract)
│   │   └── EmailNotificationConsumer.java
│   │       ├── @KafkaListener (notifications-email)
│   │       └── @KafkaListener (notifications-email-retry)
└── target/
    └── consumer-1.0.0.jar

application.properties:
- spring.kafka.bootstrap-servers=localhost:9092
- spring.kafka.consumer.group-id=email-worker-group
```

---

### 3. Worker Service (Logic Layer)
**Path:** `worker/`

#### Features:
- Email template rendering
- SendGrid integration
- Email delivery
- Mock mode support

#### Key Components:
```
worker/
├── src/main/java/com/example/notification/worker/
│   ├── config/
│   │   └── WorkerConfig.java (ObjectMapper Bean)
│   ├── adapter/
│   │   ├── DeliveryAdapter.java (Interface)
│   │   └── SendGridEmailAdapter.java (@Component)
│   ├── processor/
│   │   ├── NotificationProcessor.java (@Service)
│   │   │   └── processAndDeliver(message, channel)
│   │   └── ProcessingResult.java (Response Model)
│   └── service/
│       └── TemplateService.java (@Service)
│           ├── render(templateId, params)
│           └── Templates: simple-email, notification-generic
└── target/
    └── worker-1.0.0.jar (Embedded in Consumer)

SendGrid Config:
- sendgrid.api-key=MOCK (for testing)
- sendgrid.from-email=notifications@example.com
- sendgrid.mock-mode=true
```

---

### 4. Shared Module
**Path:** `shared/`

#### Features:
- Common data models
- Enums and constants

#### Key Models:
```
shared/
├── model/
│   ├── Channel.java (EMAIL, SMS, PUSH, etc.)
│   ├── NotificationMessage.java (Main DTO)
│   ├── NotificationStatus.java
│   └── DeliveryResult.java
```

---

## Adding New Producer (Template)

When adding a new producer service:

### 1. Create Producer Module:
```
new-producer/
├── pom.xml (Add shared dependency)
├── src/main/java/com/example/notification/new-producer/
│   ├── NewProducerApplication.java
│   ├── config/
│   │   └── NewProducerConfig.java
│   ├── controller/
│   │   └── NotificationController.java
│   │       └── @PostMapping("/send-{type}")
│   └── service/
│       └── NotificationProducerService.java
└── src/main/resources/
    └── application.properties
```

### 2. Update Consumer (if needed):
```
consumer/
├── kafka/
│   ├── {Type}NotificationConsumer.java
│   │   ├── @KafkaListener(topics="{type}-topic")
│   │   └── @KafkaListener(topics="{type}-topic-retry")
│   └── BaseNotificationConsumer.java
```

### 3. Extend Worker (if needed):
```
worker/
├── adapter/
│   ├── DeliveryAdapter.java (Existing Interface)
│   └── {Type}Adapter.java (New Implementation)
└── service/
    └── TemplateService.java (Add new template type)
```

### 4. Update Main POM:
```xml
<module>new-producer</module>
```

---

## Message Flow

```
REST API (Producer)
    ↓
NotificationProducerService.sendNotification()
    ↓
ObjectMapper: Java Object → JSON
    ↓
KafkaTemplate.send(topic, key, message)
    ↓
Kafka Broker (notifications-email topic)
    ↓
EmailNotificationConsumer @KafkaListener
    ↓
BaseNotificationConsumer.handleKafkaMessage()
    ↓
NotificationProcessor.processAndDeliver()
    ├→ ObjectMapper: JSON → Java Object
    ├→ TemplateService.render()
    └→ SendGridEmailAdapter.deliver()
    ↓
Email Sent (or logged in mock mode)
```

---

## Configuration Summary

| Service | Port | Topic | Config File |
|---------|------|-------|------------|
| Producer (Email) | 8888 | notifications-email | producer/application.properties |
| Payment Producer | 9090 | payment-events | payment/application.properties |
| Consumer | N/A | notifications-email, notifications-email-retry | consumer/application.properties |
| Worker | N/A (embedded) | N/A | worker/config/WorkerConfig.java |

---

## Run Commands

**Start Kafka Infrastructure:**
```bash
.\start-infrastructure.bat
```

**Build All:**
```bash
mvn clean package -DskipTests -q
```

**Run Producer (Email):**
```bash
.\run-producer.bat
```

**Run Payment Producer:**
```bash
.\run-payment.bat
```

**Run Consumer:**
```bash
java -jar consumer/target/consumer-1.0.0.jar
```

**View Kafka Messages - Email Topic:**
```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic notifications-email --from-beginning
```

**View Kafka Messages - Payment Topic:**
```bash
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic payment-events --from-beginning
```

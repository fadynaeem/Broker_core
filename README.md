# Email Notification System - Kafka-Based Mini App

## 🏗️ Architecture Overview

A simple Kafka-based email notification system with retry logic and dead letter queue:

```
Kafka Topic: notifications-email
     ↓
Consumer: email-worker-group
     ↓
Worker Pipeline:
  1. Consume message
  2. Schema validation
  3. Delayed message handling (if scheduled)
  4. Template rendering
  5. Send via SendGrid (or mock mode)
  6. Success → Commit offset
  7. Transient failure → Retry topic (exponential backoff)
  8. Max retries → DLQ (Dead Letter Queue)
```

  ## 💳 Payment Kafka Flow (Current)

  ```
  API /api/payment/process
    ↓
  Payment Producer (save PENDING in DB)
    ↓
  Kafka Topic: payment-events
    ↓
  Consumer: payment-worker-group
    ↓
  Worker (StripePaymentAdapter)
    ↓
  Success → publish confirmation to payment-confirmations + commit offset
  Failure → no ack (message retried)
    ↓
  Payment Producer listener updates DB to COMPLETED
    ↓
  Kafka Topic: maill (email notification event)
    ↓
  Mail Observer sends confirmation email
  ```

  ### Topics Used (Payment)
  - `payment-events` - Payment requests from producer
  - `payment-confirmations` - Confirmation events back to producer
  - `maill` - Email notification events (transactionId + userEmail + status)

## 🚀 Features

- ✅ **Email Notifications** via SendGrid
- ✅ **Delayed Message Delivery** - Schedule emails for future delivery
- ✅ **Template Engine** - Dynamic content with `{{placeholder}}` syntax
- ✅ **Retry Mechanism** - Exponential backoff with configurable retries
- ✅ **Dead Letter Queue** - Captures failed messages after max retries
- ✅ **Mock Mode** - Test without real SendGrid account

## 📋 Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose (for Kafka)

## 🔧 Setup

### 1. Start Kafka

```bash
docker-compose up -d
```

This starts:
- **Kafka** on `localhost:9092`
- **Zookeeper** on `localhost:2181`

### 2. Configure SendGrid (Optional)

Edit `src/main/resources/application.properties`:

```properties
# For real SendGrid Email
sendgrid.api-key=YOUR_SENDGRID_API_KEY
sendgrid.from-email=your-email@example.com
sendgrid.mock-mode=false
```

### 3. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

## 📡 API Endpoints

### Send Simple Email
```bash
curl -X POST "http://localhost:8080/api/notifications/send-email-simple" \
  -d "recipient=user@example.com" \
  -d "message=Hello from notification system!"
```

### Send Email with Template
```bash
curl -X POST "http://localhost:8080/api/notifications/send-email" \
  -d "recipient=user@example.com" \
  -d "subject=Welcome" \
  -d "templateId=welcome-email" \
  -d "templateParams[name]=John" \
  -d "templateParams[appName]=MyApp"
```

### Schedule Future Email
```bash
curl -X POST "http://localhost:8080/api/notifications/send-email" \
  -d "recipient=user@example.com" \
  -d "subject=Reminder" \
  -d "templateId=notification-generic" \
  -d "templateParams[message]=Meeting in 1 hour" \
  -d "sendAt=2026-02-07T14:00:00"
```

### Health Check
```bash
curl http://localhost:8080/api/notifications/health
```

## 📊 Monitoring Kafka

### List Topics
```bash
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### Consume from DLQ
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic notifications-email-dlq \
  --from-beginning
```

## 🎨 Templates

Built-in templates:
- `welcome-email`: "Hello {{name}}, Welcome to {{appName}}!"
- `otp-sms`: "Your OTP is {{otp}}. Valid for {{validity}} minutes."
- `order-push`: "Order {{orderId}} has been {{status}}. Track it here: {{trackingUrl}}"
- `reset-password`: "Hi {{name}}, Click here to reset your password: {{resetUrl}}"
- `notification-generic`: "{{message}}"

Add custom templates in `TemplateService.java`.

## ⚙️ Configuration

### Retry Settings
```properties
# Max retry attempts before DLQ
notification.max-retries=3

# Base backoff time (exponential)
notification.retry-backoff-ms=5000
```

## 🏗️ Project Structure

```
src/main/java/com/example/notification/
├── adapter/              # SendGridEmailAdapter
├── config/               # Kafka configuration
├── consumer/             # EmailNotificationConsumer
├── controller/           # REST API
├── model/                # Domain models
└── service/              # Business logic
    ├── TemplateService.java
    └── NotificationProducerService.java
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Manual Testing (Mock Mode)

By default, mock mode is enabled. Check console logs for simulated email deliveries:

```
📧 EMAIL NOTIFICATION (MOCK MODE)
To: user@example.com
Subject: Welcome
Content: Hello John, Welcome to MyApp!
```

## 📝 Kafka Topics

- `notifications-email` - Main email queue
- `notifications-email-retry` - Retry queue
- `notifications-email-dlq` - Dead letter queue
- `notifications-email-delay` - Scheduled emails

## 🔧 Troubleshooting

**Kafka not starting?**
```bash
docker-compose down -v
docker-compose up -d
```

**Check Kafka logs:**
```bash
docker logs kafka
```

**Check application logs:**
Look for `📧`, `✅`, `❌`, `🔄`, `☠️` emojis in console output.

## 📝 License

MIT License

## 🏗️ Architecture Overview

This is a production-ready, Kafka-based notification system supporting multiple channels (Push, Email, SMS) with comprehensive features:

```
Kafka Topics (notifications-push, notifications-email, notifications-sms)
     ↓
Consumer Groups (push-worker-group, email-worker-group, sms-worker-group)
     ↓
Worker Pipeline:
  1. Consume message
  2. Schema validation
  3. Duplicate check (Redis)
  4. Delayed message handling
  5. Tenant + Global rate limiting (Redis token-bucket)
  6. Template rendering
  7. Batching (where applicable)
  8. Delivery via adapters (Twilio/SendGrid/FCM)
  9. Success → Commit offset + Metrics
 10. Transient failure → Retry topic (exponential backoff)
 11. Max retries → DLQ (Dead Letter Queue)
```

## 🚀 Features

- ✅ **Multi-Channel Support**: Push, Email, SMS
- ✅ **Redis-Based Deduplication**: Prevents duplicate message processing
- ✅ **Token Bucket Rate Limiting**: Both tenant-level and global
- ✅ **Delayed Message Delivery**: Schedule notifications for future delivery
- ✅ **Template Engine**: Dynamic content rendering with placeholders
- ✅ **Retry Mechanism**: Exponential backoff with configurable retries
- ✅ **Dead Letter Queue**: Captures failed messages after max retries
- ✅ **Prometheus Metrics**: Comprehensive monitoring
- ✅ **Mock Mode**: Test without real external services
- ✅ **Delivery Adapters**: Twilio (SMS), SendGrid (Email), FCM (Push)

## 📋 Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose (for Kafka, Redis, Prometheus)

## 🔧 Setup

### 1. Start Infrastructure Services

```bash
docker-compose up -d
```

This starts:
- **Kafka** on `localhost:9092`
- **Redis** on `localhost:6379`
- **Prometheus** on `localhost:9090`
- **Zookeeper** on `localhost:2181`

### 2. Configure Application

Edit `src/main/resources/application.properties`:

```properties
# For real Twilio SMS
twilio.account-sid=YOUR_ACCOUNT_SID
twilio.auth-token=YOUR_AUTH_TOKEN
twilio.from-number=+1234567890
twilio.mock-mode=false

# For real SendGrid Email
sendgrid.api-key=YOUR_SENDGRID_API_KEY
sendgrid.from-email=your-email@example.com
sendgrid.mock-mode=false

# For real FCM Push
fcm.mock-mode=false
```

### 3. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

## 📡 API Endpoints

### V2 API (Kafka-Based)

#### Send Generic Notification
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/send" \
  -d "channel=EMAIL" \
  -d "tenantId=tenant-123" \
  -d "recipient=user@example.com" \
  -d "subject=Welcome" \
  -d "templateId=welcome-email" \
  -d "templateParams[name]=John" \
  -d "templateParams[appName]=MyApp"
```

#### Send Simple Notification
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/send-simple" \
  -d "channel=SMS" \
  -d "recipient=+1234567890" \
  -d "message=Hello from notification system!"
```

#### Send Email
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/email" \
  -d "recipient=user@example.com" \
  -d "subject=Your OTP" \
  -d "templateId=otp-sms" \
  -d "params[otp]=123456" \
  -d "params[validity]=5"
```

#### Send SMS
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/sms" \
  -d "recipient=+1234567890" \
  -d "templateId=otp-sms" \
  -d "params[otp]=654321" \
  -d "params[validity]=10"
```

#### Send Push Notification
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/push" \
  -d "recipient=DEVICE_TOKEN_HERE" \
  -d "subject=Order Update" \
  -d "templateId=order-push" \
  -d "params[orderId]=12345" \
  -d "params[status]=shipped" \
  -d "params[trackingUrl]=https://track.example.com/12345"
```

#### Scheduled Notification
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/send" \
  -d "channel=EMAIL" \
  -d "tenantId=tenant-123" \
  -d "recipient=user@example.com" \
  -d "subject=Reminder" \
  -d "templateId=notification-generic" \
  -d "templateParams[message]=Meeting in 1 hour" \
  -d "sendAt=2026-02-07T10:00:00"
```

### Health & Metrics

```bash
# Health Check
curl http://localhost:8080/api/notifications/health

# Prometheus Metrics
curl http://localhost:8080/actuator/prometheus

# All Actuator Endpoints
curl http://localhost:8080/actuator
```

## 📊 Monitoring

### Prometheus Metrics

Access Prometheus at `http://localhost:9090`

Key metrics:
- `notification_success_total{channel="email"}` - Successful deliveries
- `notification_failure_total{channel="sms"}` - Failed deliveries
- `notification_rate_limited_total` - Rate-limited requests
- `notification_duplicate_total` - Duplicate messages
- `notification_processing_time_seconds` - Processing time

### Kafka Topics

Monitor topics:
```bash
# List topics
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

# Consume from DLQ
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic notifications-email-dlq \
  --from-beginning
```

### Redis Monitoring

```bash
# Connect to Redis
docker exec -it redis redis-cli

# Check deduplication keys
KEYS dedupe:*

# Check rate limit keys
KEYS rate:*
```

## 🎨 Templates

Built-in templates:
- `welcome-email`: "Hello {{name}}, Welcome to {{appName}}!"
- `otp-sms`: "Your OTP is {{otp}}. Valid for {{validity}} minutes."
- `order-push`: "Order {{orderId}} has been {{status}}. Track it here: {{trackingUrl}}"
- `reset-password`: "Hi {{name}}, Click here to reset your password: {{resetUrl}}"
- `notification-generic`: "{{message}}"

Add custom templates in `TemplateService.java`.

## ⚙️ Configuration

### Rate Limiting

```properties
# Global limits (per channel)
rate-limit.global.capacity=1000
rate-limit.global.refill-rate=100

# Per-tenant limits
rate-limit.tenant.capacity=100
rate-limit.tenant.refill-rate=10
```

### Retry Configuration

```properties
notification.max-retries=3
notification.retry-backoff-ms=5000
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Manual Testing with Mock Mode

All delivery adapters run in mock mode by default. Check console logs for simulated deliveries.

## 🏗️ Project Structure

```
src/main/java/com/example/notification/
├── adapter/              # Delivery adapters (Twilio, SendGrid, FCM)
├── config/               # Kafka, Redis, Spring configuration
├── consumer/             # Kafka consumers for each channel
├── controller/           # REST API controllers
├── model/                # Domain models and DTOs
└── service/              # Business logic services
    ├── DeduplicationService.java
    ├── RateLimitService.java
    ├── TemplateService.java
    └── NotificationProducerService.java
```

## 🔒 Production Considerations

1. **Security**:
   - Use Spring Security for API authentication
   - Encrypt sensitive configuration with Spring Cloud Config
   - Use Kafka ACLs for topic access control

2. **Scalability**:
   - Increase Kafka partitions for higher throughput
   - Scale consumer instances horizontally
   - Use Redis Cluster for high availability

3. **Reliability**:
   - Monitor DLQ topics and set up alerts
   - Implement circuit breakers for external services
   - Use Kafka transactions for exactly-once semantics

4. **Observability**:
   - Set up Grafana dashboards for Prometheus metrics
   - Add distributed tracing with Zipkin/Jaeger
   - Implement structured logging with ELK stack

## 📝 License

MIT License

## 👤 Author

Notification System Team

The server will start on http://localhost:8080

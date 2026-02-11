# API Examples - Email Notification System

## 1. Send Simple Email
```bash
curl -X POST "http://localhost:8080/api/notifications/send-email-simple" \
  -d "recipient=fadynaeem215@gmail.com" \
  -d "message=Hello from Kafka email system!"
```

## 2. Send Welcome Email with Template
```bash
curl -X POST "http://localhost:8080/api/notifications/send-email" \
  -d "recipient=fadynaeem215@gmail.com" \
  -d "subject=Welcome to Our Platform" \
  -d "templateId=welcome-email" \
  -d "templateParams[name]=Fady" \
  -d "templateParams[appName]=EmailHub"
```

## 3. Send OTP Email
```bash
curl -X POST "http://localhost:8080/api/notifications/send-email" \
  -d "recipient=fadynaeem215@gmail.com" \
  -d "subject=Your OTP Code" \
  -d "templateId=otp-sms" \
  -d "templateParams[otp]=987654" \
  -d "templateParams[validity]=10"
```

## 4. Send Password Reset Email
```bash
curl -X POST "http://localhost:8080/api/notifications/send-email" \
  -d "recipient=fadynaeem215@gmail.com" \
  -d "subject=Reset Your Password" \
  -d "templateId=reset-password" \
  -d "templateParams[name]=Fady" \
  -d "templateParams[resetUrl]=https://example.com/reset?token=abc123"
```

## 5. Schedule Future Email
```bash
curl -X POST "http://localhost:8080/api/notifications/send-email" \
  -d "recipient=fadynaeem215@gmail.com" \
  -d "subject=Meeting Reminder" \
  -d "templateId=notification-generic" \
  -d "templateParams[message]=Your meeting starts in 1 hour" \
  -d "sendAt=2026-02-07T14:00:00"
```

## 6. Check System Health
```bash
curl http://localhost:8080/api/notifications/health
```

## 7. Legacy API (v1) - Direct Send
```bash
curl -X POST "http://localhost:8080/api/notifications/send-to-fady?message=Testing%20legacy%20API"
```

## 8. View Notification History (Legacy)
```bash
curl http://localhost:8080/api/notifications/history
```

---

## Response Format

Success response:
```json
{
  "status": "accepted",
  "dedupeId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Email queued successfully"
}
```

## Monitor Kafka Topics

### List all topics
```bash
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### View messages in main queue
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic notifications-email \
  --from-beginning
```

### View DLQ messages
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic notifications-email-dlq \
  --from-beginning
```

## 1. Send Simple Email
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/send-simple" \
  -d "channel=EMAIL" \
  -d "recipient=fadynaeem215@gmail.com" \
  -d "message=Hello from the new Kafka-based notification system!"
```

## 2. Send Welcome Email with Template
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/email" \
  -d "recipient=fadynaeem215@gmail.com" \
  -d "subject=Welcome to Our Platform" \
  -d "templateId=welcome-email" \
  -d "params[name]=Fady" \
  -d "params[appName]=NotificationHub"
```

## 3. Send OTP via SMS
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/sms" \
  -d "recipient=+201234567890" \
  -d "templateId=otp-sms" \
  -d "params[otp]=987654" \
  -d "params[validity]=10"
```

## 4. Send Push Notification
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/push" \
  -d "recipient=DEVICE_FCM_TOKEN_HERE" \
  -d "subject=New Order" \
  -d "templateId=order-push" \
  -d "params[orderId]=ORD-12345" \
  -d "params[status]=confirmed" \
  -d "params[trackingUrl]=https://example.com/track/12345"
```

## 5. Schedule Future Notification
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/send" \
  -d "channel=EMAIL" \
  -d "tenantId=acme-corp" \
  -d "recipient=fadynaeem215@gmail.com" \
  -d "subject=Reminder" \
  -d "templateId=notification-generic" \
  -d "templateParams[message]=Your meeting starts in 1 hour" \
  -d "sendAt=2026-02-07T14:00:00"
```

## 6. Multi-Tenant Notification
```bash
curl -X POST "http://localhost:8080/api/notifications/v2/send" \
  -d "channel=SMS" \
  -d "tenantId=tenant-premium-001" \
  -d "recipient=+201234567890" \
  -d "templateId=notification-generic" \
  -d "templateParams[message]=Premium feature activated!"
```

## 7. Check Health
```bash
curl http://localhost:8080/api/notifications/health
```

## 8. View Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus | grep notification
```

## 9. Legacy API (v1) - Still Works
```bash
curl -X POST "http://localhost:8080/api/notifications/send-to-fady?message=Testing%20legacy%20API"
```

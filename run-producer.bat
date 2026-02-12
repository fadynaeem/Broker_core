@echo off
REM Start the Producer Service
echo.
echo ========================================
echo Starting Email Notification Producer
echo ========================================
echo.
set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
set KAFKA_EMAIL_TOPIC=notifications-email

REM Run the producer
echo Starting on port 8888...
echo.
cd /d C:\tmp_broker
java -jar producer/target/producer-1.0.0.jar --server.port=8888

pause

@echo off
REM Start the Payment Service
echo.
echo ========================================
echo Starting Payment Producer Service
echo ========================================
echo.
set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
set KAFKA_PAYMENT_TOPIC=payment-events

REM Run the payment producer
echo Starting on port 9090...
echo.
cd /d C:\tmp_broker
java -jar payment/target/payment-1.0.0.jar --server.port=9090

pause

@echo off
echo ========================================
echo Starting Email Notification System
echo ========================================
echo.

echo Starting Kafka and Zookeeper...
docker-compose up -d

echo.
echo Waiting for services to start (20 seconds)...
timeout /t 20 /nobreak

echo.
echo ========================================
echo Infrastructure Status:
echo ========================================
docker-compose ps

echo.
echo ========================================
echo Services are ready!
echo ========================================
echo - Kafka: localhost:9092
echo - Zookeeper: localhost:2181
echo.
echo You can now start the application with:
echo   mvn spring-boot:run
echo.
echo ========================================

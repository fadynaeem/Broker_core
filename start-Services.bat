@echo off
echo Starting BrokerCore services...
echo.

echo Starting Kafka and Zookeeper...
docker-compose up -d

echo.
echo Waiting for services to start (20 seconds)...
timeout /t 20 /nobreak

echo.
echo Infrastructure Status:
docker-compose ps

echo.
echo Services are ready!
echo - Kafka: localhost:9092
echo - Zookeeper: localhost:2181

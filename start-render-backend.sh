#!/bin/bash
set -e

PORT="${PORT:-8080}"
echo "Configuring Nginx on port $PORT..."
sed "s/PORT_PLACEHOLDER/$PORT/g" /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

JAVA_OPTS="-Xmx64m -Xms32m -XX:+UseSerialGC -Xss256k -XX:TieredStopAtLevel=1"

echo "Starting Spring Boot microservices with Render low-memory settings..."
mkdir -p /app/data

java $JAVA_OPTS -jar /app/auth-service.jar > /app/data/auth.log 2>&1 &
java $JAVA_OPTS -jar /app/user-service.jar > /app/data/user.log 2>&1 &
java $JAVA_OPTS -jar /app/product-service.jar > /app/data/product.log 2>&1 &
java $JAVA_OPTS -jar /app/cart-service.jar > /app/data/cart.log 2>&1 &
java $JAVA_OPTS -jar /app/order-service.jar > /app/data/order.log 2>&1 &
java $JAVA_OPTS -jar /app/notification-service.jar > /app/data/notification.log 2>&1 &

echo "Starting Nginx reverse proxy..."
exec nginx -g "daemon off;"

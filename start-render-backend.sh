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

# Keepalive & Frontend sync ping loop (every 10 minutes)
# Keeps Render alive and pings Netlify frontend to ensure both are in sync
FRONTEND_URL="${FRONTEND_URL:-https://simple-marketplace-e-com.netlify.app}"
(
  while true; do
    sleep 600
    echo "[keep-alive] Pinging frontend: $FRONTEND_URL"
    curl -s -m 10 "$FRONTEND_URL" > /dev/null || true
  done
) &

echo "Starting Nginx reverse proxy..."
exec nginx -g "daemon off;"
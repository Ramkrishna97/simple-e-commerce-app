#!/bin/bash
set -Eeuo pipefail

PORT="${PORT:-8080}"
SERVICE_PIDS=()

cleanup() {
    local exit_code=$?
    trap - EXIT INT TERM
    echo "Stopping marketplace processes..."
    for pid in "${SERVICE_PIDS[@]:-}"; do
        kill "$pid" 2>/dev/null || true
    done
    wait 2>/dev/null || true
    exit "$exit_code"
}
trap cleanup EXIT INT TERM

echo "Configuring Nginx on port $PORT..."
sed "s/PORT_PLACEHOLDER/$PORT/g" /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

# Render runs one production JVM instead of six service JVMs. This leaves room
# for class metadata and native allocations while keeping heap below the 512 MB
# container limit.
JAVA_OPTS=(
    -Xms32m
    -Xmx256m
    -Xss256k
    -XX:+UseSerialGC
    -XX:TieredStopAtLevel=1
    -XX:ReservedCodeCacheSize=32m
    -XX:MaxDirectMemorySize=16m
    -XX:+ExitOnOutOfMemoryError
    -XX:ActiveProcessorCount=1
    -Djava.awt.headless=true
    -Dspring.jmx.enabled=false
    -Dserver.tomcat.threads.max=16
    -Dserver.tomcat.threads.min-spare=1
    -Dspring.datasource.hikari.maximum-pool-size=4
    -Dspring.datasource.hikari.minimum-idle=0
)

mkdir -p /app/data

echo "Starting Nginx reverse proxy..."
nginx -g "daemon off;" &
SERVICE_PIDS+=("$!")

echo "Starting unified Spring Boot marketplace service (heap: 256 MB)..."
java "${JAVA_OPTS[@]}" -jar /app/marketplace-service.jar &
MARKETPLACE_PID=$!
SERVICE_PIDS+=("$MARKETPLACE_PID")

for attempt in $(seq 1 120); do
    if ! kill -0 "$MARKETPLACE_PID" 2>/dev/null; then
        wait "$MARKETPLACE_PID" || true
        echo "ERROR: Marketplace service exited before becoming ready."
        exit 1
    fi
    if curl --fail --silent --output /dev/null --max-time 2 \
        "http://127.0.0.1:8080/api/products?page=0"; then
        echo "Marketplace API is ready."
        break
    fi
    if [ "$attempt" -eq 120 ]; then
        echo "ERROR: Marketplace API did not become ready within 120 seconds."
        exit 1
    fi
    sleep 1
done

# Fail the container if Nginx or the JVM exits instead of leaving Nginx alive to
# serve misleading 502 responses. Render will restart it and log the failure.
set +e
wait -n
exit_code=$?
set -e
echo "ERROR: A marketplace process exited with status $exit_code."
exit "$exit_code"

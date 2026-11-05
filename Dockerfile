# Stage 1: Build Java Spring Boot JARs using official Gradle image
FROM gradle:8.10.2-jdk17-alpine AS builder
WORKDIR /build

# Copy build configuration files
COPY build.gradle settings.gradle ./
COPY auth-service/build.gradle auth-service/
COPY user-service/build.gradle user-service/
COPY product-service/build.gradle product-service/
COPY cart-service/build.gradle cart-service/
COPY order-service/build.gradle order-service/
COPY notification-service/build.gradle notification-service/
COPY marketplace-service/build.gradle marketplace-service/

# Cache dependencies
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY auth-service/src auth-service/src
COPY user-service/src user-service/src
COPY product-service/src product-service/src
COPY cart-service/src cart-service/src
COPY order-service/src order-service/src
COPY notification-service/src notification-service/src
COPY marketplace-service/src marketplace-service/src

# Build the single-JVM production bundle. Individual service JARs remain
# available for local development but are not deployed to the 512 MB instance.
RUN gradle :marketplace-service:bootJar --no-daemon -x test

# Stage 2: Low-Memory Production Image for Render Free Tier
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache bash nginx curl

WORKDIR /app

COPY --from=builder /build/marketplace-service/build/libs/*.jar /app/marketplace-service.jar

COPY nginx.conf.template /etc/nginx/nginx.conf.template
COPY start-render-backend.sh /app/start-render-backend.sh

RUN chmod +x /app/start-render-backend.sh

EXPOSE 8080

CMD ["/app/start-render-backend.sh"]

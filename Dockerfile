# Stage 1: Build Java Spring Boot JARs
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build

COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY auth-service/build.gradle auth-service/
COPY user-service/build.gradle user-service/
COPY product-service/build.gradle product-service/
COPY cart-service/build.gradle cart-service/
COPY order-service/build.gradle order-service/
COPY notification-service/build.gradle notification-service/

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY auth-service/src auth-service/src
COPY user-service/src user-service/src
COPY product-service/src product-service/src
COPY cart-service/src cart-service/src
COPY order-service/src order-service/src
COPY notification-service/src notification-service/src

RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Low-Memory Production Image for Render Free Tier
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache bash nginx

WORKDIR /app

COPY --from=builder /build/auth-service/build/libs/*.jar /app/auth-service.jar
COPY --from=builder /build/user-service/build/libs/*.jar /app/user-service.jar
COPY --from=builder /build/product-service/build/libs/*.jar /app/product-service.jar
COPY --from=builder /build/cart-service/build/libs/*.jar /app/cart-service.jar
COPY --from=builder /build/order-service/build/libs/*.jar /app/order-service.jar
COPY --from=builder /build/notification-service/build/libs/*.jar /app/notification-service.jar

COPY nginx.conf.template /etc/nginx/nginx.conf.template
COPY start-render-backend.sh /app/start-render-backend.sh

RUN chmod +x /app/start-render-backend.sh

EXPOSE 8080

CMD ["/app/start-render-backend.sh"]

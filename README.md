# Simple E-Marketplace

Java 17 / Spring Boot 3.5.6 / Gradle 8.10.2 multi-service starter.

| Service | Port | Responsibility |
| --- | --- | --- |
| auth-service | 8081 | Credential registration, login, JWT creation, password reset/change |
| user-service | 8082 | User profile, mobile, image, and address management |
| product-service | 8083 | Seller catalog, categories, and paginated product management |
| cart-service | 8084 | Persistent user carts |
| order-service | 8085 | Authenticated checkout, stock reservation, and order history |
| notification-service | 8086 | Embedded Kafka event delivery and persistent buyer/seller notifications |

Run a service with `./gradlew :auth-service:bootRun` (PowerShell: `./gradlew.bat :auth-service:bootRun`). Use Java 17 to build or run it.

Double-click `start-all.bat` to build and start all services and the Angular dashboard, which opens at `http://localhost:4200`. Kafka runs embedded inside the notification service from Gradle dependencies; Docker is not required. The launcher waits for every backend service before opening the browser and writes diagnostics to `data/logs/`. Double-click `stop-all.bat` to stop exactly the processes started by that launcher. Product pages contain 20 products; the dashboard supports tile/list display and preserves anonymous carts through login. Each service stores its local data in a persistent H2 file under that service's `data/` directory.

The single administrator account is `admin` / `qwertyuiop`. Registration exposes buyer and seller roles only.

The auth service returns a temporary password only to simplify local development. Replace that response with an email provider before production. In production, place an API gateway in front of the user and product services and validate the JWT issued by auth-service before allowing account or seller changes.

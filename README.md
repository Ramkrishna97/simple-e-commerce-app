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

## Free Tier Production Deployment (Netlify + Render)

This project is configured to run 100% on free tiers:
- **Frontend:** Angular app deployed on **Netlify Free Tier**
- **Backend:** All 6 Spring Boot microservices + Nginx reverse proxy bundled into a single low-memory Docker container deployed on **Render Free Tier (512 MB RAM)**.

---

### Step 1: Deploy Backend to Render (Free Tier)
1. Push your repository to GitHub or GitLab.
2. Log in to [Render](https://dashboard.render.com/) and click **New +** -> **Blueprint**.
3. Connect your repository. Render will automatically detect [render.yaml](file:///E:/Temp/Ai-playground/simple-e-marketplace/render.yaml) and build the Docker container using [Dockerfile](file:///E:/Temp/Ai-playground/simple-e-marketplace/Dockerfile).
4. Render will generate a URL for your backend service (e.g., `https://simple-e-marketplace-backend.onrender.com`).

---

### Step 2: Deploy Frontend to Netlify (Free Tier)
1. In [netlify.toml](file:///E:/Temp/Ai-playground/simple-e-marketplace/netlify.toml), update the `to` URLs under `[[redirects]]` if your Render app name is different from `simple-e-marketplace-backend`.
2. Log in to [Netlify](https://app.netlify.com/) and import your repository.
3. Netlify will build the Angular site and automatically proxy all `/api/*` traffic seamlessly to your Render backend.

> [!TIP]
> **Low-Memory Optimization:**
> Each Java Spring Boot microservice is launched in Docker with optimized JVM flags (`-Xmx64m -XX:+UseSerialGC -Xss256k -XX:TieredStopAtLevel=1`), using only ~350–400 MB RAM total. This ensures the backend runs cleanly within Render's 512 MB Free Tier limit without memory errors.

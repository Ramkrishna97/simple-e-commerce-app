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

## Deploying the Frontend to Netlify

This project includes a [netlify.toml](file:///E:/Temp/Ai-playground/simple-e-marketplace/netlify.toml) configuration file in the root directory for deploying the Angular frontend to Netlify.

### Netlify Configuration (`netlify.toml`)
- **Base directory:** `frontend`
- **Build command:** `npm run build`
- **Publish directory:** `dist/frontend/browser`
- **Redirects:** Single Page Application (SPA) catch-all redirect configured to `index.html` (status 200).

### Deployment Steps:
1. Push your repository to GitHub, GitLab, or Bitbucket.
2. Log in to [Netlify](https://app.netlify.com/) and click **Add new site** -> **Import an existing project**.
3. Connect your Git provider and select this repository.
4. Netlify will automatically detect the `netlify.toml` file and pre-fill the build settings (`base`, `command`, `publish`).
5. Click **Deploy site**.

> [!NOTE]
> **Netlify Free Tier Limitation & Backend Hosting:**
> Netlify Free Tier hosts static frontends and serverless Node functions only — it cannot run Java/Spring Boot JARs or Gradle background processes.
> 
> The Angular app dynamically resolves API URLs:
> - **Local Development:** Routes to `http://localhost:808x` via `proxy.conf.json`.
> - **Deployed Host (Netlify):** Routes requests relatively via `/api/*`.
> 
> To connect your Netlify frontend to live backends for free:
> 1. Host your Spring Boot JARs or Docker containers on a free backend platform (such as [Render Free Tier](https://render.com/), [Railway](https://railway.app/), or [Koyeb](https://www.koyeb.com/)).
> 2. Uncomment and update the `[[redirects]]` section in `netlify.toml` to proxy `/api/*` routes to your free backend URL.

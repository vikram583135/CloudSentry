<p align="center">
  <h1 align="center">☁️ CloudSentry</h1>
  <p align="center">
    <strong>AI-Powered Smart DevOps Incident & Cost Optimization Platform</strong>
  </p>
  <p align="center">
    A production-grade observability and cost optimization platform combining features of <b>Datadog + AWS Cost Explorer + PagerDuty</b> with AI-powered insights.
  </p>
  <p align="center">
    <a href="https://github.com/vikram583135/CloudSentry">
      <img src="https://img.shields.io/badge/GitHub-CloudSentry-181717?logo=github" alt="GitHub Repo" />
    </a>
    <img src="https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk&logoColor=white" alt="Java" />
    <img src="https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL" />
    <img src="https://img.shields.io/badge/Apache%20Kafka-7.5-231F20?logo=apachekafka&logoColor=white" alt="Kafka" />
    <img src="https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white" alt="Redis" />
    <img src="https://img.shields.io/badge/License-MIT-yellow?logo=opensourceinitiative&logoColor=white" alt="License" />
  </p>
</p>

---

## 📖 Table of Contents

- [Features](#-features)
- [Architecture](#️-architecture)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [Deploying to Cloudflare](#-deploying-to-cloudflare-pages--edge)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Configuration](#-configuration)
- [Testing](#-testing)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Features

### Core Capabilities

| Module | Description |
|--------|-------------|
| **Metrics Ingestion** | Real-time tracking of CPU, Memory, Latency, and Error rates via Kafka streams |
| **Anomaly Detection** | Statistical + Rule-based anomaly detection engine with predictive analysis |
| **Incident Management** | Automatic incident creation with intelligent severity calculation and lifecycle tracking |
| **AI Root Cause Analysis** | Intelligent RCA suggestions using a configurable rule engine and optional LLM integration |
| **Cloud Cost Analyzer** | AWS cost tracking, resource utilization monitoring, and optimization recommendations |
| **Notification Engine** | Multi-channel alerting (Email, Slack, Webhook) with deduplication and escalation policies |
| **Role-Based Dashboards** | Aggregated views tailored for Admin, SRE, Developer, and Manager personas |

### Dashboards (Role-Based Access)

| Role | Dashboard Highlights |
|------|----------------------|
| **Admin** | System-wide metrics, user management, platform configuration |
| **SRE** | Active incidents, on-call management, alert tuning, anomaly trends |
| **Developer** | Application metrics, deployment correlation, error logs, RCA insights |
| **Manager** | Cost analytics, SLA reports, incident trends, budget forecasts |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     API Gateway (Spring Cloud)                  │
└─────────────────────────────────────────────────────────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    ▼             ▼             ▼
              ┌──────────┐ ┌──────────┐ ┌──────────────┐
              │   Auth   │ │ Metrics  │ │   Cost       │
              │ Service  │ │ Service  │ │  Analyzer    │
              └──────────┘ └────┬─────┘ └──────────────┘
                                │
                         ┌──────▼──────┐
                         │  Apache     │
                         │  Kafka      │
                         └──────┬──────┘
                                │
                    ┌───────────┼───────────┐
                    ▼           ▼           ▼
              ┌──────────┐ ┌──────────┐ ┌──────────────┐
              │ Anomaly  │ │ Incident │ │ Notification │
              │ Detector │ │ Manager  │ │   Engine     │
              └──────────┘ └────┬─────┘ └──────────────┘
                                │
                    ┌───────────┼───────────┐
                    ▼                       ▼
              ┌─────────────┐       ┌─────────────┐
              │  Root Cause │       │  Dashboard  │
              │  Analysis   │       │   Service   │
              └─────────────┘       └─────────────┘
```

---

## 🧰 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2, Spring Cloud 2023.0 |
| **Security** | Spring Security, JWT (jjwt 0.12.3) |
| **Database** | PostgreSQL 15, Spring Data JPA |
| **Cache** | Redis 7 |
| **Messaging** | Apache Kafka (Spring Kafka) |
| **Cloud** | AWS SDK v2 (Cost Explorer, EC2) |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Monitoring** | Micrometer + Prometheus |
| **Build** | Maven, Docker Compose |
| **Testing** | JUnit 5, Testcontainers, Spring Security Test |

---

## 🚀 Getting Started

### Prerequisites

- **Java** 17+
- **Maven** 3.8+
- **Docker** & Docker Compose
- **Node.js** 18+ *(optional — for frontend)*

### 1. Clone the Repository

```bash
git clone https://github.com/vikram583135/CloudSentry.git
cd CloudSentry
```

### 2. Configure Environment

```bash
# Copy the environment template
cp .env.example .env

# Edit .env with your configuration (database, Redis, Kafka, JWT secret, etc.)
```

### 3. Start Infrastructure Services

```bash
# Start PostgreSQL, Redis, and Kafka using Docker Compose
docker-compose -f docker/docker-compose.yml up -d

# Verify all services are healthy
docker-compose -f docker/docker-compose.yml ps
```

### 4. Run the Application

```bash
# Development mode
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or build and run the JAR
./mvnw clean package -DskipTests
java -jar target/ai-devops-platform-1.0.0-SNAPSHOT.jar
```

### 5. Access the Platform

| Service | URL |
|---------|-----|
| **Web Console (UI)** | `http://localhost:8080/` |
| **REST API** | `http://localhost:8080` |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` |
| **Kafka UI** | `http://localhost:8090` *(dev only)* |

---

## ⚡ Deploying to Cloudflare (Pages & Edge)

CloudSentry's Observability Web Console and Edge Functions can be deployed globally to **Cloudflare Pages** in under 60 seconds with zero server management.

### Method 1: 1-Click CLI Deployment (Wrangler)

```powershell
# On Windows PowerShell
.\deploy-cloudflare.ps1
```

```bash
# On Linux / macOS
chmod +x deploy-cloudflare.sh
./deploy-cloudflare.sh
```

Or using `npx` directly:
```bash
# Authenticate (first time only)
npx wrangler login

# Deploy static web console + edge functions
npx wrangler pages deploy src/main/resources/static --project-name cloudsentry
```

### Method 2: Automated GitHub Actions CI/CD

1. Go to your repository **Settings > Secrets and variables > Actions**.
2. Add the following repository secrets:
   - `CLOUDFLARE_API_TOKEN`: Created in your Cloudflare dashboard with *Cloudflare Pages Edit* permissions.
   - `CLOUDFLARE_ACCOUNT_ID`: Found in your Cloudflare dashboard URL or Workers & Pages overview.
3. Every push to `main` or `master` will automatically build and deploy via [`.github/workflows/deploy-cloudflare.yml`](.github/workflows/deploy-cloudflare.yml).

### Method 3: Cloudflare Dashboard (Git Integration)

1. Open [Cloudflare Pages Dashboard](https://dash.cloudflare.com/?to=/:account/pages).
2. Click **Create an application** > **Pages** > **Connect to Git**.
3. Select this repository and use the following build settings:
   - **Framework preset**: None
   - **Build command**: `./mvnw clean package -DskipTests` *(or leave blank)*
   - **Build output directory**: `src/main/resources/static`
4. *(Optional)* Under **Settings > Environment variables**, add `API_BACKEND_URL` pointing to your deployed Spring Boot API cluster (e.g., `https://api.cloudsentry.dev`) to enable the edge proxy.

---

## 📚 API Documentation

All API endpoints are prefixed with `/api/v1`. Full interactive docs are available at **Swagger UI** when the application is running.

### Authentication

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"password123"}'

# Login and receive JWT tokens
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'
```

### Authenticated Requests

```bash
# Include the JWT token in the Authorization header
curl http://localhost:8080/api/v1/metrics \
  -H "Authorization: Bearer <your-access-token>"
```

### Key API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/auth/register` | Register a new user |
| `POST` | `/api/v1/auth/login` | Authenticate and get JWT tokens |
| `GET` | `/api/v1/metrics` | Retrieve system metrics |
| `POST` | `/api/v1/metrics` | Ingest new metrics data |
| `GET` | `/api/v1/incidents` | List all incidents |
| `POST` | `/api/v1/incidents` | Create/manage incidents |
| `GET` | `/api/v1/cost/records` | Fetch cloud cost records |
| `GET` | `/api/v1/cost/recommendations` | Get cost optimization recommendations |
| `POST` | `/api/v1/rca/analyze` | Trigger root cause analysis |
| `GET` | `/api/v1/dashboard/admin` | Admin dashboard data |
| `GET` | `/api/v1/dashboard/sre` | SRE dashboard data |
| `GET` | `/api/v1/dashboard/developer` | Developer dashboard data |
| `GET` | `/api/v1/dashboard/manager` | Manager dashboard data |

---

## 📁 Project Structure

```
CloudSentry/
├── .env.example                 # Environment variable template
├── .gitignore
├── pom.xml                      # Maven build configuration
├── mvnw / mvnw.cmd              # Maven wrapper scripts
├── docker/                      # Docker Compose files for infrastructure
├── src/
│   └── main/
│       ├── java/com/devops/platform/
│       │   ├── DevOpsPlatformApplication.java   # Main entry point
│       │   │
│       │   ├── config/          # Global configuration
│       │   │   ├── SecurityConfig.java
│       │   │   ├── KafkaConfig.java
│       │   │   ├── RedisConfig.java
│       │   │   └── WebClientConfig.java
│       │   │
│       │   ├── auth/            # 🔐 Authentication & Authorization (JWT)
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── model/
│       │   │   ├── repository/
│       │   │   ├── security/
│       │   │   └── dto/
│       │   │
│       │   ├── metrics/         # 📊 Metrics Ingestion Module
│       │   ├── analyzer/        # 🔍 Anomaly Detection Engine
│       │   ├── incident/        # 🚨 Incident Management
│       │   ├── rca/             # 🧠 AI Root Cause Analysis
│       │   ├── cost/            # 💰 Cloud Cost Analyzer
│       │   ├── notification/    # 🔔 Notification Engine
│       │   ├── dashboard/       # 📈 Role-Based Dashboards
│       │   └── common/          # 🛠️ Shared DTOs & Exception Handling
│       │       ├── dto/
│       │       └── exception/
│       │
│       └── resources/
│           ├── application.yml          # Default configuration
│           ├── application-dev.yml      # Dev profile
│           └── application-prod.yml     # Production profile
└── target/                      # Build output (gitignored)
```

---

## 🔧 Configuration

### Environment Variables

Copy `.env.example` to `.env` and configure the following:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `devops_platform` |
| `DB_USERNAME` | Database user | `devops_user` |
| `DB_PASSWORD` | Database password | `devops_password` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | `redis_password` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka servers | `localhost:9092` |
| `JWT_SECRET` | JWT signing key *(min 32 chars)* | *(required)* |
| `AWS_ACCESS_KEY_ID` | AWS access key | *(optional)* |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | *(optional)* |
| `OPENAI_API_KEY` | OpenAI API key for AI RCA | *(optional)* |
| `MAIL_HOST` | SMTP server host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP server port | `587` |
| `SLACK_WEBHOOK_URL` | Slack webhook URL | *(optional)* |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |

### Application Profiles

| Profile | Purpose |
|---------|---------|
| `dev` | Local development with relaxed security and verbose logging |
| `prod` | Production-ready configuration with strict security |

---

## 🧪 Testing

```bash
# Run unit tests
./mvnw test

# Run integration tests (requires Docker for Testcontainers)
./mvnw verify -Pintegration-test

# Generate test coverage report
./mvnw jacoco:report
# Report available at: target/site/jacoco/index.html
```

---

## 🤝 Contributing

**CloudSentry is open source and we love contributions!** Whether you're fixing a bug, improving documentation, or building an entirely new feature — your help is valued and appreciated. 🎉

### 🌟 Areas Where You Can Help

We're actively looking for contributors in the following areas:

| Area | Examples |
|------|----------|
| 🔍 **Anomaly Detection** | New detection algorithms, ML model integration, threshold tuning |
| ☁️ **Cloud Providers** | Add support for Azure Cost Management, GCP Billing, etc. |
| 🖥️ **Frontend Dashboard** | Build a React/Angular frontend for the dashboard APIs |
| 📖 **Documentation** | API guides, architecture deep-dives, deployment tutorials |
| 🧪 **Testing** | Unit tests, integration tests, performance benchmarks |
| 🐛 **Bug Fixes** | Check the [Issues](https://github.com/vikram583135/CloudSentry/issues) tab for open bugs |

### 📝 How to Contribute

1. **Fork** the repository — [CloudSentry on GitHub](https://github.com/vikram583135/CloudSentry)
2. **Create** a feature branch
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit** your changes using [Conventional Commits](https://www.conventionalcommits.org/)
   ```bash
   git commit -m "feat: add amazing feature"
   ```
4. **Push** to your branch
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open** a Pull Request with a clear description of your changes

### 💬 Get in Touch

- 🐛 **Found a bug?** [Open an issue](https://github.com/vikram583135/CloudSentry/issues/new)
- 💡 **Have an idea?** Start a [discussion](https://github.com/vikram583135/CloudSentry/discussions) or open a feature request
- ⭐ **Like the project?** Give it a star on [GitHub](https://github.com/vikram583135/CloudSentry) — it helps others discover CloudSentry!

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) — for the robust application framework
- [Apache Kafka](https://kafka.apache.org/) — for reliable real-time message streaming
- [PostgreSQL](https://www.postgresql.org/) — for the powerful relational database
- [Redis](https://redis.io/) — for high-performance caching
- [AWS SDK](https://aws.amazon.com/sdk-for-java/) — for cloud cost analysis integration

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/vikram583135">vikram583135</a>
</p>

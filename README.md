# AI-Powered Smart DevOps Incident & Cost Optimization Platform

> A production-grade observability and cost optimization platform combining features of **Datadog + AWS Cost Explorer + PagerDuty** with AI-powered insights.

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Kafka](https://img.shields.io/badge/Kafka-7.5-black)
![Redis](https://img.shields.io/badge/Redis-7-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 🎯 Features

### Core Capabilities
- **Real-time Metrics Ingestion** - CPU, Memory, Latency, Error rate tracking
- **Anomaly Detection Engine** - Statistical + Rule-based detection with prediction
- **Incident Auto-Creation** - Automatic incident creation with severity calculation
- **AI Root Cause Assistant** - Intelligent suggestions using rules + optional LLM
- **Cloud Cost Analyzer** - AWS cost tracking and optimization suggestions
- **Smart Alerting** - Deduplication, escalation, and multi-channel notifications

### Dashboards (Role-Based)
| Role | Access |
|------|--------|
| **Admin** | All metrics, user management, system config |
| **SRE** | Incident management, on-call, alert tuning |
| **Developer** | App metrics, deployment correlation, error logs |
| **Manager** | Cost analytics, SLA reports, incident trends |

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (Spring Cloud)                  │
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
                         │    Kafka    │
                         └──────┬──────┘
                                │
                    ┌───────────┼───────────┐
                    ▼           ▼           ▼
              ┌──────────┐ ┌──────────┐ ┌──────────────┐
              │ Analyzer │ │ Incident │ │ Notification │
              │ Service  │ │ Service  │ │   Engine     │
              └──────────┘ └────┬─────┘ └──────────────┘
                                │
                         ┌──────▼──────┐
                         │  Root Cause │
                         │  Assistant  │
                         └─────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+ (for frontend)

### 1. Clone and Setup
```bash
git clone <repository-url>
cd ai-devops-platform

# Copy environment template
cp .env.example .env
# Edit .env with your configuration
```

### 2. Start Infrastructure
```bash
# Start PostgreSQL, Redis, Kafka
docker-compose -f docker/docker-compose.yml up -d

# Wait for services to be healthy
docker-compose -f docker/docker-compose.yml ps
```

### 3. Run the Application
```bash
# Development mode
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or build and run
./mvnw clean package -DskipTests
java -jar target/ai-devops-platform-1.0.0-SNAPSHOT.jar
```

### 4. Access
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Kafka UI**: http://localhost:8090 (development only)

## 📚 API Documentation

### Authentication
```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'
```

### Using JWT Token
```bash
# Include token in requests
curl http://localhost:8080/api/v1/metrics \
  -H "Authorization: Bearer <your-access-token>"
```

## 🧪 Testing

```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker)
./mvnw verify -Pintegration-test

# Test coverage
./mvnw jacoco:report
```

## 📁 Project Structure

```
src/main/java/com/devops/platform/
├── DevOpsPlatformApplication.java
├── config/                 # Configuration classes
│   ├── SecurityConfig.java
│   ├── KafkaConfig.java
│   ├── RedisConfig.java
│   └── WebClientConfig.java
├── auth/                   # Authentication module
│   ├── controller/
│   ├── service/
│   ├── model/
│   ├── repository/
│   ├── security/
│   └── dto/
├── metrics/                # Metrics ingestion module
├── analyzer/               # Anomaly detection module
├── incident/               # Incident management module
├── rootcause/              # AI root cause analysis
├── cost/                   # Cloud cost analyzer
├── notification/           # Alerting & notifications
└── common/                 # Shared utilities
    ├── dto/
    └── exception/
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | devops_platform |
| `DB_USERNAME` | Database user | devops_user |
| `DB_PASSWORD` | Database password | devops_password |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka servers | localhost:9092 |
| `JWT_SECRET` | JWT signing key | (required) |
| `AWS_ACCESS_KEY_ID` | AWS access key | (optional) |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | (optional) |
| `OPENAI_API_KEY` | OpenAI API key | (optional) |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Apache Kafka for reliable message streaming
- Confluent for Kafka ecosystem tools

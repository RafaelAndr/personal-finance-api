# Personal Finance API

REST API for personal finance management, designed to help users organize and track their financial activities, including accounts, incomes, expenses, and payments.

## Tech Stack

* Java 21
* Spring Boot
* Spring Security
* JWT Authentication
* Spring Data JPA
* PostgreSQL
* MapStruct
* SpringDoc OpenAPI (Swagger)
* JUnit 5
* Mockito
* Docker

## Security Features

### Authentication & Authorization

The application implements a stateless authentication mechanism using Spring Security and JWT (JSON Web Token).

Features include:

* User registration
* User authentication
* JWT generation and validation
* Stateless security configuration
* Password encryption using BCrypt
* Protected endpoints
* Role-based access preparation
* Custom authentication filter

### Authentication Flow

```text
Client
   │
   ▼
POST /auth
   │
   ▼
Validate Credentials
   │
   ▼
Generate JWT
   │
   ▼
Return Access Token
```

For protected endpoints:

```text
Client
   │
Authorization: Bearer <token>
   │
   ▼
JWT Validation Filter
   │
   ▼
Spring Security Context
   │
   ▼
Protected Resource
```

## Features

### Security

* User registration and authentication
* JWT-based authorization
* Password encryption with BCrypt
* Stateless session management
* Endpoint protection with Spring Security

### Account Management

* Create and manage financial accounts
* Retrieve account information
* Filter transactions by account

### Expense Management

* Register expenses
* Retrieve paid expenses
* Retrieve pending expenses
* Update expense status
* Delete expenses

### Income Management

* Register incomes
* Track financial entries

### Payment Management

* Register payments
* Manage payment status and history


### API Features
- RESTful architecture
- DTO mapping with MapStruct
- API documentation with Swagger/OpenAPI
- Unit and integration testing support
- Dockerized environment
## Project Structure

```text
personal-finance/
├── src/
│   ├── main/
│   │   ├── java/com/personal_finance/
│   │   │   ├── config/            # Application configurations and beans
│   │   │   ├── controller/        # REST API endpoints
│   │   │   ├── dto/               # Data Transfer Objects
│   │   │   │   ├── account/       # Account-related DTOs
│   │   │   │   ├── expense/       # Expense-related DTOs
│   │   │   │   ├── income/        # Income-related DTOs
│   │   │   │   ├── payment/       # Payment-related DTOs
│   │   │   │   └── user/          # User-related DTOs
│   │   │   ├── entity/            # JPA entities
│   │   │   │   └── enums/         # Enumerations used by entities
│   │   │   ├── exception/         # Exception handling
│   │   │   ├── mapper/            # Entity and DTO mapping (MapStruct)
│   │   │   ├── repository/        # Data access layer (JPA repositories)
│   │   │   ├── security/          # Security configurations and components
│   │   │   └── service/           # Business logic layer
│   │   └── resources/
│   │       ├── static/            # Static resources
│   │       └── templates/         # Application templates
│   └── test/
│       ├── java/com/personal_finance/
│       │   ├── controller/        # Controller tests
│       │   ├── integration/       # Integration tests
│       │   ├── repository/        # Repository tests
│       │   └── service/           # Service tests
│       └── resources/             # Test resources
```
## Installation

### Clone Repository

```bash
git clone https://github.com/your-username/personal-finance.git
cd personal-finance
```

### Configure Database

Configure the `application.yml` file:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/personal_finance
    username: postgres
    password: password
```

### Run Application

```bash
./gradlew bootRun
```

---

## Running with Docker

Start containers:

```bash
docker compose up -d
```

---

## Testing

Run all tests:

```bash
./gradlew test
```

Test coverage includes:

- Unit tests
- Integration tests
- Repository tests
- Controller tests

---

## API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

---

## API Endpoints

### Accounts

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/accounts` | Create account |
| GET | `/accounts` | Retrieve accounts |
| DELETE | `/accounts/{id}` | Delete account |

### Expenses

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/expenses` | Create expense |
| GET | `/expenses` | Retrieve expenses |
| PATCH | `/expenses/{id}` | Update expense status |
| DELETE | `/expenses/{id}` | Delete expense |

---

## Architecture

The application follows a layered architecture:

- **Controller Layer** – REST API endpoints
- **Service Layer** – Business logic
- **Repository Layer** – Data persistence
- **DTO Layer** – Data transfer objects
- **Mapper Layer** – Entity mapping using MapStruct

---

## Author

Rafael Nascimento Andrade

Backend Developer | Java | Spring Boot

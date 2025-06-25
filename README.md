# Keycloak User Management API

A **reactive**, **WebFlux**-based Spring Boot application providing CRUD operations over Keycloak users, secured with OAuth2/JWT, and documented with OpenAPI/Swagger UI.

---

## Table of Contents

- [Features](#features)  
- [Tech Stack](#tech-stack)  
- [Prerequisites](#prerequisites)  
- [Getting Started](#getting-started)  
  - [Clone & Build](#clone--build)  
  - [Configuration](#configuration)  
  - [Run with Docker Compose](#run-with-docker-compose)  
- [API Endpoints](#api-endpoints)  
- [Swagger / OpenAPI](#swagger--openapi)  
- [Keycloak Setup](#keycloak-setup)  
- [License](#license)  

---

## Features

- Reactive CRUD over Keycloak users (create, read, update, delete)  
- Token-based authentication & authorization via Keycloak  
- Role-based access control (USER, EDITOR, ADMIN / manage-users)  
- OpenAPI documentation with Swagger UI  
- Caching of admin access token for efficiency  
- Configurable via `application.yml`  

---

## Tech Stack

- **Java 21**  
- **Spring Boot 3.5**  
  - Spring WebFlux  
  - Spring Security (OAuth2 Resource Server)  
- **Reactor** (Mono, Flux)  
- **Keycloak** (v22)  
- **SpringDoc OpenAPI** (Swagger UI)  
- **Maven 3.8+**  

---

## Prerequisites

- Java 21 JDK  
- Maven 3.8+  
- Docker & Docker Compose  
- Keycloak server (can be run via Docker Compose)  

---

## Getting Started

### Clone & Build

```bash
git clone https://github.com/your-org/demo-user-management.git
cd demo-user-management
mvn clean package -DskipTests
```

### Configuration

- Edit src/main/resources/application.yml to match your Keycloak settings:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://keycloak:8080/realms/demo/protocol/openid-connect/certs

server:
  port: 8081

keycloak:
  server-url:      http://keycloak:8080
  realm:           demo
  master-realm:    master
  admin-client-id: admin-client
  admin-client-secret: admin-secret
  admin-user:      admin
  admin-password:  admin
```

### Run with Docker Compose

```yaml
version: '3.8'

services:
  keycloak:
    image: quay.io/keycloak/keycloak:22.0.4
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    command: start-dev
    ports:
      - "8080:8080"

  app:
    build: .
    depends_on:
      - keycloak
    ports:
      - "8081:8081"
    environment:
      SPRING_PROFILES_ACTIVE: default
```

```bash
docker compose up --build
```

## API Endpoints

### Public

| Method | Path         | Roles  | Description         |
| ------ | ------------ | ------ | ------------------- |
| GET    | `/api/users` | USER   | List user summaries |
| GET    | `/api/edit`  | EDITOR | Editor-only access  |
| GET    | `/api/admin` | ADMIN  | Admin-only access   |

### Admin User Management

| Method | Path                             | Roles               | Description                |
| ------ | -------------------------------- | ------------------- | -------------------------- |
| GET    | `/api/admin/users`               | ADMIN, manage-users | List all users             |
| GET    | `/api/admin/users/{userId}`      | ADMIN, manage-users | Get detailed user info     |
| POST   | `/api/admin/users`               | ADMIN, manage-users | Create a new user          |
| PUT    | `/api/admin/users/{userId}`      | ADMIN, manage-users | Update existing user       |
| DELETE | `/api/admin/users/{userId}`      | ADMIN, manage-users | Delete a user by ID        |


## Swagger / OpenAPI

After starting the app, browse:

- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)  
- **Raw OpenAPI JSON**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

To authorize Swagger, click **Authorize** and enter your JWT token:

```text
Bearer <your-jwt-token>
```

## Keycloak Setup (auto-imported)

When you bring up Keycloak via Docker Compose, the `demo` realm is automatically imported with the following configuration:

```json
{
  "realm": "demo",
  "enabled": true,

  "clients": [
    {
      "clientId": "spring-boot-app",
      "clientAuthenticatorType": "client-secret",
      "secret": "secret",
      "redirectUris": ["http://localhost:8081/*"],
      "webOrigins": ["http://localhost:8081"],
      "protocol": "openid-connect",
      "publicClient": false,
      "directAccessGrantsEnabled": true
    },
    {
      "clientId": "admin-client",
      "name": "Admin Client",
      "enabled": true,
      "clientAuthenticatorType": "client-secret",
      "secret": "admin-secret",
      "serviceAccountsEnabled": true,
      "fullScopeAllowed": true,
      "protocol": "openid-connect",
      "protocolMappers": [
        {
          "name": "client-roles → roles",
          "protocol": "openid-connect",
          "protocolMapper": "oidc-usermodel-client-role-mapper",
          "config": {
            "multivalued": "true",
            "access.token.claim": "true",
            "claim.name": "roles",
            "jsonType.label": "String"
          }
        }
      ]
    }
  ],

  "clientScopes": [
    {
      "name": "roles",
      "protocol": "openid-connect",
      "attributes": {
        "include.in.token.scope": "true",
        "display.on.consent.screen": "true"
      },
      "protocolMappers": [
        {
          "name": "realm roles",
          "protocolMapper": "oidc-usermodel-realm-role-mapper",
          "config": {
            "multivalued": "true",
            "id.token.claim": "true",
            "access.token.claim": "true",
            "claim.name": "roles",
            "jsonType.label": "String"
          }
        }
      ]
    }
  ],

  "defaultDefaultClientScopes": ["roles"],

  "roles": {
    "realm": [
      { "name": "USER",  "description": "Basic user role" },
      { "name": "EDITOR","description": "User with edit permissions" },
      { "name": "ADMIN", "description": "Full administrative rights" }
    ]
  },

  "users": [
    {
      "username": "testuser",
      "enabled": true,
      "credentials": [{ "type": "password", "value": "password" }],
      "realmRoles": ["USER","EDITOR"]
    },
    {
      "username": "admin",
      "enabled": true,
      "credentials": [{ "type": "password", "value": "admin" }],
      "realmRoles": ["ADMIN"],
      "clientRoles": {
        "realm-management": ["manage-users","view-users","query-users","impersonation"]
      }
    },
    {
      "username": "service-account-admin-client",
      "enabled": true,
      "serviceAccountClientId": "admin-client",
      "clientRoles": {
        "realm-management": [
          "manage-users","view-users","query-users","impersonation",
          "realm-admin","create-client","manage-clients"
        ]
      }
    }
  ]
}
```

## How to Obtain an Admin Access Token
- The admin-client is configured with serviceAccountsEnabled: true, so you can obtain a token via the client credentials grant:

```bash
curl -X POST 'http://localhost:8080/realms/demo/protocol/openid-connect/token' \
  -d 'grant_type=client_credentials' \
  -d 'client_id=admin-client' \
  -d 'client_secret=admin-secret'
```

# 💰 Personal Finance API

API REST para gerenciamento financeiro pessoal, desenvolvida para auxiliar usuários no controle de contas, receitas, despesas e pagamentos.

O projeto foi construído utilizando Java e Spring Boot, seguindo boas práticas de arquitetura em camadas, autenticação stateless com JWT e testes automatizados.

---

# 🚀 Tecnologias Utilizadas

### Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Spring Security
* JWT Authentication
* MapStruct

### Banco de Dados

* PostgreSQL

### Documentação

* SpringDoc OpenAPI (Swagger)

### Testes

* JUnit 5
* Mockito

### DevOps

* Docker
* Docker Compose

---

# 🔐 Segurança

A aplicação implementa autenticação e autorização utilizando Spring Security e JWT (JSON Web Token).

### Funcionalidades de Segurança

* Cadastro de usuários
* Login de usuários
* Geração de Access Token
* Geração de Refresh Token
* Renovação de Access Token
* Autenticação Stateless
* Criptografia de senhas com BCrypt
* Endpoints protegidos
* Controle de acesso baseado em permissões

### Fluxo de Autenticação

```text
Cliente
   │
   ▼
POST /auth
   │
   ▼
Validação de Credenciais
   │
   ▼
Geração de JWT
   │
   ▼
Access Token + Refresh Token
```

### Fluxo de Autorização

```text
Cliente
   │
Authorization: Bearer <token>
   │
   ▼
JWT Filter
   │
   ▼
Spring Security Context
   │
   ▼
Endpoint Protegido
```

---

# 📋 Funcionalidades

## Usuários

* Cadastro de usuários
* Consulta do perfil autenticado
* Alteração de senha
* Promoção de usuários para administrador
* Gerenciamento de usuários por administradores

## Contas

* Cadastro de contas financeiras
* Consulta de contas
* Controle de saldo
* Filtro de transações por conta

## Receitas

* Cadastro de receitas
* Consulta de entradas financeiras

## Despesas

* Cadastro de despesas
* Consulta de despesas pagas
* Consulta de despesas pendentes
* Atualização de status
* Exclusão de despesas

## Pagamentos

* Registro de pagamentos
* Controle de histórico de pagamentos
* Gerenciamento de status

---

# 📁 Estrutura do Projeto

```text
personal-finance/
├── src/
│   ├── main/
│   │   ├── java/com/personal_finance/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── mapper/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   └── test/
│       ├── controller/
│       ├── integration/
│       ├── repository/
│       └── service/
```

---

# ⚙️ Instalação

### Clonar Repositório

```bash
git clone https://github.com/seu-usuario/personal-finance.git
cd personal-finance
```

### Configurar Banco de Dados

Configure o arquivo `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/personal_finance
    username: postgres
    password: password
```

### Executar Aplicação

```bash
./gradlew bootRun
```

---

# 🐳 Executando com Docker

Suba os containers:

```bash
docker compose up -d
```

---

# 🧪 Testes

Executar todos os testes:

```bash
./gradlew test
```

Cobertura de testes:

* Testes Unitários
* Testes de Integração
* Testes de Repositório
* Testes de Controllers

---

# 📚 Documentação da API

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

---

# 🔑 Endpoints de Autenticação

| Método | Endpoint         | Descrição                                                    |
| ------ | ---------------- | ------------------------------------------------------------ |
| POST   | `/auth`          | Realiza autenticação e retorna Access Token e Refresh Token  |
| POST   | `/auth/register` | Cadastra um novo usuário                                     |
| POST   | `/auth/refresh`  | Gera um novo Access Token utilizando um Refresh Token válido |

---

# 👤 Endpoints de Usuários

| Método | Endpoint                 | Descrição                               | Permissão           |
| ------ | ------------------------ | --------------------------------------- | ------------------- |
| GET    | `/users/me`              | Retorna os dados do usuário autenticado | Usuário Autenticado |
| GET    | `/users/{id}`            | Busca usuário por ID                    | ADMIN               |
| GET    | `/users`                 | Lista todos os usuários                 | ADMIN               |
| PATCH  | `/users/change-password` | Altera a senha do usuário autenticado   | Usuário Autenticado |
| DELETE | `/users/{id}`            | Remove um usuário                       | ADMIN               |
| PUT    | `/users/{id}/promote`    | Promove um usuário para ADMIN           | ADMIN               |

### Perfis de Acesso

| Perfil | Permissões                                             |
| ------ | ------------------------------------------------------ |
| USER   | Gerenciar seus próprios recursos e alterar senha       |
| ADMIN  | Gerenciar usuários e acessar operações administrativas |

---

# 🏦 Endpoints de Contas

| Método | Endpoint         | Descrição     |
| ------ | ---------------- | ------------- |
| POST   | `/accounts`      | Criar conta   |
| GET    | `/accounts`      | Listar contas |
| DELETE | `/accounts/{id}` | Excluir conta |

---

# 💸 Endpoints de Despesas

| Método | Endpoint         | Descrição                   |
| ------ | ---------------- | --------------------------- |
| POST   | `/expenses`      | Criar despesa               |
| GET    | `/expenses`      | Listar despesas             |
| PATCH  | `/expenses/{id}` | Atualizar status da despesa |
| DELETE | `/expenses/{id}` | Excluir despesa             |

---

# 🏛 Arquitetura

A aplicação segue uma arquitetura em camadas:

```text
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
PostgreSQL
```

### Camadas

* **Controller Layer** → Exposição dos endpoints REST
* **Service Layer** → Regras de negócio
* **Repository Layer** → Persistência de dados
* **DTO Layer** → Transferência de dados
* **Mapper Layer** → Conversão entre entidades e DTOs utilizando MapStruct
* **Security Layer** → Autenticação e autorização com Spring Security e JWT

---

# 👨‍💻 Autor

**Rafael Nascimento Andrade**

Desenvolvedor Backend Java

* Java
* Spring Boot
* Spring Security
* PostgreSQL
* Docker
* APIs REST

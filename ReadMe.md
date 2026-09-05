# Banking REST API

A Spring Boot backend for a simple banking application — user
registration/login, account management, and balance operations
(credit and withdraw), backed by MySQL via Spring Data JPA.

## Features

- User registration, login, update, and delete
- Passwords hashed with BCrypt (`spring-security-crypto`) — never
  stored or returned in plaintext
- One account created automatically per user (`User` ↔ `Account`,
  1:1, sharing a primary key via JPA's `@MapsId`)
- Credit money into an account
- Withdraw money from an account, with an overdraft guard (rejects
  a withdrawal that would take the balance below zero)
- Look up an account by user id or by account number
- CORS enabled for a separate-origin frontend
- Unit tests (JUnit 5 + Mockito) for the service layer, covering
  credit/withdraw edge cases and login success/failure

**In progress:** transferring money between two accounts
(`sender_account_no` / `receiver_account_no` style transfer). Not
yet wired up end-to-end.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java |
| Framework | Spring Boot |
| Data access | Spring Data JPA (Hibernate) |
| Database | MySQL |
| Security | Spring Security Crypto (BCrypt) |
| Build tool | Maven |
| Testing | JUnit 5, Mockito |

## Architecture

Standard layered structure:

```
controller/   → HTTP concerns: request mapping, status codes
service/      → business logic (credit/debit rules, password hashing/verification)
repository/   → Spring Data JPA interfaces
model/        → JPA entities (User, Account)
dto/          → request bodies decoupled from entities (e.g. AmountRequest)
config/       → CORS config, PasswordEncoder bean
```

Entities are kept separate from API request bodies on purpose —
credit/withdraw take a dedicated `AmountRequest` DTO rather than
the `Account` entity itself, so the API contract doesn't shift
every time the database schema does.

## API endpoints

### User

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/user` | Register a new user (also creates their account) |
| POST | `/api/user/login` | Log in with name, mobile number, and password |
| PUT | `/api/user` | Update a user's details |
| DELETE | `/api/user` | Delete a user |

### Account

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/account/user/{userId}` | Get the account belonging to a user |
| GET | `/api/account/balance/{accountNo}` | Get an account by its account number |
| PUT | `/api/account/credit` | Credit money into an account |
| PUT | `/api/account/withdraw` | Withdraw money from an account |

Credit/withdraw request body:
```json
{
  "accountNo": 100001,
  "amount": 500.00
}
```

## Getting started

### Prerequisites
- Java 17+ (or whatever JDK version this project targets)
- Maven
- A running MySQL instance

### Configure the database

Edit `src/main/resources/application.properties` with your local
MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/<your_db_name>
spring.datasource.username=<your_username>
spring.datasource.password=<your_password>
spring.jpa.hibernate.ddl-auto=update
```

> Don't commit real credentials — use environment variables or a
> local, git-ignored properties file for anything beyond your own
> machine.

### Run it

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080` by default.

### Run the tests

```bash
./mvnw test
```

## Security notes

- Passwords are hashed with BCrypt before being saved, and are
  never included in any JSON response (`User.password` is
  write-only).
- There is currently no authentication token (JWT/session) — every
  endpoint is open once CORS allows the request through. This is
  fine for local development but would need addressing before any
  real deployment.

## Roadmap

- [ ] Transfer money between two accounts
- [ ] JWT-based authentication
- [ ] Request validation (`@Valid` / `@NotNull` on DTOs)
- [ ] Global exception handling (`@ControllerAdvice`)
- [ ] Wrap multi-step balance operations in `@Transactional`

## Frontend

A React frontend that consumes this API is maintained in a
separate repository.
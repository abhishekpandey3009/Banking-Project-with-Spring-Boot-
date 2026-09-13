# Banking Spring Boot Web Project (JWT Secured)

A REST API for a simple banking system built with **Spring Boot**, **Spring Security**, **Spring Data JPA**, and **JWT** authentication. Users can register, log in, and manage a linked bank account (credit, debit, and transfer funds).

## Features

- User registration and login with **BCrypt**-hashed passwords
- **Stateless JWT authentication** — no server-side sessions
- One bank account automatically created per user on registration
- Credit, debit, and peer-to-peer transfer between accounts
- Update and delete user records
- Route-level authorization: registration and login are public, everything else requires a valid token

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Security | Spring Security, JJWT 0.13.0 |
| Persistence | Spring Data JPA, MySQL |
| Build tool | Maven |
| Testing | JUnit 5, Mockito |

## Project Structure

```
src/main/java/com/abhi/Banking_SpringBootWeb_Project
├── config
│   ├── SecurityConfig.java     # Security filter chain, auth provider, auth manager
│   ├── JWTFilter.java          # Intercepts requests, validates Bearer tokens
│   └── PasswordConfig.java     # PasswordEncoder bean (BCrypt)
├── controller
│   ├── UserController.java     # /api/user endpoints
│   └── AccountController.java  # /api/account endpoints
├── dto
│   ├── AmountRequest.java      # credit/withdraw payload
│   └── TransactionRequest.java # transfer payload
├── model
│   ├── User.java
│   ├── UserPrincipal.java      # Spring Security UserDetails wrapper
│   └── Account.java
├── repository
│   ├── UserRepo.java
│   └── AccountRepo.java
└── service
    ├── UserService.java
    ├── AccountService.java
    ├── MyUserService.java       # UserDetailsService implementation
    └── JWTService.java          # Token generation / validation
```

## Getting Started

### Prerequisites
- Java 21
- Maven
- MySQL running locally (or update the datasource URL for your setup)

### 1. Clone and configure the database

Create a schema matching `spring.datasource.url` in `application.properties`, or edit that value to point at your own database. `spring.jpa.hibernate.ddl-auto=update` will create/update tables automatically on startup.

### 2. Configure the JWT secret

`application.properties` ships with a placeholder:
```properties
jwt.secret = //the secret key should be defined here
```
This is intentional — the real secret should **never** be committed. Generate a 256-bit Base64 key locally:
```bash
openssl rand -base64 32
```
Then supply it one of two ways (don't paste it into the tracked `application.properties`):

**Option A — local profile (gitignored):**
Create `src/main/resources/application-local.properties`:
```properties
jwt.secret=<your-generated-key>
```
Add it to `.gitignore`, then run with:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Option B — environment variable:**
```properties
jwt.secret=${JWT_SECRET}
```
```bash
export JWT_SECRET=<your-generated-key>
mvn spring-boot:run
```

### 3. Run the application
```bash
mvn spring-boot:run
```
The API starts on `http://localhost:8080`.

### 4. Run the tests
```bash
mvn test
```

## API Endpoints

### User

| Method | Endpoint | Auth required | Description |
|---|---|---|---|
| POST | `/api/user` | No | Register a new user (also creates a linked account) |
| POST | `/api/user/login` | No | Log in with `mobileNo` + `password`, returns a JWT |
| GET | `/api/user/id/{id}` | Yes | Check if a user exists by id |
| GET | `/api/user/mobile_no/{mobile_no}` | Yes | Check if a user exists by mobile number |
| PUT | `/api/user` | Yes | Update name / mobile number / password (send `user_id`) |
| DELETE | `/api/user` | Yes | Delete a user and their linked account |

### Account

| Method | Endpoint | Auth required | Description |
|---|---|---|---|
| GET | `/api/account/user/{userId}` | Yes | Get account details by user id |
| PUT | `/api/account/credit` | Yes | Credit an amount to an account |
| PUT | `/api/account/withdraw` | Yes | Debit an amount from an account |
| POST | `/api/account/transaction` | Yes | Transfer between two accounts |

### Authentication

Include the token returned from `/api/user/login` on every protected request:
```
Authorization: Bearer <token>
```
Tokens are valid for **30 minutes**.

### Sample requests

**Register**
```json
POST /api/user
{
  "name": "Jane Doe",
  "mobileNo": "9998887777",
  "password": "secret123"
}
```

**Login**
```json
POST /api/user/login
{
  "mobileNo": "9998887777",
  "password": "secret123"
}
```

**Update**
```json
PUT /api/user
{
  "user_id": 1,
  "name": "Jane D.",
  "mobileNo": "9998887777"
}
```
Omit `password` to leave it unchanged.

**Credit**
```json
PUT /api/account/credit
{
  "accountNo": 100001,
  "amount": 500.00
}
```

**Transfer**
```json
POST /api/account/transaction
{
  "senderAccountNo": 100001,
  "receiverAccountNo": 100002,
  "amount": 250.00
}
```

## Security Notes

- Login identifier is **mobile number**, not name, since it's the field enforced as `unique` in the database.
- Passwords are hashed with BCrypt before being stored — plaintext passwords are never persisted.
- The JWT signing key is loaded from configuration (`jwt.secret`), not generated at runtime, so restarting the application does not invalidate existing tokens.
- Sessions are stateless (`SessionCreationPolicy.STATELESS`) — all authorization happens via the bearer token on each request.

## Possible Future Improvements

- Move to a dedicated update DTO instead of accepting the full `User` entity on `PUT /api/user`
- Add refresh tokens so users aren't forced to re-login every 30 minutes
- Add transaction history / an audit log table
- Add global exception handling (`@ControllerAdvice`) for consistent error responses
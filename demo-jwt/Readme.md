# demo-jwt — JWT Authentication & Authorization Demo

A Spring Boot 4 / Spring Security demo showing stateless JWT-based authentication and
role-based authorization using the OAuth2 Resource Server support.

## Tech Stack

- Java 17, Spring Boot 4.0.8
- `spring-boot-starter-security`
- `spring-boot-starter-oauth2-resource-server` (JWT encode/decode via Nimbus)
- Stateless sessions (no cookies) — clients authenticate with a `Bearer` token

## How It Works

1. Client calls `POST /api/auth/login` with a username/password.
2. `AuthenticationManager` validates the credentials against the in-memory user store.
3. On success, a signed HS256 JWT is issued containing the username (`sub`) and a `roles` claim.
4. The client sends that token as `Authorization: Bearer <token>` on subsequent requests.
5. `SecurityFilterChain` authorizes each request by path pattern and role (`hasRole` / `hasAnyRole`).

## In-Memory Users

| Username | Password      | Roles           |
|----------|---------------|-----------------|
| customer | Customer@123  | USER            |
| admin    | Admin@123     | USER, ADMIN     |

## Endpoints & Access Rules

| Endpoint              | Method | Access                  |
|-----------------------|--------|--------------------------|
| `/api/auth/login`     | POST   | Public (no token)        |
| `/api/public/health`  | GET    | Public (no token)        |
| `/api/accounts/me`    | GET    | Roles: USER or ADMIN     |
| `/api/admin/report`   | GET    | Role: ADMIN only         |

## Running the Application

```powershell
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` by default.

---

## Testing Authentication & Authorization with cURL

> On Windows PowerShell, use `curl.exe` (not the `curl` alias for `Invoke-WebRequest`) so that
> flags like `-H` and `-d` behave as shown below.

### 1. Call a public endpoint (no token required)

```powershell
curl.exe -i http://localhost:8080/api/public/health
```

Expected: `200 OK` with `{"status":"UP"}`.

### 2. Try a protected endpoint without a token (should fail)

```powershell
curl.exe -i http://localhost:8080/api/accounts/me
```

Expected: `401 Unauthorized`.

### 3. Log in as `customer` to get a JWT

```powershell
curl.exe -i -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"customer\",\"password\":\"Customer@123\"}"
```

Expected: `200 OK` with a JSON body like:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 600
}
```

Copy the `accessToken` value for the next steps (referred to below as `<USER_TOKEN>`).

### 4. Log in with invalid credentials (should fail)

```powershell
curl.exe -i -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"customer\",\"password\":\"wrong-password\"}"
```

Expected: `401 Unauthorized`.

### 5. Call `/api/accounts/me` with the customer token (should succeed)

```powershell
curl.exe -i http://localhost:8080/api/accounts/me `
  -H "Authorization: Bearer <USER_TOKEN>"
```

Expected: `200 OK` with the customer's account details.

### 6. Call the admin-only endpoint with the customer token (should be forbidden)

```powershell
curl.exe -i http://localhost:8080/api/admin/report `
  -H "Authorization: Bearer <USER_TOKEN>"
```

Expected: `403 Forbidden` — `customer` has role `USER` but not `ADMIN`.

### 7. Log in as `admin` to get an admin JWT

```powershell
curl.exe -i -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"admin\",\"password\":\"Admin@123\"}"
```

Copy the `accessToken` value (referred to below as `<ADMIN_TOKEN>`).

### 8. Call the admin-only endpoint with the admin token (should succeed)

```powershell
curl.exe -i http://localhost:8080/api/admin/report `
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

Expected: `200 OK` with the admin report payload.

### 9. Admin token also works on the USER/ADMIN-shared endpoint

```powershell
curl.exe -i http://localhost:8080/api/accounts/me `
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

Expected: `200 OK`.

### 10. Call a protected endpoint with a malformed/expired token (should fail)

```powershell
curl.exe -i http://localhost:8080/api/accounts/me `
  -H "Authorization: Bearer not-a-real-token"
```

Expected: `401 Unauthorized`.

### Summary of Expected Results

| Scenario                                        | Expected Status |
|--------------------------------------------------|------------------|
| Public endpoint, no token                         | 200              |
| Protected endpoint, no token                      | 401              |
| Login with valid credentials                      | 200              |
| Login with invalid credentials                    | 401              |
| USER token → shared endpoint                      | 200              |
| USER token → admin-only endpoint                  | 403              |
| ADMIN token → admin-only endpoint                 | 200              |
| ADMIN token → shared endpoint                     | 200              |
| Invalid/malformed token → protected endpoint      | 401              |

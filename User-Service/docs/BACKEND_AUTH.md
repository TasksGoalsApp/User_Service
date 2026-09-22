# Backend authentication contract

The authoritative service sources for this change are the User_Service, Tasks_Service,
and Goal_service repositories under the Desktop's `To Do Managment project` folder.
The Daily Dojo frontend copy does not include backend services. This change does not
edit either frontend or run a migration against an existing database.

## Running locally

Use Java 21. The three independent Gradle builds retain Spring Boot 3.5.14.

| Service | Port | Current-user read endpoint |
| --- | --- | --- |
| User | 8081 | GET /user/me |
| Task | 8082 | GET /tasks/user |
| Goal | 8083 | GET /goal |

Set `JWT_SECRET` to the **same Base64-encoded random key of at least 32 bytes** in
all three service processes or IDE run configurations. There is no production secret
fallback in source. Generate a new value locally; do not reuse the old committed key.
To generate it into a PowerShell variable without printing it:

```powershell
$keyBytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($keyBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($keyBytes)
```

Child processes started from that PowerShell session inherit the variable. Separate
terminals/IDE configurations must receive that same value; generating one per service
will cause signature validation to fail. This does not set a persistent machine-wide
environment variable. Configure existing database connection settings separately.

Optional settings (identical issuer/audience across services):

| Environment variable | Default |
| --- | --- |
| JWT_ISSUER | daily-dojo-user-service |
| JWT_AUDIENCE | daily-dojo-api |
| JWT_EXPIRATION_MS | 3600000 (user service only) |
| CORS_ALLOWED_ORIGINS | http://localhost:5173 |

`CORS_ALLOWED_ORIGINS` accepts comma-separated exact origins. Browser cookies are not
used for authentication; requests carry `Authorization: Bearer <accessToken>`.
The frontend must later set its task service URL to `http://localhost:8082/tasks`.

## Login and token rules

POST `/user/register` creates a CUSTOMER with a BCrypt password hash.
POST `/user/login` accepts `username` and `password`. Invalid credentials return 401.
The response retains `accessToken`, `tokenType`, and `expiresIn` (`idToken` is unused).
**expiresIn is now seconds**, matching the actual JWT lifetime rounded to whole seconds.

All services validate HS256 signatures, issuer, audience, expiration and required
claims. The numeric positive integral `id` identifies the user. `roles` is a nonempty
array of uppercase names, normally `["CUSTOMER"]` or `["ADMIN"]`. `sub`, `iat`, and
`exp` are required. Spring's default timestamp validation permits its standard clock
skew. Numeric IDs are converted safely regardless of Integer/Long JSON representation.

Old tokens need a fresh login because the issuer/audience contract and signing key
have changed. There is no refresh endpoint or server-side token revocation in this
iteration; clearing browser storage alone does not invalidate an already issued JWT.

## Authorization

Spring Security's resource-server filter is the only bearer authentication mechanism.
Sessions are stateless and JSR-250 method authorization is enabled in all three services.
`roles` maps to `ROLE_` authorities. CUSTOMER resource routes require CUSTOMER;
ADMIN has explicit access to `/user/admin/{userid}` and `/user/me`, not an implicit
override of every CUSTOMER permission.

Missing/invalid tokens return 401; authenticated users without the required role
receive 403. A resource ID belonging to another user returns 404, the same as an
absent resource. Task update and all subtask operations now scope access to the token
user. Existing goal ownership checks are retained. Creation takes ownership from the
validated JWT, never from a supplied userId in the request.

Existing endpoint and request field names are preserved, including the oddly named
PUT `/tasks/subtasks/delete` update endpoint. Renaming that endpoint is separate API
cleanup. Subtask lists now return all matches; the update completion date uses a
date-compatible validation constraint.

## Verification and limits

Run `./gradlew test` (Windows: `.\gradlew.bat test`) in each service root. The test
profile uses an isolated H2 database and a public test-only signing key. Tests cover
registration/login/profile, malformed/expired/tampered tokens, claim/issuer/audience
validation, role denial, allowed/disallowed CORS origins, and record ownership.

These tests exercise Spring's actual security filter chain and signed JWTs, not mocked
authenticated users. H2 verifies the application mappings and repository queries; it
does not establish the state of an existing MySQL schema. In particular, any older
user-role constraint/data must allow uppercase CUSTOMER/ADMIN. No live MySQL data,
schema, or constraints were changed. Database credentials remain as originally configured.

Habits has no service implementation in the inspected copy. Notification is outside
this user-facing JWT pass; its ingestion endpoint needs an explicit service-to-service
authentication design before external exposure.

Reference: Spring Security 6.5 [JWT resource server](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html)
and [method security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html).

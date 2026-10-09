# Module 6: Course Wrap-up

## Topics Covered
- Best practices
- Project discussion
- Post assessment
- Q&A

---

## 1. Best Practices Recap

A consolidated checklist across everything covered in the course:

### Design & Configuration
- Prefer constructor injection over field injection for testability and immutability.
- Favor annotation-based configuration + component scanning; reserve `@Configuration`/`@Bean` for third-party or conditional beans.
- Keep environment-specific settings in Spring profiles (`application-{profile}.properties`), never hardcoded.

### REST API Design
- Use proper HTTP status codes and `ResponseEntity` to make API responses explicit and predictable.
- Validate all inbound data with Bean Validation (`@Valid`) at the controller boundary.
- Centralize error handling with `@RestControllerAdvice` — never leak stack traces or internal exception messages to clients.

### Data Access
- Default to `ddl-auto=validate` (or migrations via Flyway/Liquibase) in production; never `create`/`update`.
- Use `FetchType.LAZY` for associations by default to avoid unintended N+1 queries.
- Keep `@Transactional` service methods short-lived and free of external I/O calls.

### Security
- Hash passwords with `BCryptPasswordEncoder`; never store plaintext.
- Use stateless JWT authentication for REST APIs, with short-lived tokens and HTTPS everywhere.
- Store secrets (DB passwords, JWT signing keys) in environment variables or a secrets manager — never in source control.

### Microservices
- Each service should own its own data; avoid direct cross-service database access.
- Use service discovery (Eureka) instead of hardcoded service URLs.
- Design for failure: timeouts, retries, and circuit breakers are essential once calls cross network boundaries.

## 2. Project Discussion

Use this time to review the capstone project (Day 5, Module 5) as a group:

- Walk through the end-to-end request flow: Gateway → Employee Service → Department Service → PostgreSQL.
- Discuss design decisions made (e.g., database-per-service vs shared database, synchronous vs asynchronous inter-service communication).
- Identify potential improvements: adding a circuit breaker (e.g., Resilience4j) around the `department-service` call, introducing caching, or adding pagination to list endpoints.
- Compare alternative implementations across participants/teams and discuss trade-offs.

## 3. Post Assessment

A self-check to validate understanding across the five days. Sample questions:

1. What is the difference between `BeanFactory` and `ApplicationContext`?
2. Why does Spring Boot's auto-configuration "back off" when you define your own bean?
3. When would you choose a native SQL query over JPQL?
4. What HTTP status code should a validation failure return, and how do you produce it globally?
5. Why is `SessionCreationPolicy.STATELESS` used with JWT-based authentication?
6. What problem does service discovery (Eureka) solve in a microservices architecture?
7. Name two deployment patterns that reduce risk when releasing a new service version.

Suggested format: a short quiz (10–15 questions) plus a practical task — extend the capstone project with one new feature (e.g., add a `PATCH` endpoint, or secure `department-service` with its own JWT validation).

## 4. Q&A

Open floor for participant questions. Suggested topics to prompt discussion if needed:

- How does this map to real-world production systems (observability, CI/CD, containerization)?
- What's the recommended learning path after this course (Spring Cloud Config, Resilience4j, Kubernetes, testing with Testcontainers)?
- How do teams typically structure multi-module Maven/Gradle projects for microservices?
- What are common pitfalls teams hit when migrating from a monolith to microservices?

---

## Key Takeaways
- Consistently apply the security, validation, and transaction best practices established across the course to any new Spring Boot project.
- The capstone project is a template — extend it further to reinforce and deepen the concepts learned.
- Use the post-assessment to identify weaker areas and revisit the corresponding day's module documents.

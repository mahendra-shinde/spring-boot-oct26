# Module 1: Data Access in Spring

## Topics Covered
- Overview of JDBC
- JDBC vs JPA
- ORM concepts
- Hibernate overview

---

## 1. Overview of JDBC

JDBC (Java Database Connectivity) is the low-level Java API for connecting to and executing SQL against relational databases.

```java
String sql = "SELECT id, name, email FROM employee WHERE id = ?";

try (Connection conn = dataSource.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {

    stmt.setLong(1, employeeId);

    try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
            Employee employee = new Employee(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email"));
        }
    }
}
```

Spring simplifies raw JDBC via `JdbcTemplate`, removing boilerplate connection/resource management and exception handling:

```java
@Repository
public class EmployeeJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Employee findById(Long id) {
        String sql = "SELECT id, name, email FROM employee WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new Employee(rs.getLong("id"), rs.getString("name"), rs.getString("email")),
                id);
    }
}
```

**Security note**: always use `PreparedStatement`/parameter binding (`?` placeholders) — never concatenate user input into SQL strings, to prevent SQL injection.

## 2. JDBC vs JPA

| Aspect | JDBC | JPA |
|---|---|---|
| Abstraction level | Low-level, manual SQL | High-level, object-relational mapping |
| Boilerplate | High (connections, statements, result sets) | Low (annotated entities, repositories) |
| Portability | SQL often database-specific | More portable across databases via JPQL |
| Caching | None built-in | First-level (and optional second-level) cache |
| Object mapping | Manual (`ResultSet` → object) | Automatic (entity ↔ table mapping) |
| Use case | Fine-grained control, simple/legacy access | Rich domain models, relationships, complex queries |

## 3. ORM Concepts

Object-Relational Mapping (ORM) maps Java objects to relational database tables, letting developers work with objects instead of writing SQL for every operation.

Key ORM concepts:
- **Entity** — a Java class mapped to a database table.
- **Persistence Context** — a first-level cache managing entity instances and their lifecycle within a session/transaction.
- **Entity states**: *Transient* (not persisted) → *Managed/Persistent* (tracked by persistence context) → *Detached* (no longer tracked) → *Removed* (marked for deletion).
- **Dirty checking** — the ORM automatically detects changes to managed entities and issues `UPDATE` statements at flush/commit time, without explicit save calls.
- **Lazy vs eager loading** — associations can be fetched on-demand (lazy) or immediately (eager) alongside the owning entity.

## 4. Hibernate Overview

Hibernate is the most widely used JPA implementation (JPA is the specification; Hibernate is a provider). Spring Data JPA uses Hibernate as its default provider.

```properties
# application.properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

`ddl-auto` options:

| Value | Behavior |
|---|---|
| `none` | No schema management by Hibernate |
| `validate` | Validates schema matches entities, makes no changes (recommended for prod) |
| `update` | Updates schema to match entities (convenient for dev, risky for prod) |
| `create` | Drops and recreates schema on startup |
| `create-drop` | Creates schema on startup, drops it on shutdown (useful for tests) |

**Best practice**: use `validate` (or a proper migration tool like Flyway/Liquibase) in production; reserve `update`/`create` for local development only.

---

## Key Takeaways
- JDBC gives full manual control over SQL execution; `JdbcTemplate` removes boilerplate while keeping SQL explicit.
- JPA raises the abstraction level via ORM, trading some fine-grained control for productivity and portability.
- Hibernate is the default JPA provider used by Spring Data JPA, managing entity state and dirty checking automatically.
- Prefer `ddl-auto=validate` with dedicated schema migration tools in production environments.

# Module 5: Transactions

## Topics Covered
- Transaction management
- `@Transactional`

---

## 1. Transaction Management

A transaction groups multiple operations into a single atomic unit of work — either all operations succeed (commit) or none do (rollback), preserving data consistency (ACID properties: Atomicity, Consistency, Isolation, Durability).

Spring provides a consistent transaction abstraction (`PlatformTransactionManager`) across different data access technologies (JDBC, JPA, JMS), so application code doesn't need to manage transactions manually.

Without Spring's declarative support (manual/programmatic transaction management):

```java
TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
try {
    employeeRepository.save(employee);
    auditLogRepository.save(auditEntry);
    transactionManager.commit(status);
} catch (Exception ex) {
    transactionManager.rollback(status);
    throw ex;
}
```

Spring Boot auto-configures a `PlatformTransactionManager` (e.g., `JpaTransactionManager`) automatically when Spring Data JPA is on the classpath — no manual bean setup required.

## 2. `@Transactional`

`@Transactional` provides **declarative** transaction management — Spring wraps the annotated method in a transaction via an AOP proxy, without any manual boilerplate.

```java
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuditLogRepository auditLogRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                            AuditLogRepository auditLogRepository) {
        this.employeeRepository = employeeRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public Employee transferDepartment(Long employeeId, Long newDepartmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        employee.setDepartmentId(newDepartmentId);
        employeeRepository.save(employee);

        auditLogRepository.save(new AuditLog("Department changed", employeeId));

        // If auditLogRepository.save(...) throws, the employee update above
        // is also rolled back automatically.
        return employee;
    }
}
```

### Key Attributes

```java
@Transactional(
    propagation = Propagation.REQUIRED,
    isolation = Isolation.READ_COMMITTED,
    readOnly = false,
    timeout = 5,
    rollbackFor = Exception.class,
    noRollbackFor = IllegalArgumentException.class
)
public void processPayroll() { ... }
```

| Attribute | Purpose |
|---|---|
| `propagation` | How the method's transaction relates to an existing one (see below) |
| `isolation` | Isolation level (`READ_COMMITTED`, `REPEATABLE_READ`, `SERIALIZABLE`, etc.) |
| `readOnly` | Hints the persistence provider to optimize for read-only access |
| `timeout` | Maximum seconds before the transaction times out and rolls back |
| `rollbackFor` / `noRollbackFor` | Customize which exceptions trigger rollback |

**Default rollback rule**: Spring rolls back only on unchecked exceptions (`RuntimeException`/`Error`) by default. Checked exceptions do **not** trigger rollback unless declared via `rollbackFor`.

### Propagation Types

| Propagation | Behavior |
|---|---|
| `REQUIRED` (default) | Join existing transaction, or create a new one if none exists |
| `REQUIRES_NEW` | Always start a new transaction, suspending any existing one |
| `SUPPORTS` | Join existing transaction if present; run non-transactionally otherwise |
| `MANDATORY` | Must run within an existing transaction; throws an exception if none exists |
| `NOT_SUPPORTED` | Run non-transactionally, suspending any existing transaction |
| `NEVER` | Must run without a transaction; throws an exception if one exists |
| `NESTED` | Runs within a nested transaction (savepoint) if one exists |

### Common Pitfalls

- **Self-invocation**: calling a `@Transactional` method from another method *within the same class* bypasses the proxy — the transaction annotation has no effect. Move the call through a separate Spring-managed bean instead.
- **Read-only transactions**: mark query-only service methods with `@Transactional(readOnly = true)` to allow the persistence provider to optimize (e.g., skip dirty checking).
- **Long-running transactions**: keep transactional methods short-lived — avoid external calls (HTTP, file I/O) inside a transaction boundary, as they hold database connections/locks longer than necessary.

```java
@Transactional(readOnly = true)
public List<Employee> findAllEmployees() {
    return employeeRepository.findAll();
}
```

---

## Key Takeaways
- Transactions group operations into an all-or-nothing unit of work; Spring's `PlatformTransactionManager` abstracts this across JDBC/JPA.
- `@Transactional` provides declarative transaction demarcation via AOP proxies — no manual commit/rollback code needed.
- By default, only unchecked exceptions trigger rollback; use `rollbackFor` to include checked exceptions.
- Watch for self-invocation pitfalls, and mark read-only service methods with `readOnly = true` for optimization.

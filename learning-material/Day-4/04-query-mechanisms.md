# Module 4: Query Mechanisms

## Topics Covered
- Derived queries
- JPQL
- Native queries

---

## 1. Derived Queries

Spring Data JPA can generate queries automatically by parsing repository method names, following a defined keyword convention — no query implementation required.

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartmentName(String departmentName);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByAgeGreaterThanEqual(int age);

    List<Employee> findByNameContainingIgnoreCase(String namePart);

    List<Employee> findByDepartmentNameAndAgeBetween(String departmentName, int minAge, int maxAge);

    boolean existsByEmail(String email);

    long countByDepartmentName(String departmentName);

    List<Employee> findTop5ByOrderByAgeDesc();
}
```

Common keywords: `And`, `Or`, `Between`, `LessThan`, `GreaterThanEqual`, `IsNull`, `In`, `Containing`, `IgnoreCase`, `OrderBy`, `Top`/`First`.

**Trade-off**: derived queries are concise but can become unreadable for very complex conditions — switch to `@Query`/JPQL once method names get unwieldy.

## 2. JPQL (Java Persistence Query Language)

JPQL is an object-oriented query language similar to SQL but operates on **entities and their fields** instead of tables and columns, making queries portable across databases.

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e WHERE e.department.name = :deptName")
    List<Employee> findByDepartment(@Param("deptName") String deptName);

    @Query("SELECT e FROM Employee e WHERE e.age > :age ORDER BY e.name")
    List<Employee> findOlderThan(@Param("age") int age);

    @Query("SELECT new com.example.dto.EmployeeSummary(e.id, e.name, e.department.name) " +
           "FROM Employee e")
    List<EmployeeSummary> findAllSummaries();

    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.age = e.age + 1 WHERE e.department.id = :deptId")
    int incrementAgeForDepartment(@Param("deptId") Long deptId);
}
```

- `@Param` binds named parameters (`:deptName`) to method arguments.
- Constructor expressions (`SELECT new package.Dto(...)`) project results directly into DTOs.
- `@Modifying` is required for `UPDATE`/`DELETE` JPQL queries, and such methods must run within a transaction.

## 3. Native Queries

When JPQL cannot express a query (database-specific functions, complex joins, performance-tuned SQL), use native SQL directly via `nativeQuery = true`.

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query(value = "SELECT * FROM employee WHERE department_id = :deptId", nativeQuery = true)
    List<Employee> findByDepartmentIdNative(@Param("deptId") Long deptId);

    @Query(value = """
            SELECT e.*, d.name AS department_name
            FROM employee e
            JOIN department d ON e.department_id = d.id
            WHERE d.name = :deptName
            """, nativeQuery = true)
    List<Employee> findByDepartmentNameNative(@Param("deptName") String deptName);
}
```

- Native queries use actual **table/column names**, not entity/field names.
- **Security note**: always use `@Param`/positional bind parameters (never string-concatenate user input) to prevent SQL injection — this applies to native queries and JDBC alike.
- Native queries are less portable across database vendors than JPQL.

### Comparison

| Approach | Based On | Portability | Complexity Handling | Type Safety |
|---|---|---|---|---|
| Derived queries | Method name parsing | High | Low–Medium | High |
| JPQL (`@Query`) | Entity/field names | High | Medium–High | High |
| Native queries (`@Query(nativeQuery=true)`) | Actual SQL/table names | Low (DB-specific) | Highest | Medium |

**Guideline**: start with derived queries for simple lookups, move to JPQL for more complex but portable queries, and reserve native SQL for cases requiring database-specific features or query performance tuning.

---

## Key Takeaways
- Derived queries generate SQL from repository method names using Spring Data's keyword conventions.
- JPQL operates on entity/field names, staying portable across database vendors, and supports DTO projections via constructor expressions.
- Native queries provide full SQL control but sacrifice portability; always use bind parameters to avoid SQL injection.
- `@Modifying` + `@Transactional` are required for JPQL/native `UPDATE`/`DELETE` queries.

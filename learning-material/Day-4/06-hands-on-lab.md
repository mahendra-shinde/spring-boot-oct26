# Module 6: Hands-on Lab

## Topics Covered
- Integrate MySQL/PostgreSQL
- Create entities and repositories
- Implement relationships
- Perform CRUD operations with JPA

---

## Prerequisites
- JDK 21 installed
- Maven (or the Maven wrapper)
- PostgreSQL running locally (or via Docker)
- An IDE (VS Code / IntelliJ)

## Step 1: Integrate PostgreSQL

Generate a project via [Spring Initializr](https://start.spring.io/) with dependencies: `Spring Web`, `Spring Data JPA`, `PostgreSQL Driver`, `Validation`.

Start a local PostgreSQL instance via Docker:

```bash
docker run --name demo-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=demo -p 5432:5432 -d postgres:16
```

Configure the datasource:

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/demo
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> To use MySQL instead, swap the `PostgreSQL Driver` starter for `mysql-connector-j` and set `spring.datasource.url=jdbc:mysql://localhost:3306/demo`. The rest of the code below is identical — Spring Data JPA is database-agnostic.

**Security note**: for anything beyond local development, externalize credentials via environment variables (`SPRING_DATASOURCE_PASSWORD`) instead of hardcoding them in `application.properties`.

## Step 2: Create Entities and Repositories

```java
// entity/Department.java
@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees = new ArrayList<>();

    // getters/setters
}
```

```java
// entity/Employee.java
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // getters/setters
}
```

```java
// repository/DepartmentRepository.java
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
```

```java
// repository/EmployeeRepository.java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartmentId(Long departmentId);

    Optional<Employee> findByEmail(String email);
}
```

## Step 3: Implement Relationships

Wire the `Department` ↔ `Employee` (one-to-many / many-to-one) relationship through the service layer, keeping both sides in sync:

```java
// service/DepartmentService.java
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Department create(String name) {
        Department department = new Department();
        department.setName(name);
        return departmentRepository.save(department);
    }
}
```

```java
// service/EmployeeService.java
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                            DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public Employee create(String name, String email, Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));

        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment(department);

        return employeeRepository.save(employee);
    }
}
```

## Step 4: Perform CRUD Operations with JPA

```java
// controller/EmployeeController.java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeService employeeService, EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody EmployeeRequest request) {
        Employee employee = employeeService.create(request.name(), request.email(), request.departmentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @GetMapping
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Employee> update(@PathVariable Long id, @RequestBody EmployeeRequest request) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.setName(request.name());
                    employee.setEmail(request.email());
                    return ResponseEntity.ok(employeeRepository.save(employee));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!employeeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        employeeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

public record EmployeeRequest(String name, String email, Long departmentId) { }
```

Run and verify:

```bash
mvn spring-boot:run
```

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com","departmentId":1}'

curl http://localhost:8080/api/employees
```

## Verification Checklist

- [ ] Application connects to PostgreSQL successfully on startup
- [ ] `Department` and `Employee` tables are created (`ddl-auto=update`) with a foreign key relationship
- [ ] Creating an employee correctly associates it with an existing department
- [ ] `GET /api/employees` returns persisted employees including department data
- [ ] Update and delete endpoints correctly modify/remove rows in PostgreSQL

## Stretch Goals
- Add a derived query `findByDepartmentNameAndEmailContaining(...)` and test it via a new endpoint.
- Introduce Flyway for schema migrations and switch `ddl-auto` to `validate`.
- Add a `@ManyToMany` relationship between `Employee` and `Project`, and expose an endpoint to assign employees to projects.

---

## Key Takeaways
- Spring Data JPA + Spring Boot auto-configuration make integrating a relational database (PostgreSQL or MySQL) largely configuration-driven.
- Entities and repositories are defined declaratively; Spring Data generates the implementation and SQL.
- Relationship annotations (`@OneToMany`/`@ManyToOne`) model foreign keys directly on the domain model.
- Wrap multi-step writes in `@Transactional` service methods to guarantee atomicity.

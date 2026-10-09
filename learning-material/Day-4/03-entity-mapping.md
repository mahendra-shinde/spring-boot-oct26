# Module 3: Entity Mapping

## Topics Covered
- `@Entity`
- One-to-One relationship
- One-to-Many relationship
- Many-to-One relationship
- Many-to-Many relationship

---

## 1. `@Entity`

`@Entity` marks a class as a JPA-managed persistent entity, mapped to a database table.

```java
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    // getters/setters, no-arg constructor required by JPA
}
```

Requirements: a no-argument constructor, a field annotated with `@Id` (primary key), and a `@GeneratedValue` strategy (`IDENTITY`, `SEQUENCE`, `AUTO`, or `TABLE`) if the key is database-generated.

## 2. One-to-One Relationship

Each `Employee` has exactly one `EmployeeProfile`.

```java
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private EmployeeProfile profile;
}

@Entity
public class EmployeeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bio;

    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}
```

- The side holding `@JoinColumn` owns the relationship (its table has the foreign key).
- `mappedBy` on the inverse side references the owning side's field name.

## 3. One-to-Many / Many-to-One Relationship

One `Department` has many `Employee`s; each `Employee` belongs to one `Department`. This is modeled as a bidirectional `@OneToMany`/`@ManyToOne` pair.

```java
@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees = new ArrayList<>();
}

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}
```

- `@ManyToOne` is the **owning side** (its table stores the `department_id` foreign key) and defaults to `EAGER` fetch — usually overridden to `LAZY` to avoid unnecessary joins.
- `@OneToMany(mappedBy = ...)` is the **inverse side**, defaults to `LAZY` fetch.
- Helper methods keep both sides in sync:

```java
public void addEmployee(Employee employee) {
    employees.add(employee);
    employee.setDepartment(this);
}
```

## 4. Many-to-Many Relationship

An `Employee` can work on multiple `Project`s, and a `Project` can have multiple `Employee`s.

```java
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
        name = "employee_project",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> projects = new HashSet<>();
}

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "projects")
    private Set<Employee> employees = new HashSet<>();
}
```

- `@JoinTable` defines the join table (`employee_project`) with foreign keys to both sides.
- Prefer `Set` over `List` for `@ManyToMany` collections to avoid duplicate-row issues and Hibernate's "MultipleBagFetchException".

### Relationship Summary

| Relationship | Owning Side Annotation | Inverse Side Annotation | Join Structure |
|---|---|---|---|
| One-to-One | `@OneToOne` + `@JoinColumn` | `@OneToOne(mappedBy=...)` | Foreign key on owning table |
| One-to-Many / Many-to-One | `@ManyToOne` + `@JoinColumn` | `@OneToMany(mappedBy=...)` | Foreign key on the "many" side table |
| Many-to-Many | `@ManyToMany` + `@JoinTable` | `@ManyToMany(mappedBy=...)` | Separate join table |

---

## Key Takeaways
- `@Entity` + `@Id` map a Java class to a database table with a primary key.
- Relationship annotations (`@OneToOne`, `@OneToMany`, `@ManyToOne`, `@ManyToMany`) model associations; the owning side controls the foreign key/join table.
- Default to `FetchType.LAZY` for collections and to-one associations to avoid unintended eager loading and performance issues.
- Use `Set` for `@ManyToMany` collections and helper methods to keep bidirectional associations consistent.

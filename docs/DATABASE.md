# Database Design

## ER Diagram (Mermaid)

```mermaid
erDiagram
    ROLES ||--o{ USERS : has
    USERS ||--o| EMPLOYEES : linked
    DEPARTMENTS ||--o{ EMPLOYEES : contains
    EMPLOYEES ||--o{ ATTENDANCE_RECORDS : has
    EMPLOYEES ||--o{ LEAVE_REQUESTS : submits
    EMPLOYEES ||--o{ LEAVE_BALANCES : has
    LEAVE_REQUESTS ||--o{ LEAVE_APPROVAL_HISTORY : audited
    USERS ||--o{ LEAVE_APPROVAL_HISTORY : performs

    ROLES {
        bigint id PK
        string name UK
    }
    USERS {
        bigint id PK
        string email UK
        string password_hash
        bigint role_id FK
        boolean active
    }
    EMPLOYEES {
        bigint id PK
        string employee_code UK
        bigint user_id FK
        string first_name
        string last_name
        string email UK
        bigint department_id FK
        string employment_status
        date joining_date
        decimal salary
        timestamp deleted_at
    }
    DEPARTMENTS {
        bigint id PK
        string name UK
        string code UK
        bigint head_id FK
    }
    ATTENDANCE_RECORDS {
        bigint id PK
        bigint employee_id FK
        date attendance_date
        timestamp check_in_at
        timestamp check_out_at
        string status
    }
    LEAVE_REQUESTS {
        bigint id PK
        bigint employee_id FK
        string leave_type
        date start_date
        date end_date
        string status
    }
    LEAVE_BALANCES {
        bigint id PK
        bigint employee_id FK
        string leave_type
        int year
        decimal total_days
        decimal used_days
        decimal pending_days
    }
```

## Migrations

- `V1__init_schema.sql` — Tables, indexes, FKs, seed roles
- `V2__seed_data.sql` — Sample departments and employees (demo)

Application `DataInitializer` ensures demo users have correct BCrypt passwords on startup.

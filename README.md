# Project Tracker API - BuildMaster 🚀

A backend system for tracking projects, tasks, and developers at **BuildMaster**, built using Spring Boot, JPA, MongoDB, and other modern backend practices.

## 📚 Overview

BuildMaster is growing rapidly, and needs better visibility into team performance and task progress. This Project Tracker API provides a scalable backend solution to:

- Track projects, tasks, and developers.
- Filter and sort data by deadlines, status, and activity.
- Persist audit logs in MongoDB for every create/update/delete action.
- Optimize performance with caching, pagination, and transactions.

## 🛠️ Tech Stack

- `Backend`: Spring Boot 3.3.12
- `Spring Data JPA`
- `Spring Web`
- `Caching`: Spring Cache
- `NoSQL DB`: MongoDB (for audit logs)
- `Relational DB`: PostgreSQL
- `Spring Validation`
- `Swagger/OpenAPI (SpringDoc)`
- `Lombok`
- `MapStruct`(DTO mapping)
- `Logging`: SLF4J + Logback
- `Build Tool`: Maven

---

## 📦 Features

### ✅ Core Functionality

- CRUD operations for:
    - **Projects**
    - **Developers**
    - **Tasks**
- Assign tasks to developers
- Retrieve tasks by developer or project
- Pagination & sorting support
- Caching frequently accessed endpoints
- Transactional operations for consistency

### 🔍 Advanced Features

- Audit logging to **MongoDB**
- Query performance optimization
- Advanced querying with JPQL/native SQL
- Custom projections and DTOs
- Endpoints for analytics (e.g., overdue tasks, top developers)

---

## 📁 Entity Structure

### 🗂 Project

- `id`: UUID
- `name`: String
- `description`: String
- `deadline`: LocalDate
- `status`: Enum (PLANNING, IN_PROGRESS, COMPLETED, ON_HOLD, CANCELLED)
- `createdDate`: LocalDateTime
- `updatedDate`: LocalDateTime
- `tasks`: One-to-Many with Task

### 👨‍💻 Developer

- `id`: UUID
- `name`: String
- `email`: String
- `skills`: String
- `tasks`: One-to-Many with Task

### ✅ Task

- `id`: UUID
- `title`: String
- `description`: String
- `status`: Enum (TODO, IN_PROGRESS, COMPLETED, BLOCKED)
- `dueDate`: LocalDate
- `project`: Many-to-One with Project
- `assignedDeveloper`: Many-to-One with Developer

### 📜 AuditLog (MongoDB)

- `id`: ObjectId
- `actionType`: String (CREATE, UPDATE, DELETE)
- `entityType`: String
- `entityId`: UUID
- `timestamp`: Date
- `actorName`: String
- `payload`: JSON (snapshot of data)

---

## 🔁 API Endpoints Overview

| Resource | Method | Endpoint | Description |
|---------|--------|----------|-------------|
| Projects | GET | `/api/projects?page=0&size=10` | List projects (paginated) |
| Projects | POST | `/api/projects` | Create a new project |
| Projects | DELETE | `/api/projects/{id}` | Delete a project (cascade delete tasks) |
| Tasks | POST | `/api/tasks` | Create a new task |
| Tasks | GET | `/api/tasks/overdue` | Get all overdue tasks |
| Tasks | GET | `/api/tasks?sort=dueDate` | List sorted tasks |
| Developers | POST | `/api/developers` | Create a developer |
| Developers | GET | `/api/developers/top` | Top 5 developers by task count |
| Logs | GET | `/api/logs?entityType=Task&actorName=John` | Filter logs from MongoDB |

---

## 🧪 How to Run

1. **Clone the repo**
   ```bash
   git clone https://github.com/Ganza-Kevin-Murinda/BuildMaster-Project-Tracker-System.git
   cd projecttracker
   ```

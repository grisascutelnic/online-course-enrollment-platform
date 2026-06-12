# Online Course Enrollment Platform

## Project Overview

This project is a Proof of Concept (POC) microservice-based application developed as part of an internship assignment.

The platform allows students to enroll in courses, teachers to manage courses and enrollment requests, and administrators to manage users and permissions.

The application demonstrates:

* Microservice architecture
* REST communication
* Event-driven communication with RabbitMQ
* Service discovery with Eureka
* Centralized configuration with Spring Cloud Config Server
* API Gateway routing
* JWT authentication and authorization
* MongoDB persistence
* Docker-based deployment

---

## Technology Stack

* Java 21
* Spring Boot
* Spring Security
* Spring Data MongoDB
* Spring Cloud OpenFeign
* Spring Cloud Gateway
* Spring Cloud Netflix Eureka
* Spring Cloud Config Server
* RabbitMQ
* MongoDB
* Docker
* Docker Compose
* Postman

---

## Architecture

The system consists of two business microservices and three infrastructure services.

### Course Service

Responsibilities:

* User registration and authentication
* JWT token generation and validation
* User role management
* Course creation, update, deletion and retrieval
* Course status management
* Available seat validation
* Publishing enrollment events through RabbitMQ
* Retrieving enrollment statistics through REST communication

Database:

* MongoDB (`course_db`)

### Enrollment Service

Responsibilities:

* Consuming enrollment events from RabbitMQ
* Creating enrollment records automatically
* Managing enrollment statuses
* Providing enrollment statistics
* Exposing enrollment REST APIs

Database:

* MongoDB (`enrollment_db`)

### Infrastructure Services

#### Config Server

Provides centralized configuration for all services.

#### Eureka Server

Provides service registration and discovery.

#### API Gateway

Acts as a single entry point to the platform and routes requests to the appropriate microservice.

The Gateway forwards requests and Authorization headers. JWT validation is handled inside the microservices.

---

## Communication Between Services

### RabbitMQ Communication

Course Service publishes an enrollment event when a student requests enrollment.

Enrollment Service consumes the event and automatically creates an enrollment record.

Flow:

```text
Student
   ↓
Course Service
   ↓
RabbitMQ
   ↓
Enrollment Service
```

### REST Communication

Course Service uses OpenFeign to retrieve enrollment statistics from Enrollment Service.

Flow:

```text
Course Service
      ↓
Enrollment Service
```

---

## Security

Authentication is implemented using JWT.

Roles:

### ADMIN

* Full access to the system
* Promote or demote users between STUDENT and TEACHER roles

### TEACHER

* Create courses
* Update courses
* Delete courses
* View enrollments for own courses
* Approve enrollments
* Reject enrollments
* Mark enrollments as completed

### STUDENT

* View available courses
* Request enrollment in courses
* View personal enrollments
* Track enrollment status

---

## Enrollment Workflow

Enrollment statuses follow a predefined workflow:

```text
PENDING
 ├── APPROVED
 │     ├── COMPLETED
 │     └── CANCELLED
 │
 ├── REJECTED
 └── CANCELLED
```

Final statuses:

* REJECTED
* COMPLETED
* CANCELLED

---

## Main Business Flow

1. User registers.
2. User authenticates and receives a JWT token.
3. Administrator promotes a user to TEACHER if necessary.
4. Teacher creates a course.
5. Student requests enrollment in a course.
6. Course Service validates seat availability.
7. Course Service publishes an EnrollmentRequested event.
8. Enrollment Service consumes the event.
9. Enrollment Service creates an enrollment with status `PENDING`.
10. Teacher approves or rejects the enrollment.
11. Enrollment status is updated according to the workflow.
12. Course statistics can be retrieved through REST communication.

---

## Running the Project

### Build Docker Images

```bash
cd course-service
mvn clean package -DskipTests
docker build -t course-service .

cd ../enrollment-service
mvn clean package -DskipTests
docker build -t enrollment-service .

cd ../eureka-server
mvn clean package -DskipTests
docker build -t eureka-server .

cd ../config-server
mvn clean package -DskipTests
docker build -t config-server .

cd ../gateway
mvn clean package -DskipTests
docker build -t gateway .
```

### Start the Platform

```bash
docker compose up -d
```

### Stop the Platform

```bash
docker compose down
```

---

## Service Ports

| Service             | Port  |
| ------------------- | ----- |
| API Gateway         | 8080  |
| Course Service      | 8081  |
| Enrollment Service  | 8082  |
| Eureka Server       | 8761  |
| Config Server       | 8888  |
| RabbitMQ            | 5672  |
| RabbitMQ Management | 15672 |
| Mongo Course DB     | 27017 |
| Mongo Enrollment DB | 27018 |

---

## API Examples

### Authentication

```http
POST /auth/register
POST /auth/login
```

### User Management

```http
PATCH /users/{id}/role
```

### Course Management

```http
POST /courses
GET /courses
GET /courses/{id}
PUT /courses/{id}
DELETE /courses/{id}
PATCH /courses/{id}/status
```

### Enrollment Management

```http
POST /courses/{id}/enrollment-requests
GET /enrollments
GET /enrollments/course/{courseId}/stats
PATCH /enrollments/{id}/status
```

---

## Postman Collection



The collection contains:

* Authentication requests
* User management requests
* Course management requests
* Enrollment management requests


---

## Example Scenario

1. Admin logs in.
2. Admin promotes a user to TEACHER.
3. Teacher creates a course.
4. Student logs in.
5. Student requests enrollment.
6. Course Service publishes an event to RabbitMQ.
7. Enrollment Service creates an enrollment record.
8. Teacher approves the enrollment.
9. Enrollment statistics are retrieved through REST communication.

---

## Project Structure

```text
online-course-enrollment-platform
├── course-service
├── enrollment-service
├── eureka-server
├── config-server
├── gateway
├── config-repo
├── postman
├── docker-compose.yml
└── README.md
```

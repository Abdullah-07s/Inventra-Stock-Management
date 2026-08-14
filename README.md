# Inventra

> **Inventory and warehouse management with custom JWT authentication and fine-grained RBAC.**

Inventra is a full-stack inventory and warehouse management system built with **Java, Spring Boot, MySQL, and a server-rendered Thymeleaf frontend**. It is designed around a deliberate custom authentication and authorization architecture rather than Spring Security's built-in security stack.

The key idea is **permission-aware access**: `SUPERADMIN` has complete control, `ADMIN` accounts receive only the specific activity permissions granted to them, and `USER` accounts get standard product and order access.

---

## ✨ Highlights

- 🔐 **Custom JWT authentication** implemented manually without `spring-boot-starter-security`
- 🛡️ **Fine-grained RBAC** with `SUPERADMIN`, `ADMIN`, and `USER` roles
- 🎯 **Per-admin activity permissions** instead of blanket ADMIN access
- 👥 **Admin management** with promotion and permission assignment
- 📦 **Product management** with categories and suppliers
- 🏭 **Supplier management**
- 📊 **Warehouse stock tracking**
- 🧾 **Incoming/outgoing order workflow** with status management
- 🗄️ **MySQL + JPA/Hibernate** persistence
- 🌱 **Large realistic seed dataset** designed around internally consistent inventory records
- 🌓 **Responsive dark/light UI** with an amber-accent, CSS-variable-driven design
- 🖥️ **Role-aware dashboard** that hides modules the current user is not permitted to access
- 🧩 **Lombok-based entities and DTOs** with annotation explanations in source comments

---

## 🧠 Permission Model

Inventra separates **roles** from **activities**.

### SUPERADMIN

`SUPERADMIN` is the highest-privilege role and automatically passes activity checks.

They can:

- Manage products
- Manage suppliers
- Manage orders
- Manage stock
- View reports
- Manage administrators
- Promote users to `ADMIN`
- Assign or revoke individual activity permissions

### ADMIN

An `ADMIN` has **no blanket management access by default**.

Each administrator receives a custom set of activities such as:

```text
MANAGE_PRODUCTS
MANAGE_SUPPLIERS
MANAGE_ORDERS
MANAGE_STOCK
VIEW_REPORTS
MANAGE_ADMINS
```

For example:

```text
Admin A
├── MANAGE_PRODUCTS
├── MANAGE_STOCK
└── VIEW_REPORTS

Admin B
├── MANAGE_SUPPLIERS
└── MANAGE_ORDERS
```

The authorization layer checks these assignments at the endpoint level.

### USER

`USER` is the standard scoped role. Users can browse products and place orders, but they do not receive administrative management permissions.

---

## 🏗️ Architecture

Inventra follows a conventional layered backend architecture:

```text
┌────────────────────────────────────┐
│        Thymeleaf + Vanilla JS      │
│       Role-aware frontend UI       │
└──────────────────┬─────────────────┘
                   │ fetch()
                   ▼
┌────────────────────────────────────┐
│             Controllers            │
│        HTTP / endpoint layer       │
└──────────────────┬─────────────────┘
                   ▼
┌────────────────────────────────────┐
│              Services              │
│ Business logic + validation + auth │
└──────────────────┬─────────────────┘
                   ▼
┌────────────────────────────────────┐
│           Repositories             │
│          Spring Data JPA           │
└──────────────────┬─────────────────┘
                   ▼
┌────────────────────────────────────┐
│               MySQL                │
└────────────────────────────────────┘
```

Authentication and authorization sit across the request pipeline:

```text
Request
  │
  ▼
Custom JWT Filter
  │
  ├── No token ───────────────► public endpoint / reject
  │
  ├── Invalid token ──────────► reject
  │
  └── Valid token
          │
          ▼
   Authenticated User
          │
          ▼
 Custom Activity Check
          │
          ├── SUPERADMIN ─────► allow
          ├── ADMIN + granted ─► allow
          └── otherwise ───────► reject
```

---

## 🔐 Why Custom JWT Instead of Spring Security?

This project intentionally **does not use `spring-boot-starter-security`**.

Instead, authentication is implemented directly using application-level components:

- A JWT utility handles token creation, signing, expiry, and validation.
- A custom servlet/`OncePerRequestFilter`-style filter reads the `Authorization: Bearer ...` header.
- Valid JWT claims are converted into the application's authenticated-user context.
- Passwords are hashed with BCrypt independently of Spring Security.
- Authorization is performed through custom role/activity checks at the endpoint layer.

This is a deliberate architectural choice for demonstrating how authentication and authorization can be built and understood without relying on Spring Security's automatic filter chain and annotation-based authorization system.

> The custom implementation is intended to make the security flow explicit and inspectable in the codebase.

---

## 🗃️ Core Domain

The database is organized around the following main concepts:

```text
User
 ├── Role
 └── AdminPermission ──► Activity

Product
 ├── Category
 └── Supplier

StockRecord
 ├── Product
 └── Warehouse

Order
 └── OrderItem ──► Product
```

### Main entities

| Entity | Purpose |
|---|---|
| `User` | Authentication identity and role assignment |
| `Activity` | Defines a granular management permission |
| `AdminPermission` | Links an admin to an assigned activity |
| `Product` | Inventory catalogue item |
| `Category` | Product classification |
| `Supplier` | Supplier directory and product sourcing |
| `StockRecord` | Product quantity by warehouse/location |
| `Order` | Incoming/outgoing order record |
| `OrderItem` | Individual products and quantities within an order |

---

## 🌱 Realistic Seed Data

Inventra is intended to run against a **large, realistic dataset**, rather than a small collection of placeholder records such as `Product 1`, `Supplier A`, or `Order 10`.

The seed process is designed to create internally consistent relationships across:

- Products
- Categories
- Suppliers
- Warehouse stock
- Orders
- Order items
- Users and permissions

The confirmed seed source/approach should be documented here once the Phase 1 data-source decision is finalized.

---

## 🖥️ Frontend

The UI is a server-rendered Thymeleaf application backed by vanilla JavaScript.

### Dashboard behavior

The dashboard changes according to the authenticated user's role and granted activities.

**SUPERADMIN**

- Admin management
- Product management
- Supplier management
- Stock management
- Order management

**ADMIN**

- Only modules corresponding to explicitly granted activities
- No blanket ADMIN dashboard access

**USER**

- Product browsing
- Order placement
- Personal order visibility

The frontend uses the same JWT returned by the REST API and attaches it as a Bearer token to authenticated requests.

### UI direction

- Dark mode by default
- Optional light mode
- Amber/gold accent system
- CSS variables for consistent theming
- Responsive sidebar navigation
- Dashboard summary cards
- Searchable product and order tables
- Permission toggles for SUPERADMIN
- Mobile navigation
- Role-aware module visibility

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Backend | Spring Boot |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Authentication | Custom JWT |
| Password Hashing | BCrypt |
| Frontend | Thymeleaf + Vanilla JavaScript |
| Styling | Hand-written CSS / CSS variables |
| Boilerplate Reduction | Lombok |
| Build Tool | Maven |

**Important:** Spring Security is intentionally excluded from the project.

---

## 📡 API Reference

The API surface is intentionally limited to the following endpoints.

### Authentication

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a user |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT |

### Products

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/products` | Authenticated | List products |
| `POST` | `/api/products` | Activity-controlled | Create product |
| `PUT` | `/api/products/{id}` | Activity-controlled | Update product |
| `DELETE` | `/api/products/{id}` | Activity-controlled | Delete product |

### Suppliers

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/suppliers` | Authenticated | List suppliers |
| `POST` | `/api/suppliers` | Activity-controlled | Create supplier |
| `PUT` | `/api/suppliers/{id}` | Activity-controlled | Update supplier |
| `DELETE` | `/api/suppliers/{id}` | Activity-controlled | Delete supplier |

### Stock

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/stock` | Authenticated | View stock records |
| `PUT` | `/api/stock/{productId}` | Activity-controlled | Update product stock |

### Orders

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/orders` | Authenticated | List scoped orders |
| `POST` | `/api/orders` | User / permitted role | Place an order |
| `PUT` | `/api/orders/{id}/status` | Activity-controlled | Update order status |

### Administration

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/admins` | `SUPERADMIN` | List admins and assigned activities |
| `POST` | `/api/admins/{userId}/promote` | `SUPERADMIN` | Promote a user to admin |
| `PUT` | `/api/admins/{userId}/permissions` | `SUPERADMIN` | Assign/revoke an admin activity |
| `GET` | `/api/activities` | Authenticated | List available activity types |

No additional application endpoints are required by the project specification.

---

## 🚀 Setup

### Prerequisites

- JDK version required by the project's Spring Boot baseline
- MySQL Server
- Maven or the included Maven wrapper

Verify Java on Windows PowerShell:

```powershell
java -version
```

Verify Maven if installed globally:

```powershell
mvn -version
```

### Local configuration

Production/committed configuration should use environment placeholders rather than real credentials.

Example:

```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

For local development, keep real values in a gitignored local configuration such as:

```text
.env
application-local.properties
```

Never commit real database credentials or JWT secrets.

### Windows / PowerShell

Project commands should use PowerShell syntax. For example, when using the Maven wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

---

## 🔒 Secret & Git Safety

Before the first Git operation, verify that `.gitignore` contains at least:

```text
target/
.env
application-local.properties
.idea/
*.class
```

Then inspect the working tree before staging anything:

```powershell
git status
```

Do **not** commit:

- `.env`
- `application-local.properties`
- Database passwords
- JWT secrets
- Other generated credential files

---

## 📂 Project Structure

```text
Inventra/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ...
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   └── js/
│   │       ├── templates/
│   │       │   ├── fragments/
│   │       │   ├── dashboard.html
│   │       │   ├── login.html
│   │       │   └── register.html
│   │       └── application.properties
│   └── test/
├── .gitignore
├── .env.example
├── mvnw
├── mvnw.cmd
└── pom.xml
```

---

## 🔄 Typical Workflow

```text
User registers
      │
      ▼
User logs in
      │
      ▼
JWT issued
      │
      ▼
Custom JWT filter validates each protected request
      │
      ▼
Authenticated user + role
      │
      ▼
Activity authorization check
      │
      ├── SUPERADMIN → full access
      ├── ADMIN → explicitly granted activities
      └── USER → standard scoped access
      │
      ▼
Inventory / supplier / stock / order operation
      │
      ▼
MySQL persistence
```

---

## 🎯 Project Goals

Inventra demonstrates a production-oriented approach to:

- Layered Spring Boot architecture
- Relational inventory modeling
- Manual JWT authentication
- Custom authorization middleware
- Fine-grained RBAC
- Permission assignment at the individual-admin level
- Secure configuration management
- Realistic relational seed data
- Role-aware server-rendered UI
- Clean REST API boundaries

The most distinctive part of the project is the **custom authorization model**: an administrator's capabilities are determined by explicit activity assignments rather than by simply belonging to an `ADMIN` role.

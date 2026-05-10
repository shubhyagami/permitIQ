# Smart Permit Monitoring System - Step by Step Guide

## Before You Start: What You Need

| Tool | Purpose |
|------|---------|
| **Java 21** | Programming language |
| **MySQL** | Database (stores users and documents) |
| **IntelliJ IDEA / VS Code** | Code editor |
| **Postman** (optional) | Test REST APIs |

---

## STEP 1: Create User Objects (Signup & Login)

### What We Build
A `User` entity, repository, signup form, and REST endpoint so people can create accounts.

### Files Involved

| File | What It Does |
|------|-------------|
| `entity/User.java` | Defines the database table for users |
| `entity/Role.java` | Enum: `ROLE_USER` and `ROLE_ADMIN` |
| `entity/Gender.java` | Enum: MALE, FEMALE, OTHER, etc. |
| `repository/UserRepository.java` | Talks to the database (find, save, delete users) |
| `dto/SignupRequest.java` | The form data sent when someone signs up |
| `service/impl/AuthServiceImpl.java` | Registers a new user in the database |
| `controller/AuthViewController.java` | Shows the signup HTML page |
| `controller/AuthRestController.java` | REST API for signup/login (for Postman testing) |
| `templates/signup.html` | The signup web page |
| `templates/login.html` | The login web page |
| `config/DataInitializer.java` | Creates a default admin account on startup |

### How It Works (Simple Explanation)

1. User fills signup form (name, email, password, age, gender)
2. `SignupRequest` holds the form data
3. `AuthServiceImpl.register()` checks if email already exists, then saves the user to MySQL
4. Password is encrypted with BCrypt before saving
5. After signup, user can login via `/login` page

### How to Check It Works

**Option A - Browser:**
1. Start the project (see Step 5)
2. Open `http://localhost:8080/signup`
3. Fill the form and click "Create Account"
4. You should be redirected to login page with a success message
5. Now open `http://localhost:8080/login` and login

**Option B - Postman (REST API):**
```
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "age": 25,
    "gender": "MALE"
}
```
> You should get a `201 Created` response with the user details.

To verify the database: check MySQL table `users` - you should see your new user there.

### Default Admin Account (Auto-Created)
```
Email:    admin@test.com
Password: admin123
```
This account has admin privileges to access the Admin Panel.

---

## STEP 2: File Upload System

### What We Build
A `Document` entity, file storage service, and upload page so users can upload permits (PDF/JPG/PNG).

### Files Involved

| File | What It Does |
|------|-------------|
| `entity/Document.java` | Defines the database table for uploaded documents |
| `entity/DocumentStatus.java` | ACTIVE, EXPIRING_SOON, or EXPIRED |
| `repository/DocumentRepository.java` | Database queries for documents |
| `service/FileStorageService.java` | Interface for storing/deleting files |
| `service/impl/FileStorageServiceImpl.java` | Saves files to the `uploads/` folder with UUID names |
| `dto/DocumentResponse.java` | Data sent back to the browser after upload |
| `templates/upload.html` | The upload web page |

### How It Works

1. User goes to `/upload` page, selects a PDF/JPG/PNG file
2. `FileStorageServiceImpl.store()` validates the file (size < 10MB, correct type)
3. File is saved to the `uploads/` folder with a random UUID name
4. A `Document` record is created in the database
5. User is redirected to dashboard where the document appears

### How to Check It Works

1. Login to the application
2. Click "Upload" in the navigation bar
3. Select a PDF, JPG, or PNG file
4. Click "Upload"
5. You should be redirected to `/dashboard` with a success message
6. Check: a new file should appear in the `uploads/` folder
7. Check: a new row should appear in the MySQL `documents` table

**Postman (REST API):**
```
POST http://localhost:8080/api/documents/upload
Authorization: Bearer <your-jwt-token>
Content-Type: multipart/form-data

Key: file  |  Value: (select a PDF/JPG/PNG file)
```
> You get a `201 Created` response with extracted document details.

---

## STEP 3: Connect to AI (Gemini Document Extraction)

### What We Build
A Gemini AI service that reads uploaded documents and automatically extracts:
- Document name (e.g., "PUC Certificate")
- Permit number, Issue date, Expiry date, Authority name

### Files Involved

| File | What It Does |
|------|-------------|
| `ai/GeminiService.java` | Interface for AI extraction |
| `ai/GeminiServiceImpl.java` | Sends the document image to Google Gemini for analysis |
| `dto/DocumentExtractionResult.java` | Holds the extracted data from Gemini |
| `config/ApplicationProperties.java` | Reads your Gemini API key from `.env` |

### How It Works

1. When a file is uploaded, it's sent to Google's Gemini AI
2. Gemini looks at the document image and extracts permit details
3. The extracted data (name, dates, permit number) is saved to the document record
4. If Gemini fails or no API key is set, the document still uploads but without auto-extracted data

### How to Check It Works

**You MUST set your Gemini API key first:**

In the `.env` file:
```
GEMINI_API_KEY=your-actual-gemini-api-key-here
```

1. Login and upload a permit document (like a PUC certificate)
2. After upload, go to Dashboard
3. Look at the document card - if AI extraction worked, you'll see:
   - Document Name (e.g., "PUC Certificate")
   - Permit Number (e.g., "MH03-ABC-1234")
   - Issue Date and Expiry Date filled in automatically
4. Click "Edit" on the document to see all extracted fields
5. If fields are empty, check: (a) API key is valid, (b) the image is clear

**Without Gemini API key:** The upload still works, but you'll need to manually enter document details via the Edit button.

---

## STEP 4: Thymeleaf Frontend (Web Pages)

### What We Build
All the HTML pages using Thymeleaf templates with Bootstrap for styling.

### Pages Overview

| Page URL | Template File | What It Shows |
|----------|--------------|---------------|
| `/login` | `login.html` | Email + password login form |
| `/signup` | `signup.html` | Registration form (name, email, password, etc.) |
| `/dashboard` | `dashboard.html` | All your uploaded documents with countdown timers |
| `/upload` | `upload.html` | File upload form (PDF/JPG/PNG) |
| `/profile` | `profile.html` | Your account details (with Edit button) |
| `/profile/edit` | `profile-edit.html` | Edit your name, phone, company, age |
| `/expiring` | `expiring.html` | Documents expiring within 30 days |
| `/documents/{id}/edit` | `edit-document.html` | Edit a document's extracted details |
| `/admin` | `admin.html` | **Admin only:** View all users, delete users |
| `/admin/users/{id}` | `admin-user-detail.html` | **Admin only:** View a user's details & documents |

### How Thymeleaf Works (Simple)

- `th:text="${variable}"` → prints a variable on the page
- `th:each="item : ${list}"` → loops through a list
- `th:if="${condition}"` → shows/hides element based on condition
- `th:action="@{/path}"` → creates form action links
- `th:href="@{/path}"` → creates clickable links
- `th:value="${user.name}"` → pre-fills form inputs

### How to Check It Works

1. Start the project
2. Visit `http://localhost:8080/login` → you should see a login page
3. Click "Create one" → you should see the signup page
4. After login → you should see dashboard with navbar
5. Click each nav link (Profile, Expiring, Upload) - each should load correctly

---

## STEP 5: Admin Page (User Management)

### What We Build
An admin panel where administrators can:
- See total users and documents count
- View a table of ALL registered users
- Click "View" on any user to see their profile and documents
- Delete any user (and all their documents)

### Files Involved

| File | What It Does |
|------|-------------|
| `controller/DashboardController.java` | Has `/admin` and `/admin/users/{id}` and delete endpoints |
| `controller/AdminController.java` | REST API `/admin/stats` endpoint |
| `templates/admin.html` | Admin dashboard with users table and stats |
| `templates/admin-user-detail.html` | Shows one user's details and their documents |
| `config/DataInitializer.java` | Creates `admin@test.com / admin123` on startup |

### Access Control

- Only users with `ROLE_ADMIN` can access `/admin/**` pages
- The Admin button only appears in the navbar for admin users
- Regular users CANNOT see or access admin pages

### How to Check It Works

1. Login as admin: `admin@test.com` / `admin123`
2. After login, you should see an "Admin" button in the navbar (red, right side)
3. Click "Admin" → you should see:
   - Two cards: Total Users and Total Documents
   - A table listing all registered users with View and Delete buttons
4. Click "View" on any user → you should see their profile details and documents
5. Click "Delete" on a user → confirm → the user is removed from the database
6. Logout and login as a regular user → verify NO "Admin" button is visible

**Postman (REST API):**
```
GET http://localhost:8080/admin/stats
Authorization: Bearer <admin-jwt-token>
```
> Returns `{"users": 5, "documents": 12}`

---

## How to Start the Project

### 1. Set Up MySQL
```sql
CREATE DATABASE permit_db;
```

### 2. Configure Database Password
In `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    password: YOUR_MYSQL_PASSWORD
```

Or create a `.env` file:
```
DB_PASSWORD=YOUR_MYSQL_PASSWORD
GEMINI_API_KEY=your-gemini-key
```

### 3. Run the Project

**Using Maven (from terminal):**
```
cd permitIQ
mvnw spring-boot:run
```

**Or run the JAR directly:**
```
java -jar target/smart-permit-monitoring-system-0.0.1-SNAPSHOT.jar
```

### 4. Open in Browser
```
http://localhost:8080
```
(This redirects to `/login`)

---

## Project Structure Summary

```
src/main/java/com/compliance/dashboard/
├── ai/                          → Gemini AI integration
│   ├── GeminiService.java
│   └── GeminiServiceImpl.java
├── config/                      → App configuration
│   ├── AppConfig.java           → Password encoder, Swagger
│   ├── ApplicationProperties.java → Reads application.yml
│   └── DataInitializer.java     → Creates default admin
├── controller/                  → Handles web requests
│   ├── AdminController.java     → REST: /admin/stats
│   ├── AuthRestController.java  → REST: /api/auth/signup, /login
│   ├── AuthViewController.java  → Web: /login, /signup
│   ├── DashboardController.java → Web: /dashboard, /upload, /admin, etc.
│   ├── DocumentRestController.java → REST: /api/documents/**
│   └── UserRestController.java  → REST: /api/users/me
├── dto/                         → Data Transfer Objects (form data)
│   ├── LoginRequest.java
│   ├── SignupRequest.java
│   ├── DocumentResponse.java
│   └── ... (others)
├── entity/                      → Database tables
│   ├── User.java
│   ├── Document.java
│   ├── Role.java, Gender.java, DocumentStatus.java
├── exception/                   → Custom error handling
├── repository/                  → Database queries
│   ├── UserRepository.java
│   └── DocumentRepository.java
├── security/                    → Login, JWT, passwords
│   ├── SecurityConfig.java      → Which URLs need login
│   ├── JwtTokenProvider.java    → Creates JWT tokens
│   └── CustomUserDetailsService.java
├── service/                     → Business logic
│   ├── impl/AuthServiceImpl.java    → Register & login
│   ├── impl/DocumentServiceImpl.java → Upload, edit, delete docs
│   ├── impl/FileStorageServiceImpl.java → Save files to disk
│   ├── impl/OcrServiceImpl.java  → OCR (placeholder)
│   └── impl/ReminderScheduler.java → Auto-refresh expiry status
└── timer/                       → Countdown calculation
    └── CountdownServiceImpl.java

src/main/resources/
├── application.yml              → Database, JWT, file upload settings
└── templates/                   → HTML pages (Thymeleaf)
    ├── login.html
    ├── signup.html
    ├── dashboard.html
    ├── upload.html
    ├── profile.html / profile-edit.html
    ├── expiring.html
    ├── edit-document.html
    ├── admin.html
    └── admin-user-detail.html
```

## Quick Testing Flow

```
1. Start project → localhost:8080
2. Signup as a regular user → verify login works
3. Upload a PDF → verify it appears on dashboard
4. Check countdown timer → verify it shows days/hours/minutes
5. Edit the document → verify fields update and save
6. View Expiring page → verify expiring-soon docs appear
7. Edit your profile → verify changes save
8. Login as admin (admin@test.com / admin123)
9. Click Admin button → verify all users visible
10. View a user's documents → verify their docs show
11. Delete a user → verify they're removed
```
# Smart Permit Monitoring System — Complete Documentation

> **Tech Stack:** Spring Boot 4.0 · MySQL · Thymeleaf · Gemini AI · JWT · Bootstrap 5

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [STEP 1 — User Registration & Login](#2-step-1--user-registration--login)
3. [STEP 2 — File Upload System](#3-step-2--file-upload-system)
4. [STEP 3 — AI Document Extraction (Gemini)](#4-step-3--ai-document-extraction-gemini)
5. [STEP 4 — Thymeleaf Frontend Pages](#5-step-4--thymeleaf-frontend-pages)
6. [STEP 5 — Admin Panel & User Management](#6-step-5--admin-panel--user-management)
7. [Project Folder Structure](#7-project-folder-structure)
8. [Entity Deep Dive](#8-entity-deep-dive)
9. [Security Deep Dive](#9-security-deep-dive)
10. [Full Code Reference](#10-full-code-reference)
11. [How Data Flows Through the App](#11-how-data-flows-through-the-app)
12. [Quick Reference: Key Annotations](#12-quick-reference-key-annotations)

---

## 1. Project Overview

### What This App Does

```
┌─────────────────────────────────────────────────────────────────┐
│                    Smart Permit Monitoring System                │
│                                                                 │
│   Register  ──►  Login  ──►  Upload Permit  ──►  AI Extracts   │
│                                              │        │         │
│                                              ▼        ▼         │
│                                    Dashboard shows countdown    │
│                                    Admin manages all users      │
└─────────────────────────────────────────────────────────────────┘
```

This is a web application that lets users:

- **Sign up & login** with email/password
- **Upload permit documents** (PDF, JPG, PNG)
- **AI auto-extracts** permit details (name, dates, permit number)
- **Track expiry** with live countdown timers
- **Admin panel** to view/delete any user

### How to Run

```
┌──────────────────────────────────────┐
│ 1. Start MySQL (port 3306)          │
│ 2. Create database: permit_db       │
│ 3. Set password in application.yml  │
│ 4. Set Gemini key in .env file      │
│ 5. Run: mvnw spring-boot:run       │
│ 6. Open: http://localhost:8080     │
└──────────────────────────────────────┘
```

| Default Account | Email | Password |
|-----------------|-------|----------|
| Admin | `admin@test.com` | `admin123` |

---

## 2. STEP 1 — User Registration & Login

### Files You Need

```
┌─────────────────────────────────────────────────────────────┐
│  STEP 1 FILES                                               │
│                                                             │
│  entity/User.java          Database table definition        │
│  entity/Role.java          ROLE_USER / ROLE_ADMIN           │
│  entity/Gender.java        MALE, FEMALE, OTHER, etc.        │
│  repository/UserRepository.java   Database queries          │
│  dto/SignupRequest.java    Form data validation             │
│  dto/LoginRequest.java     Login form data                  │
│  dto/JwtResponse.java      Token returned after login       │
│  service/impl/AuthServiceImpl.java   Register & login logic │
│  controller/AuthViewController.java   Login/signup pages    │
│  controller/AuthRestController.java   REST API endpoints    │
│  templates/login.html      Login web page                   │
│  templates/signup.html     Signup web page                  │
│  config/DataInitializer.java   Creates default admin        │
└─────────────────────────────────────────────────────────────┘
```

### How Registration Works

```
Browser fills signup form
        │
        ▼
POST /signup ──► AuthViewController
        │
        ▼
AuthService.register()
   ├── Check: does email exist? ──► YES ──► Throw BadRequestException
   └── NO
        │
        ▼
   Build User object
   ├── name = "Test User"
   ├── email = "test@example.com"
   ├── password = BCrypt("password123")  ← ENCRYPTED
   └── role = ROLE_USER
        │
        ▼
   userRepository.save(user)  ──► MySQL "users" table
        │
        ▼
   Redirect to /login with success message
```

### How Login Works

```
Browser fills login form
        │
        ▼
POST /login ──► Spring Security intercepts
        │
        ▼
CustomUserDetailsService.loadUserByUsername(email)
   └── Find user by email in MySQL
        │
        ▼
BCrypt compares password hash
   ├── MATCH ──► Create session → redirect to /dashboard
   └── NO MATCH ──► Redirect to /login?error=true
```

### Code: User Entity

> **File:** `entity/User.java`

```
┌──────────────────────────────────────────────────────────┐
│  USERS TABLE (MySQL)                                     │
│                                                          │
│  id (PK)    │ BIGINT auto-increment                      │
│  name       │ VARCHAR(120) NOT NULL                      │
│  email      │ VARCHAR(160) UNIQUE NOT NULL  ← login with │
│  password   │ VARCHAR(255) NOT NULL       ← encrypted    │
│  phone      │ VARCHAR(30)                                │
│  company    │ VARCHAR(160)                               │
│  age        │ INT                                        │
│  gender     │ ENUM('MALE','FEMALE','OTHER',...)          │
│  role       │ ENUM('ROLE_USER','ROLE_ADMIN')             │
│  created_at │ DATETIME                                   │
│                                                          │
│  One User ──►has──► Many Documents (cascade delete)      │
└──────────────────────────────────────────────────────────┘
```

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false)
    private String password;   // Always stored ENCRYPTED (BCrypt)

    private String phoneNumber;
    private String company;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();
    // ↑ If user is deleted, ALL their documents are also deleted

    @PrePersist
    void onCreate() {
        if (role == null) role = Role.ROLE_USER;  // Default: regular user
        createdAt = LocalDateTime.now();           // Auto-set creation time
    }
}
```

### Code: UserRepository

> **File:** `repository/UserRepository.java`

```java
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);
    // → SELECT * FROM users WHERE LOWER(email) = ?

    boolean existsByEmailIgnoreCase(String email);
    // → SELECT COUNT(*) > 0 FROM users WHERE LOWER(email) = ?
}
```

> **Note:** `JpaRepository<User, Long>` auto-generates: `save()`, `findById()`, `findAll()`, `delete()`, and `count()`. You NEVER write SQL — Spring builds it from method names.

### Code: AuthService (Register)

> **File:** `service/impl/AuthServiceImpl.java`

```java
@Service
public class AuthServiceImpl implements AuthService {

    public UserResponse register(SignupRequest request) {

        // GUARD: Check duplicate email
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        // BUILD: Create user object
        User user = User.builder()
            .name(request.getName().trim())
            .email(request.getEmail().trim().toLowerCase())
            .password(passwordEncoder.encode(request.getPassword())) // ← ENCRYPT HERE
            .phoneNumber(request.getPhoneNumber())
            .company(request.getCompany())
            .age(request.getAge())
            .gender(request.getGender())
            .role(Role.ROLE_USER)   // Every new signup = regular user
            .build();

        // SAVE: Store in MySQL
        return toUserResponse(userRepository.save(user));
    }
}
```

### Code: DataInitializer (Auto-Create Admin)

> **File:** `config/DataInitializer.java`

```java
@Component
public class DataInitializer implements CommandLineRunner {
    // Runs ONCE when the app starts

    public void run(String... args) {
        if (!userRepository.existsByEmailIgnoreCase("admin@test.com")) {
            User admin = User.builder()
                .name("Admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ROLE_ADMIN)     // ← ADMIN role
                .age(25)
                .gender(Gender.PREFER_NOT_TO_SAY)
                .build();
            userRepository.save(admin);
        }
    }
}
```

### ✅ How to Verify Step 1 Works

```
┌───────────────────────────────────────────────────────────┐
│  VERIFICATION CHECKLIST                                    │
│                                                           │
│  □ Start the app (http://localhost:8080)                  │
│  □ Open /signup → fill form → click "Create Account"      │
│  □ Should redirect to /login with success message         │
│  □ Login with your new account → should reach /dashboard  │
│  □ Check MySQL "users" table → your user is there         │
│  □ Login as admin@test.com / admin123 → should work       │
│                                                           │
│  REST API Test (Postman):                                 │
│  □ POST /api/auth/signup → returns 201 Created            │
│  □ POST /api/auth/login → returns JWT token               │
└───────────────────────────────────────────────────────────┘
```

---

## 3. STEP 2 — File Upload System

### Files You Need

```
┌──────────────────────────────────────────────────────────────┐
│  STEP 2 FILES                                                │
│                                                              │
│  entity/Document.java          Document database table       │
│  entity/DocumentStatus.java    ACTIVE/EXPIRING_SOON/EXPIRED  │
│  repository/DocumentRepository.java  Document queries       │
│  service/FileStorageService.java     Interface               │
│  service/impl/FileStorageServiceImpl.java  Save/delete files │
│  dto/DocumentResponse.java     Data returned to browser      │
│  dto/DocumentUpdateRequest.java  Edit form data              │
│  templates/upload.html         Upload web page               │
│  templates/edit-document.html  Edit document page            │
└──────────────────────────────────────────────────────────────┘
```

### How Upload Works

```
Browser selects file (PDF/JPG/PNG)
        │
        ▼
POST /upload ──► DashboardController
        │
        ▼
FileStorageServiceImpl.store(file)
   ├── VALIDATE: Is file empty? Is size > 10MB? Is type allowed?
   ├── CREATE: uploads/ folder if missing
   ├── RENAME: "puc.pdf" → "a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf"
   └── SAVE: Files.copy(inputStream, uploads/UUID.pdf)
        │
        ▼
   Returns StoredFile(originalName, storedName, filePath, contentType)
        │
        ▼
DocumentService.upload(user, file)
   ├── Call fileStorageService.store()
   ├── Call ocrService.extractText()   [placeholder]
   ├── Call geminiService.extractDocumentData()  [Step 3]
   └── Save Document to MySQL
        │
        ▼
Redirect to /dashboard → document appears in list
```

### Code: Document Entity

> **File:** `entity/Document.java`

```
┌──────────────────────────────────────────────────────────────────┐
│  DOCUMENTS TABLE (MySQL)                                         │
│                                                                  │
│  id (PK)          │ BIGINT auto-increment                        │
│  user_id (FK)     │ BIGINT → references users(id)                │
│  document_name    │ VARCHAR(180) NOT NULL   ← e.g., "PUC Cert"   │
│  document_type    │ VARCHAR(80)             ← e.g., "Insurance"  │
│  permit_number    │ VARCHAR(120)            ← e.g., "MH03-123"   │
│  issue_date       │ DATE                                        │
│  expiry_date      │ DATE                    ← used for countdown │
│  authority_name   │ VARCHAR(180)                                │
│  original_file    │ VARCHAR(255) NOT NULL   ← "puc.pdf"         │
│  stored_file      │ VARCHAR(255) UNIQUE     ← "UUID.pdf"        │
│  file_path        │ VARCHAR(255) NOT NULL   ← "uploads/UUID.pdf"│
│  extracted_text   │ LONGTEXT                ← raw AI output      │
│  upload_time      │ DATETIME NOT NULL                           │
│  status           │ ENUM('ACTIVE','EXPIRING_SOON','EXPIRED')    │
│                                                                  │
│  Many Documents ──►belong to──► One User                         │
└──────────────────────────────────────────────────────────────────┘
```

```java
@Entity
@Table(name = "documents")
public class Document {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;      // ← Owner of this document

    @Column(nullable = false, length = 180)
    private String documentName;      // e.g., "PUC Certificate"

    @Column(length = 80)
    private String documentType;      // e.g., "Vehicle Document"

    @Column(length = 120)
    private String permitNumber;      // e.g., "MH03-ABC-1234"

    private LocalDate issueDate;       // When it was issued
    private LocalDate expiryDate;      // When it expires

    @Column(length = 180)
    private String authorityName;      // Which authority issued it

    @Column(nullable = false)
    private String originalFileName;   // "puc.pdf"

    @Column(nullable = false, unique = true)
    private String storedFileName;     // "a1b2c3d4...pdf"

    @Column(nullable = false)
    private String filePath;           // Full disk path

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;      // Raw text from AI

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentStatus status;     // ACTIVE / EXPIRING_SOON / EXPIRED

    @PrePersist
    void onCreate() {
        uploadTime = LocalDateTime.now();
        if (status == null) status = DocumentStatus.ACTIVE;
    }
}
```

### Code: FileStorageService

> **File:** `service/impl/FileStorageServiceImpl.java`

```java
@Service
public class FileStorageServiceImpl implements FileStorageService {

    // ONLY these types are allowed
    private static final Set<String> ALLOWED_TYPES =
        Set.of("application/pdf", "image/jpeg", "image/png");

    public StoredFile store(MultipartFile file) {

        validate(file);  // Check: not empty, not too big, correct type

        Files.createDirectories(uploadRoot);  // Create "uploads/" folder

        // Generate unique filename to avoid name conflicts
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1);
        String storedName = UUID.randomUUID() + "." + extension;
        // Example: "a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf"

        Path target = uploadRoot.resolve(storedName);
        Files.copy(file.getInputStream(), target, REPLACE_EXISTING);

        return new StoredFile(originalName, storedName, target.toString(), contentType);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new FileStorageException("File is required");

        if (file.getSize() > maxFileSizeBytes)   // 10MB from config
            throw new FileStorageException("File exceeds 10MB limit");

        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext))
            throw new FileStorageException("Only PDF, JPG, PNG allowed");
    }
}
```

### ✅ How to Verify Step 2 Works

```
┌─────────────────────────────────────────────────────────────┐
│  VERIFICATION CHECKLIST                                      │
│                                                             │
│  □ Login → click "Upload" in navbar                         │
│  □ Select a PDF, JPG, or PNG file                           │
│  □ Click "Upload" button                                    │
│  □ Should redirect to /dashboard with success message       │
│  □ Document card appears on dashboard                       │
│  □ Check: uploads/ folder has a new UUID-named file         │
│  □ Check: MySQL "documents" table has a new row             │
│  □ Upload a .txt file → should show "Only PDF, JPG, PNG"    │
│  □ Upload a 50MB file → should show "File exceeds limit"    │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. STEP 3 — AI Document Extraction (Gemini)

### Files You Need

```
┌─────────────────────────────────────────────────────────────┐
│  STEP 3 FILES                                               │
│                                                             │
│  ai/GeminiService.java        Interface                     │
│  ai/GeminiServiceImpl.java    Actual AI integration         │
│  dto/DocumentExtractionResult.java  AI response data        │
│  config/ApplicationProperties.java   Reads .env config      │
│  .env                         Your Gemini API key           │
└─────────────────────────────────────────────────────────────┘
```

### How AI Extraction Works

```
File uploaded (PDF/JPG/PNG)
        │
        ▼
Read file bytes: Files.readAllBytes(path)
        │
        ▼
Build request:
   ├── PART 1 (Text):  "Extract document name, permit number,
   │                    issue date, expiry date, authority..."
   │
   └── PART 2 (Image): The actual document image bytes
        │
        ▼
Send to Google Gemini API
   POST https://generativelanguage.googleapis.com
   ├── Model: gemini-2.5-flash
   ├── API Key: from .env file
   └── Config: response_mime_type = "application/json"
        │
        ▼
Gemini looks at the image
   ├── Reads text from the document
   ├── Identifies: name, permit number, dates
   └── Returns JSON
        │
        ▼
Parse JSON response:
{
  "documentName": "PUC Certificate",
  "permitNumber": "MH03-ABC-1234",
  "issueDate": "2024-01-15",
  "expiryDate": "2025-01-14",
  "authorityName": "RTO Mumbai",
  "documentType": "Vehicle Document"
}
        │
        ▼
Save extracted data to Document record in MySQL
```

### Code: GeminiService

> **File:** `ai/GeminiServiceImpl.java`

```java
@Service
public class GeminiServiceImpl implements GeminiService {

    // This is the instruction we send to Gemini
    private static final String PROMPT = """
        Extract the following information from this permit/document.
        Return JSON only:
        1. Document Name (PUC Certificate, Fire Safety, etc.)
        2. Permit Number
        3. Issue Date
        4. Expiry Date
        5. Authority Name
        6. Document Type

        Use this JSON structure:
        {
          "documentName": "",
          "permitNumber": "",
          "issueDate": "yyyy-MM-dd",
          "expiryDate": "yyyy-MM-dd",
          "authorityName": "",
          "documentType": ""
        }
        If missing, use null.
        """;

    public DocumentExtractionResult extractDocumentData(
            Path filePath, String contentType, String ocrText) {

        // Check if API key is configured
        if (apiKey is blank) {
            return DocumentExtractionResult.failed("Gemini API key not configured");
        }

        try {
            // 1. Read the file bytes
            byte[] bytes = Files.readAllBytes(filePath);

            // 2. Build content: text instruction + image bytes
            Content content = Content.fromParts(
                Part.fromText(PROMPT + "\nOCR text: " + ocrText),
                Part.fromBytes(bytes, contentType)    // ← The actual image
            );

            // 3. Configure: ask for JSON response
            GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .candidateCount(1)
                .build();

            // 4. Call Gemini API
            Client client = Client.builder().apiKey(apiKey).build();
            GenerateContentResponse response = client.models
                .generateContent(model, content, config);

            // 5. Parse JSON response into our result object
            JsonNode json = objectMapper.readTree(response.text());
            return DocumentExtractionResult.builder()
                .documentName(getText(json, "documentName"))
                .permitNumber(getText(json, "permitNumber"))
                .issueDate(getDate(json, "issueDate"))
                .expiryDate(getDate(json, "expiryDate"))
                .authorityName(getText(json, "authorityName"))
                .documentType(getText(json, "documentType"))
                .extractionSuccessful(true)
                .build();

        } catch (Exception ex) {
            return DocumentExtractionResult.failed(
                "Extraction failed: " + ex.getMessage()
            );
        }
    }
}
```

### Configuration: Set Your API Key

> **File:** `.env`

```
GEMINI_API_KEY=AIzaSy...your-actual-key-here...
GEMINI_MODEL=gemini-2.5-flash
DB_PASSWORD=your_mysql_password
```

> **File:** `application.yml` (relevant section)

```yaml
app:
  gemini:
    api-key: ${GEMINI_API_KEY}    # Reads from .env
    model: ${GEMINI_MODEL:gemini-2.5-flash}
```

### ✅ How to Verify Step 3 Works

```
┌─────────────────────────────────────────────────────────────┐
│  VERIFICATION CHECKLIST                                      │
│                                                             │
│  □ Set GEMINI_API_KEY in .env file                          │
│  □ Restart the application                                  │
│  □ Upload a clear PDF/image of a permit                     │
│  □ After upload, check the document on dashboard            │
│  □ Document name should be auto-filled (e.g., "PUC Cert")   │
│  □ Permit number, dates should be populated                 │
│  □ Click "Edit" to see all extracted fields                 │
│                                                             │
│  WITHOUT API KEY:                                           │
│  □ Upload still works → fields are empty → edit manually    │
│                                                             │
│  TROUBLESHOOTING:                                           │
│  □ Image too blurry? → AI may fail to read text             │
│  □ Wrong API key? → fields will be empty                    │
│  □ Network issue? → check internet connection               │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. STEP 4 — Thymeleaf Frontend Pages

### All Pages

```
┌──────────────────────────────────────────────────────────────┐
│  PAGE MAP                                                    │
│                                                              │
│  /login          → login.html          Login form            │
│  /signup         → signup.html         Registration form     │
│  /dashboard      → dashboard.html      All documents + timer │
│  /upload         → upload.html         File upload form      │
│  /profile        → profile.html        View your account     │
│  /profile/edit   → profile-edit.html   Edit your details     │
│  /expiring       → expiring.html       Expiring documents    │
│  /documents/{id}/edit → edit-document.html  Edit document    │
│  /admin          → admin.html          Admin user mgmt       │
│  /admin/users/{id} → admin-user-detail.html  User details    │
└──────────────────────────────────────────────────────────────┘
```

### Thymeleaf Syntax: Quick Reference

```
┌─────────────────────────────────────────────────────────────────────┐
│  THYMELEAF CHEAT SHEET                                              │
│                                                                     │
│  Print variable:       <span th:text="${user.name}">John</span>     │
│  Loop over list:       <tr th:each="d : ${documents}">              │
│  Conditional show:     <div th:if="${error}">Error!</div>           │
│  Conditional hide:     <div th:unless="${success}">...</div>        │
│  Create link:          <a th:href="@{/dashboard}">Home</a>          │
│  Form target:          <form th:action="@{/login}" method="post">   │
│  Bind form to object:  <form th:object="${signupRequest}">          │
│  Bind input field:     <input th:field="*{name}">                  │
│  Pre-fill value:       <input th:value="${user.email}">            │
│  Elvis operator:       <span th:text="${user.company} ?: '-'"></span>│
│  Dynamic URL param:    @{/users/{id}(id=${user.id})}               │
│                                                                     │
│  Inline JS:            th:inline="javascript"                       │
│  Replace fragment:     th:replace="~{fragments/header :: nav}"      │
└─────────────────────────────────────────────────────────────────────┘
```

### Page: Dashboard

> **File:** `templates/dashboard.html`

```
┌──────────────────────────────────────────────────────────────┐
│  NAVBAR                                                      │
│  [Smart Permit]   Profile  Expiring  Upload  [Admin*]  Logout│
│                                               *Admin only     │
├──────────────────────────────────────────────────────────────┤
│  DASHBOARD                                                   │
│                                                              │
│  ┌─────────────────────┐  ┌─────────────────────┐           │
│  │ PUC Certificate      │  │ Fire Safety Permit   │           │
│  │ Vehicle Document     │  │ Safety              │           │
│  │ Expires: 2026-01-15  │  │ Expires: 2026-05-01  │           │
│  │ 250d 6h 23m 15s      │  │ 365d 10h 45m 2s     │           │
│  │ [ACTIVE] ✓           │  │ [ACTIVE] ✓           │           │
│  │ [Edit] [Remove]      │  │ [Edit] [Remove]      │           │
│  └─────────────────────┘  └─────────────────────┘           │
│                                                              │
│  Table View:                                                 │
│  ┌─────────┬──────────┬──────────┬──────┬──────────┬───────┐│
│  │ Name    │ Permit # │ Authority│ Issue│ Expiry   │Status ││
│  ├─────────┼──────────┼──────────┼──────┼──────────┼───────┤│
│  │ PUC Cert│ MH03-123 │ RTO Mum  │01/24│ 01/26    │ACTIVE ││
│  └─────────┴──────────┴──────────┴──────┴──────────┴───────┘│
└──────────────────────────────────────────────────────────────┘
```

Key parts of the code:

```html
<!-- Admin button: ONLY visible to ROLE_ADMIN users -->
<a th:if="${user.role.name() == 'ROLE_ADMIN'}"
   th:href="@{/admin}" class="btn btn-outline-danger btn-sm">
   Admin
</a>

<!-- Document card -->
<div th:each="document : ${documents}">
    <h2 th:text="${document.documentName()}">Doc Name</h2>

    <!-- Status badge with color based on state -->
    <span th:classappend="${document.status().name() == 'EXPIRED'}
            ? ' text-bg-danger'
            : (${document.status().name() == 'EXPIRING_SOON'}
            ? ' text-bg-warning'
            : ' text-bg-success')"
          th:text="${document.status()}">STATUS</span>

    <!-- Live countdown timer (JavaScript) -->
    <p class="countdown" th:attr="data-expiry=${document.expiryDate()}">
        <span th:text="${document.remainingDays()}">0</span>d
        <span th:text="${document.remainingHours()}">0</span>h
        <span th:text="${document.remainingMinutes()}">0</span>m
    </p>
</div>
```

**JavaScript countdown (updates every second):**

```javascript
function renderCountdown(element) {
    const expiry = element.dataset.expiry;     // e.g., "2026-01-15"
    if (!expiry) { element.textContent = 'N/A'; return; }

    const target = new Date(expiry + 'T00:00:00');
    target.setDate(target.getDate() + 1);      // Midnight of expiry date
    const diff = target.getTime() - Date.now();

    if (diff <= 0) { element.textContent = 'Expired'; return; }

    const days = Math.floor(diff / 86400000);
    const hours = Math.floor((diff % 86400000) / 3600000);
    const minutes = Math.floor((diff % 3600000) / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);
    element.textContent = `${days}d ${hours}h ${minutes}m ${seconds}s`;
}

// Run every second
setInterval(() => document.querySelectorAll('.countdown')
    .forEach(renderCountdown), 1000);
```

### ✅ How to Verify Step 4 Works

```
┌───────────────────────────────────────────────────────────┐
│  VERIFICATION CHECKLIST                                    │
│                                                           │
│  □ Visit /login → see styled login page                   │
│  □ Click "Create one" → see signup page with form         │
│  □ After login → see dashboard with navbar                │
│  □ Click Profile → see your account details               │
│  □ Click Edit Profile → change name → Save → see update   │
│  □ Click Upload → see file upload form                    │
│  □ Click Expiring → see documents expiring in 30 days     │
│  □ Upload a doc → see it on dashboard with countdown      │
│  □ Countdown numbers tick every second                    │
│  □ Status badge color changes (green/yellow/red)          │
└───────────────────────────────────────────────────────────┘
```

---

## 6. STEP 5 — Admin Panel & User Management

### Files You Need

```
┌──────────────────────────────────────────────────────────────┐
│  STEP 5 FILES                                                │
│                                                              │
│  controller/DashboardController.java   Admin endpoints       │
│  controller/AdminController.java       REST stats endpoint   │
│  templates/admin.html                  Admin dashboard       │
│  templates/admin-user-detail.html      User detail view      │
│  config/DataInitializer.java           Creates admin account │
│  security/SecurityConfig.java          /admin/** = ADMIN only│
└──────────────────────────────────────────────────────────────┘
```

### Access Control

```
┌──────────────────────────────────────────────────────────────┐
│  SECURITY RULES                                              │
│                                                              │
│  REGULAR USER (ROLE_USER):                                   │
│    /dashboard  ✓    /upload  ✓    /profile  ✓               │
│    /admin      ✗    403 FORBIDDEN                            │
│    Admin button NOT visible in navbar                        │
│                                                              │
│  ADMIN USER (ROLE_ADMIN):                                    │
│    /dashboard  ✓    /upload  ✓    /profile  ✓               │
│    /admin      ✓    /admin/users/{id}  ✓                    │
│    Admin button visible (red) in navbar                      │
│    Can view/edit/delete ANY user's data                      │
└──────────────────────────────────────────────────────────────┘
```

### Admin Dashboard Page

> **File:** `templates/admin.html`

```
┌──────────────────────────────────────────────────────────────┐
│  ADMIN PANEL                                                 │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │  Total Users     │  │  Total Documents │                 │
│  │       42         │  │       157        │                 │
│  └──────────────────┘  └──────────────────┘                 │
│                                                              │
│  ALL USERS                                                   │
│  ┌────┬─────────┬───────────────┬──────────┬───────┬───────┐│
│  │ ID │ Name    │ Email         │ Company  │ Role  │Actions││
│  ├────┼─────────┼───────────────┼──────────┼───────┼───────┤│
│  │ 1  │ Admin   │ admin@test.com│ -        │ ADMIN │[View] ││
│  │ 2  │ Shubh   │ user@mail.com │ ABC Corp │ USER  │[View] ││
│  │    │         │               │          │       │[Del]  ││
│  └────┴─────────┴───────────────┴──────────┴───────┴───────┘│
└──────────────────────────────────────────────────────────────┘
```

### Controller: Admin Endpoints

> **File:** `controller/DashboardController.java`

```java
// Main admin dashboard: shows stats + all users table
@GetMapping("/admin")
public String adminStats(Model model) {
    model.addAttribute("usersCount", userRepository.count());
    model.addAttribute("documentsCount", documentRepository.count());
    model.addAttribute("users", userRepository.findAll());
    return "admin";  // → admin.html
}

// View any user's profile + their documents
@GetMapping("/admin/users/{id}")
public String adminViewUser(@PathVariable Long id, Model model) {
    User target = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    model.addAttribute("targetUser", target);
    model.addAttribute("documents", documentService.listForUser(target));
    return "admin-user-detail";  // → admin-user-detail.html
}

// Delete any user (and ALL their documents cascade)
@PostMapping("/admin/users/{id}/delete")
public String adminDeleteUser(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
    User target = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    userRepository.delete(target);  // Cascade deletes all their docs + files
    redirectAttributes.addFlashAttribute("success",
        "User '" + target.getEmail() + "' deleted.");
    return "redirect:/admin";
}
```

### Security Config: Admin Protection

> **File:** `security/SecurityConfig.java`

```java
.authorizeHttpRequests(auth -> auth
    // PUBLIC: anyone can access
    .requestMatchers("/", "/login", "/signup", "/api/auth/**").permitAll()

    // ADMIN ONLY: only ROLE_ADMIN can access /admin/**
    .requestMatchers("/admin/**").hasRole("ADMIN")

    // AUTHENTICATED: everything else needs login
    .anyRequest().authenticated()
)
```

> **Key Point:** The `hasRole("ADMIN")` check happens at the Spring Security level — BEFORE the controller method runs. Even if someone types `/admin` in the URL, Spring rejects them with a 403 error.

### ✅ How to Verify Step 5 Works

```
┌─────────────────────────────────────────────────────────────┐
│  VERIFICATION CHECKLIST                                      │
│                                                             │
│  TEST 1: Admin Access                                       │
│  □ Login as admin@test.com / admin123                       │
│  □ Navbar shows RED "Admin" button                          │
│  □ Click Admin → see stats cards + all users table          │
│                                                             │
│  TEST 2: View User Details                                  │
│  □ Click "View" on any user                                 │
│  □ See user's profile info + all their documents            │
│  □ Document countdown timers work                           │
│                                                             │
│  TEST 3: Delete User                                        │
│  □ Click "Delete" on a user → confirm dialog                │
│  □ User disappears from table                               │
│  □ User's documents also deleted from MySQL                 │
│  □ User's files also removed from uploads/ folder           │
│                                                             │
│  TEST 4: Regular User Blocked                               │
│  □ Login as a regular user                                  │
│  □ No "Admin" button in navbar                              │
│  □ Type /admin in URL → 403 Forbidden                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 7. Project Folder Structure

```
permitIQ/
│
├── .env                         ──► Secret keys (Gemini API, DB password)
├── pom.xml                      ──► Maven: libraries & build config
├── STEP_BY_STEP_GUIDE.md        ──► How to test each feature
├── CODE_EXPLANATION.md          ──► Every file explained in detail
│
├── data/                        ──► H2 database files (if used)
├── uploads/                     ──► Uploaded files stored here
│
├── src/main/java/com/compliance/dashboard/
│   │
│   ├── SmartPermitMonitoringSystemApplication.java  ──► APP STARTS HERE
│   │
│   ├── entity/                   ──► DATABASE TABLES
│   │   ├── User.java             ──► Users table
│   │   ├── Document.java         ──► Documents table
│   │   ├── Role.java             ──► ROLE_USER / ROLE_ADMIN
│   │   ├── Gender.java           ──► MALE / FEMALE / OTHER
│   │   └── DocumentStatus.java   ──► ACTIVE / EXPIRING_SOON / EXPIRED
│   │
│   ├── dto/                      ──► DATA CARRIERS (between browser & server)
│   │   ├── SignupRequest.java
│   │   ├── LoginRequest.java
│   │   ├── JwtResponse.java
│   │   ├── UserResponse.java
│   │   ├── DocumentResponse.java
│   │   ├── DocumentUpdateRequest.java
│   │   ├── DocumentExtractionResult.java
│   │   ├── CountdownResponse.java
│   │   └── ApiErrorResponse.java
│   │
│   ├── repository/               ──► DATABASE QUERIES (auto-generated SQL)
│   │   ├── UserRepository.java
│   │   └── DocumentRepository.java
│   │
│   ├── service/                  ──► BUSINESS LOGIC
│   │   ├── AuthService.java
│   │   ├── DocumentService.java
│   │   ├── FileStorageService.java
│   │   ├── OcrService.java
│   │   └── impl/
│   │       ├── AuthServiceImpl.java     ──► Register & login logic
│   │       ├── DocumentServiceImpl.java ──► Upload, edit, delete docs
│   │       ├── FileStorageServiceImpl.java ──► Save/delete files on disk
│   │       ├── OcrServiceImpl.java      ──► OCR (placeholder)
│   │       └── ReminderScheduler.java   ──► Auto-refresh expiry status
│   │
│   ├── ai/                       ──► GEMINI AI INTEGRATION
│   │   ├── GeminiService.java
│   │   └── GeminiServiceImpl.java
│   │
│   ├── timer/                    ──► COUNTDOWN CALCULATION
│   │   ├── CountdownService.java
│   │   └── CountdownServiceImpl.java
│   │
│   ├── controller/               ──► WEB PAGES & REST APIS
│   │   ├── AuthViewController.java     ──► /login, /signup pages
│   │   ├── AuthRestController.java     ──► /api/auth/signup, /login
│   │   ├── DashboardController.java    ──► ALL main web pages
│   │   ├── DocumentRestController.java ──► /api/documents/**
│   │   ├── UserRestController.java     ──► /api/users/me
│   │   └── AdminController.java        ──► /admin/stats
│   │
│   ├── security/                 ──► LOGIN, PASSWORDS, JWT
│   │   ├── SecurityConfig.java
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   └── UserPrincipal.java
│   │
│   ├── config/                   ──► APP CONFIGURATION
│   │   ├── AppConfig.java             ──► Password encoder, Swagger
│   │   ├── ApplicationProperties.java ──► Reads application.yml
│   │   └── DataInitializer.java       ──► Creates default admin
│   │
│   ├── exception/                ──► ERROR HANDLING
│   │   ├── BadRequestException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── FileStorageException.java
│   │   └── GlobalExceptionHandler.java
│   │
│   └── util/                     ──► HELPERS
│       └── SecurityUtil.java     ──► Get current logged-in user
│
├── src/main/resources/
│   ├── application.yml           ──► All configuration
│   └── templates/                ──► HTML PAGES (Thymeleaf)
│       ├── login.html
│       ├── signup.html
│       ├── dashboard.html
│       ├── upload.html
│       ├── edit-document.html
│       ├── profile.html
│       ├── profile-edit.html
│       ├── expiring.html
│       ├── admin.html
│       └── admin-user-detail.html
│
└── src/test/                     ──► TEST FILES
    └── SmartPermitMonitoringSystemApplicationTests.java
```

---

## 8. Entity Deep Dive

### Entity Relationships

```
┌──────────────────────────────────────────────────────────────┐
│  DATABASE RELATIONSHIPS                                       │
│                                                               │
│  ┌──────────────────┐         ┌──────────────────────────┐   │
│  │      USER        │ 1    *  │       DOCUMENT           │   │
│  │──────────────────│─────────│──────────────────────────│   │
│  │ id (PK)          │◄────────│ id (PK)                  │   │
│  │ name             │         │ user_id (FK → users.id)  │   │
│  │ email (UNIQUE)   │         │ document_name            │   │
│  │ password (ENCR)  │         │ permit_number            │   │
│  │ role             │         │ issue_date               │   │
│  │ created_at       │         │ expiry_date              │   │
│  └──────────────────┘         │ status (ACTIVE/EXPIRING/ │   │
│                               │         EXPIRED)         │   │
│                               │ file_path                │   │
│                               └──────────────────────────┘   │
│                                                               │
│  Cascade Type = ALL                                           │
│  → Delete User → Deletes ALL their Documents                  │
│  → Delete Document → Removes file from uploads/ folder        │
└──────────────────────────────────────────────────────────────┘
```

### Status Auto-Calculation

```
Document has expiryDate
        │
        ▼
CountdownService.statusFor(expiryDate)
        │
        ├── expiryDate < today
        │        →  EXPIRED (red badge)
        │
        ├── expiryDate ≤ today + 30 days
        │        →  EXPIRING_SOON (yellow badge)
        │
        └── expiryDate > today + 30 days
                 →  ACTIVE (green badge)
```

### Enums Explained

```java
// Role.java — controls WHAT a user can do
public enum Role {
    ROLE_USER,   // Sees only OWN documents, NO admin access
    ROLE_ADMIN   // Sees ALL documents, CAN delete users
}

// Gender.java — user profile field
public enum Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
}

// DocumentStatus.java — auto-updated by scheduler
public enum DocumentStatus {
    ACTIVE,         // Green  → Expiry far away
    EXPIRING_SOON,  // Yellow → Expires within 30 days
    EXPIRED         // Red    → Already expired
}
```

---

## 9. Security Deep Dive

### Authentication Flow (How Login Works)

```
┌─────────────────────────────────────────────────────────────┐
│  FORM LOGIN FLOW (Browser)                                  │
│                                                             │
│  1. User types email + password on /login page              │
│  2. POST /login with form data                              │
│  3. Spring Security intercepts                              │
│  4. Calls CustomUserDetailsService.loadUserByUsername(email)│
│  5. Finds user in MySQL                                     │
│  6. BCryptPasswordEncoder.matches(password, storedHash)     │
│  7. If match: creates session → redirects to /dashboard     │
│  8. If no match: redirects to /login?error=true             │
│                                                             │
│  JWT TOKEN FLOW (API / Postman)                             │
│                                                             │
│  1. POST /api/auth/login with JSON body                     │
│  2. Server returns JWT token in response                    │
│  3. Client stores token                                     │
│  4. For every API call, include:                            │
│     Authorization: Bearer <token>                           │
│  5. JwtAuthenticationFilter reads header                    │
│  6. Validates token, loads user from DB                     │
│  7. Sets user as "authenticated" for this request           │
└─────────────────────────────────────────────────────────────┘
```

### URL Access Rules

```
┌──────────────────────────────────────────────────────────────┐
│  URL                  │ PUBLIC │ AUTHENTICATED │ ADMIN ONLY │
│───────────────────────│────────│───────────────│────────────│
│  /                    │   ✓    │               │            │
│  /login               │   ✓    │               │            │
│  /signup              │   ✓    │               │            │
│  /api/auth/**         │   ✓    │               │            │
│  /api/iot/**          │   ✓    │               │            │
│  /swagger-ui/**       │   ✓    │               │            │
│  /css/**, /js/**      │   ✓    │               │            │
│───────────────────────│────────│───────────────│────────────│
│  /dashboard           │        │       ✓       │            │
│  /upload              │        │       ✓       │            │
│  /profile             │        │       ✓       │            │
│  /expiring            │        │       ✓       │            │
│  /documents/**        │        │       ✓       │            │
│  /api/documents/**    │        │       ✓       │            │
│  /api/users/me        │        │       ✓       │            │
│───────────────────────│────────│───────────────│────────────│
│  /admin/**            │        │               │     ✓      │
│  /admin/users/**      │        │               │     ✓      │
└──────────────────────────────────────────────────────────────┘
```

### Password Encryption

```
┌──────────────────────────────────────────────────────────────┐
│  BCrypt: ONE-WAY ENCRYPTION                                  │
│                                                              │
│  User types:    password123                                  │
│       │                                                      │
│       ▼                                                      │
│  BCrypt hashes: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p     │
│                 $92ldGxad68LJZdL17lhWy                       │
│       │                                                      │
│       ▼                                                      │
│  Stored in DB:  $2a$10$N9qo8uLOickgx2ZMRZoMye...            │
│                                                              │
│  CANNOT reverse: Nobody can get "password123" from hash      │
│                                                              │
│  Login check: BCrypt.matches("password123", storedHash)      │
│               → TRUE if original password matches            │
└──────────────────────────────────────────────────────────────┘
```

---

## 10. Full Code Reference

### `pom.xml` — Dependencies Explained

```xml
<dependencies>
    <!-- Web: @Controller, @RestController, HTTP handling -->
    <dependency>spring-boot-starter-webmvc</dependency>

    <!-- Database: @Entity, @Repository, JPA queries -->
    <dependency>spring-boot-starter-data-jpa</dependency>

    <!-- Security: login, logout, password encoding, roles -->
    <dependency>spring-boot-starter-security</dependency>

    <!-- Templates: Thymeleaf HTML rendering -->
    <dependency>spring-boot-starter-thymeleaf</dependency>

    <!-- Validation: @NotBlank, @Email, @Min, @Max -->
    <dependency>spring-boot-starter-validation</dependency>

    <!-- AI: Google Gemini SDK for document extraction -->
    <dependency>
        <groupId>com.google.genai</groupId>
        <artifactId>google-genai</artifactId>
    </dependency>

    <!-- JWT: Create and validate JSON Web Tokens -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
    </dependency>

    <!-- MySQL: Connect to MySQL database -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>

    <!-- Lombok: @Getter, @Setter, @Builder, etc. -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

    <!-- Swagger: API documentation UI at /swagger-ui.html -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
</dependencies>
```

### `application.yml` — Full Configuration

```yaml
spring:
  application:
    name: smart-permit-monitoring-system

  # ── MySQL Database ──
  datasource:
    url: jdbc:mysql://localhost:3306/permit_db?createDatabaseIfNotExist=true
    username: root
    password: 5256
    driver-class-name: com.mysql.cj.jdbc.Driver

  # ── Auto-create/update tables ──
  jpa:
    hibernate:
      ddl-auto: update        # Creates tables based on @Entity classes
    open-in-view: false

  # ── File upload limits ──
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

  # ── Template caching OFF during development ──
  thymeleaf:
    cache: false

server:
  port: 8080

app:
  jwt:
    secret: change-this-development-secret-key-with-at-least-32-characters
    expiration-millis: 86400000       # 24 hours

  upload:
    dir: uploads                       # Files stored here
    max-file-size-bytes: 10485760      # 10MB in bytes

  gemini:
    api-key: ${GEMINI_API_KEY}        # From .env file
    model: ${GEMINI_MODEL:gemini-2.5-flash}

  reminder:
    expiring-soon-days: 30             # "Expiring Soon" = within 30 days
    status-refresh-cron: 0 0 * * * *   # Every hour
    daily-reminder-cron: 0 0 9 * * *   # Every day at 9 AM
```

### `CountdownService` — How the Timer Works

```java
// CALL: countdownService.calculate(expiryDate)
// RETURNS: {remainingDays: 25, remainingHours: 6, remainingMinutes: 42, expired: false}

public CountdownResponse calculate(LocalDate expiryDate) {
    if (expiryDate == null) return zeroCountdown();

    // Calculate: midnight OF the expiry date +1 day
    // e.g., expiry = 2026-01-15 → target = 2026-01-16 00:00:00
    LocalDateTime expiryEnd = expiryDate.plusDays(1).atStartOfDay();

    // Time from NOW until that midnight
    Duration duration = Duration.between(LocalDateTime.now(), expiryEnd);

    if (duration.isNegative() || duration.isZero()) {
        return EXPIRED;  // remainingDays=0, remainingHours=0, expired=true
    }

    return new CountdownResponse(
        duration.toDays(),       // e.g., 25
        duration.toHoursPart(),  // e.g., 6
        duration.toMinutesPart(),// e.g., 42
        false                    // not expired
    );
}
```

---

## 11. How Data Flows Through the App

### Flow 1: User Registration

```
Browser ──► POST /signup ──► AuthViewController
   │                              │
   │                              ▼
   │                    AuthServiceImpl.register()
   │                        ├── Check: email exists? ──► YES → throw 400
   │                        ├── Encrypt password with BCrypt
   │                        ├── Set role = ROLE_USER
   │                        └── userRepository.save(user)
   │                              │
   │                              ▼
   │                         MySQL "users" table
   │                              │
   ▼                              ▼
Redirect to /login         Return 201 Created
with success message       (REST API)
```

### Flow 2: File Upload + AI Extraction

```
Browser ──► POST /upload ──► DashboardController
   │                              │
   │                              ▼
   │                    DocumentServiceImpl.upload()
   │                        │
   │                        ├──► FileStorageServiceImpl.store()
   │                        │       ├── Validate (size, type)
   │                        │       ├── Rename (UUID.pdf)
   │                        │       └── Save to uploads/ folder
   │                        │
   │                        ├──► OcrServiceImpl.extractText()
   │                        │       └── Returns "" (placeholder)
   │                        │
   │                        ├──► GeminiServiceImpl.extractDocumentData()
   │                        │       ├── Send image to Gemini API
   │                        │       ├── Parse JSON response
   │                        │       └── Return extracted fields
   │                        │
   │                        └──► documentRepository.save(document)
   │                                └── Save to MySQL "documents" table
   │
   ▼
Redirect to /dashboard
Document appears in list
```

### Flow 3: Admin Deletes User

```
Admin ──► POST /admin/users/{id}/delete ──► DashboardController
   │                                              │
   │                                              ▼
   │                                    userRepository.delete(user)
   │                                        │
   │                                        ▼
   │                              @OneToMany(cascade = ALL)
   │                              ├── DELETE FROM documents
   │                              │   WHERE user_id = ?
   │                              │       │
   │                              │       ▼
   │                              │   FileStorageService.delete()
   │                              │       └── Delete file from uploads/
   │                              │
   │                              └── DELETE FROM users
   │                                  WHERE id = ?
   │
   ▼
Redirect to /admin
"User deleted" message
```

---

## 12. Quick Reference: Key Annotations

```
┌──────────────────────────────────────────────────────────────────┐
│  SPRING / JPA ANNOTATIONS                                        │
│                                                                  │
│  @Entity              Class is a database table                  │
│  @Table(name="x")     Custom table name                          │
│  @Id                  Primary key field                          │
│  @GeneratedValue      Auto-increment ID                          │
│  @Column              Customize column (nullable, length, unique)│
│  @Enumerated          Store enum as string in DB                 │
│  @Lob                 Large text/blob field                      │
│  @ManyToOne           Foreign key relationship (N:1)             │
│  @OneToMany           Reverse side of relationship (1:N)         │
│  @PrePersist          Run code BEFORE saving to DB               │
│  @Repository          Database access layer                      │
│  @Service             Business logic layer                       │
│  @Controller          Returns HTML pages                         │
│  @RestController      Returns JSON (API)                         │
│  @GetMapping          Handle HTTP GET                            │
│  @PostMapping         Handle HTTP POST                           │
│  @PutMapping          Handle HTTP PUT                            │
│  @DeleteMapping       Handle HTTP DELETE                         │
│  @RequestMapping      Base URL prefix                            │
│  @RequestParam        Get query/form parameter                   │
│  @RequestBody         Get JSON body (REST)                       │
│  @ModelAttribute      Bind form to Java object                   │
│  @PathVariable        Get URL segment (/users/{id})              │
│  @Valid               Trigger validation annotations             │
│  @Bean                Create a Spring-managed object             │
│  @Configuration       Configuration class                        │
│  @Component           Generic Spring bean                        │
│  @Transactional       All-or-nothing DB operation                │
│  @Scheduled(cron="")  Run method on a schedule                   │
│                                                                  │
│  LOMBOK ANNOTATIONS                                              │
│                                                                  │
│  @Getter              Generate getter methods                    │
│  @Setter              Generate setter methods                    │
│  @Builder             Generate builder pattern                   │
│  @NoArgsConstructor   Generate empty constructor                 │
│  @AllArgsConstructor  Generate all-fields constructor             │
│  @RequiredArgsConstructor  Constructor for final/@NonNull fields │
│  @Slf4j               Generate logger field                      │
│                                                                  │
│  VALIDATION ANNOTATIONS                                          │
│                                                                  │
│  @NotBlank            Cannot be null/empty/whitespace            │
│  @NotNull             Cannot be null                             │
│  @Email               Must be valid email format                 │
│  @Size(min=,max=)     String length limits                       │
│  @Min(value)          Number minimum value                       │
│  @Max(value)          Number maximum value                       │
└──────────────────────────────────────────────────────────────────┘
```

---

## Quick Testing Flow

```
┌─────────────────────────────────────────────────────────────┐
│  START TO FINISH VERIFICATION                                │
│                                                             │
│  1.  Start MySQL + App → http://localhost:8080              │
│  2.  Register a new user at /signup                         │
│  3.  Login with new account at /login                       │
│  4.  Dashboard loads → "No documents" message               │
│  5.  Upload a PDF/image at /upload                          │
│  6.  Document card appears on dashboard                     │
│  7.  Countdown timer shows days/hours/minutes               │
│  8.  Edit the document → change name → save                 │
│  9.  View Profile → click Edit → change company → save      │
│  10. View Expiring page → documents within 30 days shown    │
│  11. Logout → login as admin@test.com / admin123            │
│  12. "Admin" button visible in navbar (red)                 │
│  13. Click Admin → all users listed                         │
│  14. Click "View" on a user → see their profile + docs      │
│  15. Click "Delete" on a user → user + docs removed         │
│                                                             │
│  ALL VERIFIED ✓                                             │
└─────────────────────────────────────────────────────────────┘
```

---

> **Document Version:** 1.0
> **Last Updated:** May 2025
> **Default Admin:** admin@test.com / admin123
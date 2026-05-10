# Android App Prompt for Android Studio AI

Build a complete Android app (Kotlin, Jetpack Compose, Material 3, minimum SDK 26) that acts as an **admin panel** for the Smart Permit Monitoring System backend. The app must connect to the Spring Boot server, auto-login as admin, and provide full CRUD operations on Users and Documents.

---

## 1. APP FLOW

### First Launch — Server Setup Screen
- Show a single screen with one input field: **Server Address** (e.g. `http://192.168.1.5:8080`)
- Below it, show pre-filled (non-editable) credentials: **Email:** `admin@test.com` | **Password:** `admin123`
- A "Connect" button that:
  1. Saves the server address to `DataStore` preferences
  2. Calls `POST /api/auth/login` with `{ "email": "admin@test.com", "password": "admin123" }`
  3. On success: stores the JWT token and user info, navigates to Main Dashboard
  4. On failure: shows error toast, stays on this screen
- On subsequent launches: skip this screen, use saved server address + token (auto-refresh if token expired)

### Main Dashboard
- Bottom navigation with 3 tabs: **Users** | **Documents** | **Settings**
- Top app bar shows "PermitIQ Admin" with server status indicator (green dot = connected)

---

## 2. USERS TAB — Full CRUD

### List Users
- Call `GET /api/users` — NOTE: This endpoint currently only returns `/me`. You must also call the existing admin web endpoint data. Here's how:
  - Use `GET /admin/stats` to get total counts (returns JSON: `{ "users": <count>, "documents": <count> }`)
  - The backend does NOT yet have a REST endpoint to list ALL users. **You must add one to the backend first** (see Section 7 below), OR use this workaround in the Android app:
    - Since the app is admin-only, call `GET /admin/users` (the web controller page) — but this returns HTML, not JSON.
    - **BEST APPROACH**: Add a new REST endpoint `GET /api/admin/users` to the backend (see Section 7). The Android app should call this.
- Display users in a `LazyColumn` with each item showing: **Name**, **Email**, **Role** badge, **Company**
- Floating Action Button (FAB) to "Add User"
- Swipe-to-delete on each user card (with confirmation dialog)
- Tap a user card to navigate to User Detail screen

### User Detail Screen
- Show all user fields:
  - **ID** (read-only)
  - **Name** (editable)
  - **Email** (read-only)
  - **Phone Number** (editable)
  - **Company** (editable)
  - **Age** (editable, number input)
  - **Gender** (dropdown: MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY)
  - **Role** (dropdown: ROLE_USER, ROLE_ADMIN)
  - **Created At** (read-only, formatted date)
- "Save" button calls `PUT /api/admin/users/{id}` (see Section 7)
- "Delete User" button (red, with confirmation) calls `DELETE /api/admin/users/{id}` (see Section 7)
- Section below: "Documents for this User" — list their documents (call `GET /api/admin/users/{id}/documents`)

### Create User Screen
- Form with fields: Name, Email, Password, Phone Number, Company, Age, Gender (dropdown)
- "Create" button calls `POST /api/auth/signup` with body:
```json
{
  "name": "string",
  "email": "string",
  "password": "string (min 8 chars)",
  "phoneNumber": "string (optional)",
  "company": "string (optional)",
  "age": 13-120,
  "gender": "MALE|FEMALE|OTHER|PREFER_NOT_TO_SAY"
}
```
- Note: This endpoint creates users with `ROLE_USER` by default. To set admin role, the backend needs updating (see Section 7).

---

## 3. DOCUMENTS TAB — Full CRUD

### List Documents (All Users)
- Call `GET /api/admin/documents` (see Section 7 — new endpoint needed)
- Display in a `LazyColumn` with each item showing:
  - **Document Name** (bold)
  - **Type** badge
  - **Status** chip (color-coded: green=ACTIVE, yellow=EXPIRING_SOON, red=EXPIRED)
  - **Expiry Date** with countdown (e.g. "Expires in 15d 3h")
  - **Owner Email** (small text below)
- Search bar at top to filter by document name
- FAB to "Upload Document"
- Tap a document to navigate to Document Detail screen

### Document Detail Screen
- Show all document fields:
  - **ID** (read-only)
  - **Document Name** (editable)
  - **Document Type** (editable)
  - **Permit Number** (editable)
  - **Issue Date** (editable, date picker)
  - **Expiry Date** (editable, date picker)
  - **Authority Name** (editable)
  - **Original File Name** (read-only)
  - **Upload Time** (read-only, formatted)
  - **Status** (read-only chip, color-coded)
  - **Remaining Days / Hours / Minutes** (read-only, countdown display)
  - **Expired** (read-only, yes/no)
- "Save" button calls `PUT /api/admin/documents/{id}` (see Section 7)
- "Delete Document" button (red, with confirmation) calls `DELETE /api/admin/documents/{id}` (see Section 7)

### Upload Document Screen
- **User selector** dropdown (list all users from `/api/admin/users`)
- **File picker** button — pick PDF/JPG/PNG from device
- Show selected file name
- "Upload" button calls `POST /api/admin/documents/upload?userId={userId}` (see Section 7) with multipart form data

---

## 4. SETTINGS TAB
- **Server Address** — editable, with "Test Connection" button
- **Admin Email** — read-only display
- **Admin Role** — read-only display
- **Stats Card** — User count + Document count (from `GET /admin/stats`)
- **Logout** button — clears token, navigates back to Server Setup screen
- **About** — app version, "PermitIQ Admin Panel v1.0"

---

## 5. BACKEND API REFERENCE (Existing Endpoints)

### Authentication
```
POST /api/auth/login
  Body: { "email": "string", "password": "string" }
  Response: { "tokenType": "Bearer", "accessToken": "jwt...", "userId": 1, "name": "Admin", "email": "admin@test.com", "role": "ROLE_ADMIN" }
```
```
POST /api/auth/signup
  Body: { "name": "string", "email": "string", "password": "string", "phoneNumber": "string?", "company": "string?", "age": 25, "gender": "MALE|FEMALE|OTHER|PREFER_NOT_TO_SAY" }
  Response: { "id": 1, "name": "...", "email": "...", "phoneNumber": "...", "company": "...", "age": 25, "gender": "...", "role": "ROLE_USER", "createdAt": "2025-01-01T00:00:00" }
```

### Current User
```
GET /api/users/me
  Headers: Authorization: Bearer <token>
  Response: { "id": 1, "name": "Admin", "email": "admin@test.com", "phoneNumber": null, "company": null, "age": 25, "gender": "PREFER_NOT_TO_SAY", "role": "ROLE_ADMIN", "createdAt": "2025-01-01T00:00:00" }
```

### Documents (per-user, requires auth — owner only)
```
GET /api/documents?page=0&size=10&q=searchQuery
  Headers: Authorization: Bearer <token>
  Response: Page<DocumentResponse>
```
```
POST /api/documents/upload  (multipart: file=...)
  Headers: Authorization: Bearer <token>
  Response: DocumentResponse
```
```
PUT /api/documents/{id}
  Headers: Authorization: Bearer <token>
  Body: { "documentName": "...", "documentType": "...", "permitNumber": "...", "issueDate": "2025-01-01", "expiryDate": "2026-01-01", "authorityName": "..." }
  Response: DocumentResponse
```
```
DELETE /api/documents/{id}
  Headers: Authorization: Bearer <token>
  Response: 204 No Content
```
```
GET /api/documents/expiring
  Headers: Authorization: Bearer <token>
  Response: List<DocumentResponse>
```

### Admin Stats
```
GET /admin/stats
  Headers: Authorization: Bearer <token>
  Response: { "users": 5, "documents": 12 }
```

### Document Response Structure
```json
{
  "id": 1,
  "documentName": "PUC Certificate",
  "documentType": "Environmental",
  "permitNumber": "PUC-2025-001",
  "issueDate": "2025-01-15",
  "expiryDate": "2026-01-15",
  "authorityName": "RTO Mumbai",
  "originalFileName": "puc_cert.pdf",
  "uploadTime": "2025-06-01T10:30:00",
  "status": "ACTIVE",
  "remainingDays": 250,
  "remainingHours": 8,
  "remainingMinutes": 42,
  "expired": false
}
```

### User Response Structure
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+91-9876543210",
  "company": "Acme Corp",
  "age": 30,
  "gender": "MALE",
  "role": "ROLE_USER",
  "createdAt": "2025-01-01T00:00:00"
}
```

### Error Response Structure
```json
{
  "timestamp": "2025-06-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email is already registered",
  "path": "/api/auth/signup",
  "validationErrors": { "email": "must be a valid email" }
}
```

---

## 6. DATA MODELS (Kotlin Data Classes)

### User
```kotlin
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val company: String?,
    val age: Int?,
    val gender: String,        // MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    val role: String,           // ROLE_USER, ROLE_ADMIN
    val createdAt: String       // ISO datetime
)
```

### Document
```kotlin
data class Document(
    val id: Long,
    val documentName: String,
    val documentType: String?,
    val permitNumber: String?,
    val issueDate: String?,     // yyyy-MM-dd
    val expiryDate: String?,    // yyyy-MM-dd
    val authorityName: String?,
    val originalFileName: String,
    val uploadTime: String,     // ISO datetime
    val status: String,         // ACTIVE, EXPIRING_SOON, EXPIRED
    val remainingDays: Long,
    val remainingHours: Long,
    val remainingMinutes: Long,
    val expired: Boolean
)
```

### Auth Response
```kotlin
data class AuthResponse(
    val tokenType: String,      // always "Bearer"
    val accessToken: String,    // JWT token
    val userId: Long,
    val name: String,
    val email: String,
    val role: String            // ROLE_ADMIN
)
```

---

## 7. BACKEND CHANGES REQUIRED (Add these endpoints BEFORE building the app)

The backend currently lacks admin-specific REST endpoints for managing ALL users and ALL documents. You must add these new endpoints to the Spring Boot backend:

### New file: `AdminRestController.java`
```java
package com.compliance.dashboard.controller;

import com.compliance.dashboard.dto.DocumentResponse;
import com.compliance.dashboard.dto.DocumentUpdateRequest;
import com.compliance.dashboard.dto.UserResponse;
import com.compliance.dashboard.entity.Document;
import com.compliance.dashboard.entity.DocumentStatus;
import com.compliance.dashboard.entity.User;
import com.compliance.dashboard.repository.DocumentRepository;
import com.compliance.dashboard.repository.UserRepository;
import com.compliance.dashboard.timer.CountdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final CountdownService countdownService;

    // ---- USERS ----

    @GetMapping("/users")
    public List<UserResponse> listAllUsers() {
        return userRepository.findAll().stream()
            .map(user -> UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .company(user.getCompany())
                .age(user.getAge())
                .gender(user.getGender())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build())
            .toList();
    }

    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .company(user.getCompany())
            .age(user.getAge())
            .gender(user.getGender())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .build();
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCompany(request.getCompany());
        user.setAge(request.getAge());
        user.setRole(request.getRole());
        userRepository.save(user);
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .company(user.getCompany())
            .age(user.getAge())
            .gender(user.getGender())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    // ---- DOCUMENTS ----

    @GetMapping("/documents")
    public List<DocumentResponse> listAllDocuments() {
        return documentRepository.findAll().stream()
            .map(doc -> toDocResponse(doc))
            .toList();
    }

    @GetMapping("/documents/{id}")
    public DocumentResponse getDocument(@PathVariable Long id) {
        Document doc = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return toDocResponse(doc);
    }

    @GetMapping("/users/{userId}/documents")
    public List<DocumentResponse> listUserDocuments(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return documentRepository.findByUserOrderByUploadTimeDesc(user).stream()
            .map(this::toDocResponse)
            .toList();
    }

    @PutMapping("/documents/{id}")
    public DocumentResponse updateDocument(@PathVariable Long id, @RequestBody DocumentUpdateRequest request) {
        Document doc = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        doc.setDocumentName(request.getDocumentName().trim());
        doc.setDocumentType(request.getDocumentType());
        doc.setPermitNumber(request.getPermitNumber());
        doc.setIssueDate(request.getIssueDate());
        doc.setExpiryDate(request.getExpiryDate());
        doc.setAuthorityName(request.getAuthorityName());
        doc.setStatus(countdownService.statusFor(request.getExpiryDate()));
        documentRepository.save(doc);
        return toDocResponse(doc);
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Document doc = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        documentRepository.delete(doc);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/documents/upload")
    public DocumentResponse uploadDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam("userId") Long userId
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return documentService.upload(user, file);
    }

    private DocumentResponse toDocResponse(Document doc) {
        var countdown = countdownService.calculate(doc.getExpiryDate());
        return DocumentResponse.builder()
            .id(doc.getId())
            .documentName(doc.getDocumentName())
            .documentType(doc.getDocumentType())
            .permitNumber(doc.getPermitNumber())
            .issueDate(doc.getIssueDate())
            .expiryDate(doc.getExpiryDate())
            .authorityName(doc.getAuthorityName())
            .originalFileName(doc.getOriginalFileName())
            .uploadTime(doc.getUploadTime())
            .status(countdownService.statusFor(doc.getExpiryDate()))
            .remainingDays(countdown.remainingDays())
            .remainingHours(countdown.remainingHours())
            .remainingMinutes(countdown.remainingMinutes())
            .expired(countdown.expired())
            .build();
    }
}
```

### New DTO: `UpdateUserRequest.java`
```java
package com.compliance.dashboard.dto;

import com.compliance.dashboard.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    @NotBlank @Size(max = 120)
    private String name;
    @Size(max = 30)
    private String phoneNumber;
    @Size(max = 160)
    private String company;
    private Integer age;
    @NotNull
    private Role role;
}
```

### Security Config Update
Add `/api/admin/**` to the ADMIN-only rules in `SecurityConfig.java`:
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```
Add this line right after the existing `.requestMatchers("/admin/**").hasRole("ADMIN")` line.

---

## 8. ANDROID APP ARCHITECTURE

### Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Networking**: Retrofit2 + OkHttp3 + Gson converter
- **Image Loading**: Coil (for document thumbnails if needed)
- **DI**: Manual DI (no Hilt/Koin — keep it simple)
- **Storage**: DataStore Preferences (for server address + token)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35

### Package Structure
```
com.permitiq.admin/
├── MainActivity.kt
├── PermitIQApp.kt              (Application class, sets up Retrofit)
├── data/
│   ├── model/
│   │   ├── User.kt
│   │   ├── Document.kt
│   │   ├── AuthResponse.kt
│   │   ├── LoginRequest.kt
│   │   ├── SignupRequest.kt
│   │   ├── DocumentUpdateRequest.kt
│   │   └── UserUpdateRequest.kt
│   ├── api/
│   │   ├── AuthApi.kt           (Retrofit interface for auth)
│   │   ├── AdminApi.kt          (Retrofit interface for admin CRUD)
│   │   └── DocumentApi.kt       (Retrofit interface for documents)
│   └── local/
│       └── AppPreferences.kt    (DataStore for server address + token)
├── repository/
│   ├── AuthRepository.kt
│   ├── UserRepository.kt
│   └── DocumentRepository.kt
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── navigation/
│   │   └── AppNavigation.kt     (NavHost with all routes)
│   ├── setup/
│   │   └── ServerSetupScreen.kt
│   ├── dashboard/
│   │   └── MainDashboardScreen.kt (Bottom nav: Users|Documents|Settings)
│   ├── users/
│   │   ├── UserListScreen.kt
│   │   ├── UserDetailScreen.kt
│   │   └── CreateUserScreen.kt
│   ├── documents/
│   │   ├── DocumentListScreen.kt
│   │   ├── DocumentDetailScreen.kt
│   │   └── UploadDocumentScreen.kt
│   └── settings/
│       └── SettingsScreen.kt
└── util/
    └── DateUtils.kt
```

### Retrofit Interfaces

```kotlin
interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): User
}

interface AdminApi {
    @GET("api/admin/users")
    suspend fun listUsers(): List<User>

    @GET("api/admin/users/{id}")
    suspend fun getUser(@Path("id") id: Long): User

    @PUT("api/admin/users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body request: UserUpdateRequest): User

    @DELETE("api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long)

    @GET("api/admin/documents")
    suspend fun listDocuments(): List<Document>

    @GET("api/admin/documents/{id}")
    suspend fun getDocument(@Path("id") id: Long): Document

    @GET("api/admin/users/{userId}/documents")
    suspend fun listUserDocuments(@Path("userId") userId: Long): List<Document>

    @PUT("api/admin/documents/{id}")
    suspend fun updateDocument(@Path("id") id: Long, @Body request: DocumentUpdateRequest): Document

    @DELETE("api/admin/documents/{id}")
    suspend fun deleteDocument(@Path("id") id: Long)

    @Multipart
    @POST("api/admin/documents/upload")
    suspend fun uploadDocument(
        @Part("userId") userId: Long,
        @Part file: MultipartBody.Part
    ): Document

    @GET("admin/stats")
    suspend fun getStats(): Map<String, Long>
}
```

### OkHttp Interceptor (auto-attach JWT to every request)
```kotlin
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenProvider()
        val authenticatedRequest = if (token != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else request
        return chain.proceed(authenticatedRequest)
    }
}
```

### DataStore Preferences
```kotlin
// Keys to store:
// SERVER_ADDRESS  — String  (e.g. "http://192.168.1.5:8080")
// AUTH_TOKEN       — String  (JWT access token)
// USER_ID          — Long
// USER_NAME        — String
// USER_EMAIL       — String
// USER_ROLE        — String
```

### Navigation Routes
```kotlin
sealed class Screen(val route: String) {
    object ServerSetup : Screen("server_setup")
    object MainDashboard : Screen("main_dashboard")
    object UserList : Screen("user_list")
    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: Long) = "user_detail/$userId"
    }
    object CreateUser : Screen("create_user")
    object DocumentList : Screen("document_list")
    object DocumentDetail : Screen("document_detail/{docId}") {
        fun createRoute(docId: Long) = "document_detail/$docId"
    }
    object UploadDocument : Screen("upload_document")
    object Settings : Screen("settings")
}
```

---

## 9. UI DESIGN SPECIFICATIONS

### Color Palette (Material 3, Dark-friendly)
- Primary: `#1565C0` (blue)
- On Primary: `#FFFFFF`
- Secondary: `#4CAF50` (green for ACTIVE status)
- Error: `#E53935` (red for EXPIRED / delete buttons)
- Warning: `#FF9800` (orange for EXPIRING_SOON)
- Surface: `#FAFAFA` (light) / `#1E1E1E` (dark)
- Background: `#FFFFFF` (light) / `#121212` (dark)

### Status Chips
| Status        | Background | Text Color |
|---------------|-----------|------------|
| ACTIVE        | #E8F5E9   | #2E7D32    |
| EXPIRING_SOON | #FFF3E0   | #E65100    |
| EXPIRED       | #FFEBEE   | #C62828    |

### Role Badges
| Role      | Background | Text Color |
|-----------|-----------|------------|
| ROLE_ADMIN | #E3F2FD   | #1565C0    |
| ROLE_USER  | #F5F5F5   | #616161    |

### Cards
- Elevated cards with `shape = RoundedCornerShape(12.dp)`
- `shadowElevation = 4.dp`
- Content padding `16.dp`
- Each list item card has a `Divider` or `HorizontalDivider` between items

### Server Setup Screen
- Center-aligned vertically
- App logo/icon at top (use a simple shield or document icon)
- "PermitIQ Admin" title (24sp, bold)
- Server address field with `http://` prefix icon
- Credentials display in a card (read-only, with lock icon)
- Large "Connect" button (full width, rounded, primary color)
- Loading spinner while connecting

### User List Screen
- Top: Search bar with magnifying glass icon
- Stats row: "Total Users: X" in a small chip
- Each user card: Name (title), Email (subtitle), Role badge (trailing), Company (caption)
- FAB: "Add User" with person-add icon

### Document List Screen
- Top: Search bar
- Filter chips: ALL | ACTIVE | EXPIRING_SOON | EXPIRED
- Each document card: Document Name (title), Type badge, Status chip, "Expires in Xd Xh" (caption), Owner email (small text)
- FAB: "Upload" with upload icon

### Empty States
- Users: "No users found. Tap + to add one." with illustration
- Documents: "No documents yet. Upload your first document." with illustration

### Pull-to-Refresh
- Both Users and Documents lists support `PullToRefreshBox`

---

## 10. ERROR HANDLING
- Network errors: Show Snackbar with "Connection failed. Check server address."
- 401 Unauthorized: Clear saved token, navigate to Server Setup screen
- 404 Not Found: Show toast "Resource not found"
- 400 Bad Request: Show validation error message from API response
- Timeout: 30 seconds connect timeout, 60 seconds read timeout (for file uploads)

---

## 11. IMPORTANT NOTES

1. **All API calls must include `Authorization: Bearer <token>` header** — use the OkHttp interceptor
2. **The server address is user-configurable** — build Retrofit dynamically when the address changes
3. **Default admin credentials**: email = `admin@test.com`, password = `admin123`
4. **File uploads**: Only PDF, JPG, PNG allowed (max 10MB) — match backend validation
5. **Dates**: Use `yyyy-MM-dd` format for issue/expiry dates in API calls
6. **JWT expiration**: 24 hours (86400000ms). If expired, re-login automatically
7. **The backend runs on port 8080** by default
8. **Android network security**: For HTTP (non-HTTPS) connections to local server, add `android:usesCleartextTraffic="true"` in AndroidManifest.xml and a `network_security_config.xml`
9. **Keep the app simple** — no Hilt, no Room, no complex architecture. Just Retrofit + DataStore + Compose
10. **All CRUD operations must show a success/error Snackbar after completion**

---

## 12. BACKEND ENTITY REFERENCE (for completeness)

### User Entity Fields
| Field       | Type             | Nullable | Constraints              |
|-------------|------------------|----------|--------------------------|
| id          | Long             | No       | Auto-generated           |
| name        | String(120)      | No       |                          |
| email       | String(160)      | No       | Unique                   |
| password    | String           | No       | BCrypt hashed            |
| phoneNumber | String(30)       | Yes      |                          |
| company     | String(160)      | Yes      |                          |
| age         | Integer          | Yes      |                          |
| gender      | Enum             | Yes      | MALE,FEMALE,OTHER,PREFER_NOT_TO_SAY |
| role        | Enum             | No       | ROLE_USER, ROLE_ADMIN (default: ROLE_USER) |
| createdAt   | LocalDateTime    | No       | Auto-set on persist      |

### Document Entity Fields
| Field           | Type             | Nullable | Constraints              |
|-----------------|------------------|----------|--------------------------|
| id              | Long             | No       | Auto-generated           |
| user_id         | Long             | No       | FK → users.id            |
| documentName    | String(180)      | No       |                          |
| documentType    | String(80)       | Yes      |                          |
| permitNumber    | String(120)      | Yes      |                          |
| issueDate       | LocalDate        | Yes      |                          |
| expiryDate      | LocalDate        | Yes      |                          |
| authorityName   | String(180)      | Yes      |                          |
| originalFileName| String           | No       |                          |
| storedFileName  | String           | No       | Unique                   |
| filePath        | String           | No       |                          |
| extractedText   | LONGTEXT         | Yes      | AI-extracted text        |
| uploadTime      | LocalDateTime    | No       | Auto-set on persist      |
| status          | Enum             | No       | ACTIVE, EXPIRING_SOON, EXPIRED |

---

## END OF PROMPT

Paste everything from Section 1 through Section 12 into Android Studio's AI assistant (Gemini/Copilot). The AI will generate the complete Android project. Make sure to first add the backend endpoints from Section 7 to your Spring Boot project.

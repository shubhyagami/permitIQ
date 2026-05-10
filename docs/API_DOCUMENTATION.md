# API Documentation

This document lists the REST API endpoints available in the system. These endpoints are used by the frontend interface (via AJAX or form submissions) and are also available for external clients.

## 1. Authentication APIs

### `POST /api/auth/signup`
- **Purpose:** Register a new user.
- **Access:** Public
- **Request Body (JSON):**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secretpassword",
  "company": "Tech Corp"
}
```
- **Response (201 Created):** User details.

### `POST /api/auth/login`
- **Purpose:** Authenticate a user and return a JWT.
- **Access:** Public
- **Request Body (JSON):**
```json
{
  "email": "john@example.com",
  "password": "secretpassword"
}
```
- **Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "john@example.com",
  "role": "ROLE_USER"
}
```

---

## 2. User APIs

### `GET /api/users/me`
- **Purpose:** Return the currently authenticated user's profile.
- **Access:** Requires valid JWT / authenticated session.
- **Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "1234567890",
  "company": "Tech Corp",
  "age": 30,
  "gender": "MALE",
  "role": "ROLE_USER",
  "createdAt": "2026-05-09T12:00:00"
}
```

> Note: This endpoint is available for API clients and is not directly rendered in the current website templates.

---

## 3. Document APIs

### `GET /api/documents`
- **Purpose:** List documents for the current user.
- **Query Parameters:**
  - `q` (optional): search query
  - `page` (optional, default `0`)
  - `size` (optional, default `10`)
- **Access:** Requires valid JWT / authenticated session.
- **Response (200 OK):** Page of documents in JSON.

### `POST /api/documents/upload`
- **Purpose:** Upload a document file and trigger AI extraction.
- **Access:** Requires valid JWT / authenticated session.
- **Request:** `multipart/form-data` with field `file`.
- **Response (201 Created):** Newly created document metadata.

### `PUT /api/documents/{id}`
- **Purpose:** Update document metadata.
- **Access:** Requires valid JWT / authenticated session.
- **Request Body (JSON):**
```json
{
  "documentName": "Updated Name",
  "documentType": "Permit",
  "permitNumber": "1234",
  "issueDate": "2026-01-01",
  "expiryDate": "2027-01-01",
  "authorityName": "Compliance Office"
}
```
- **Response (200 OK):** Updated document metadata.

### `DELETE /api/documents/{id}`
- **Purpose:** Delete a document and its stored file.
- **Access:** Requires valid JWT / authenticated session.
- **Response (204 No Content):** Document removed.

### `GET /api/documents/expiring`
- **Purpose:** Return documents that are expiring soon.
- **Access:** Requires valid JWT / authenticated session.
- **Response (200 OK):** List of expiring documents.

> Note: The current website UI does not directly call these REST API endpoints; they are available for frontend integration and future clients.

---

## 4. Admin APIs

### `GET /admin/stats`
- **Purpose:** Return basic application statistics.
- **Access:** Intended for administrative use.
- **Response (200 OK):**
```json
{
  "users": 12,
  "documents": 42
}
```

> Note: `/admin/stats` is a backend-only endpoint and is not linked from the current website UI.

---

## 5. Web UI Endpoints (HTML Returns)

These are not JSON APIs; they are the website pages users interact with directly.

- **`GET /login`**: Returns the login HTML page.
- **`GET /signup`**: Returns the signup HTML page.
- **`GET /dashboard`**: Returns the main dashboard HTML page.
- **`GET /upload`**: Returns the document upload page.
- **`GET /documents/{id}/edit`**: Returns the edit document page.
- **`POST /upload`**: Accepts a `multipart/form-data` file upload and redirects back to the dashboard.
- **`POST /documents/{id}/edit`**: Saves document metadata edits.
- **`POST /documents/{id}/delete`**: Deletes a document.

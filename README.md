# Smart Compliance Dashboard

A simple web application for managing compliance documents with AI-powered text extraction.

## Features

- User authentication (signup/login)
- Document upload and AI text extraction using Google Gemini
- Dashboard with document status and countdown timers
- Secure file storage

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+

### Setup
1. Clone or download the project
2. Set your Google Gemini API key:
   ```bash
   export GEMINI_API_KEY=your_api_key_here
   ```
   Or create a `.env` file in the project root:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```

### Run the Application
```bash
./mvnw spring-boot:run
```

The application will start on http://localhost:8080

### Build for Production
```bash
./mvnw clean package
java -jar target/smart-permit-monitoring-system-0.0.1-SNAPSHOT.jar
```

## Usage

1. Visit http://localhost:8080/signup to create an account
2. Login at http://localhost:8080/login
3. Upload documents via the dashboard
4. View extracted information and expiry countdowns

## Available Backend APIs

The project also includes backend REST APIs that are not directly used by the current website templates:

- `GET /api/users/me`
- `GET /api/documents`
- `POST /api/documents/upload`
- `PUT /api/documents/{id}`
- `DELETE /api/documents/{id}`
- `GET /api/documents/expiring`
- `GET /admin/stats`

These routes are available for integration with mobile clients or future frontend enhancements.

## Technology Stack

- Java 21, Spring Boot 3
- MySQL Database
- Thymeleaf templates
- Google Gemini AI
- Bootstrap 5 UI

*Other helpful guides:*
- [Deployment Guide](docs/DEPLOYMENT_GUIDE.md)
- [Screenshot Guide](docs/SCREENSHOT_GUIDE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)

---
# permitIQ

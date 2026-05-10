# IoT Smart Compliance Dashboard

Welcome to the **IoT Smart Compliance Dashboard** project! This repository contains a backend system built to manage compliance documents (like permits and licenses), extract data from them automatically using Artificial Intelligence, and trigger hardware buzzers (IoT devices) when documents are about to expire.

If you are a beginner looking to understand and rebuild this project from scratch, you are in the right place! This documentation is written step-by-step in simple English.

## 🚀 What Does This Project Do?

1. **User Authentication:** Users can sign up, log in, and securely manage their own documents.
2. **Document Upload:** Users can upload scanned documents (images or PDFs).
3. **AI Extraction:** The system uses the **Google Gemini AI API** to automatically read the uploaded documents and extract important details (like Permit Number, Issue Date, Expiry Date, etc.).
4. **Dashboard Management:** A clean web interface where users can view all their documents and see a real-time countdown until expiry.
5. **IoT Integration:** Hardware devices (like an ESP8266 or ESP32) can call the system's API to check if any document is expiring soon. If it is, the device will trigger a buzzer and display a warning.

## 🛠️ Technology Stack

Here are the main technologies used in this project:

- **Java 21:** The core programming language.
- **Spring Boot (v3+):** The main framework that makes building Java web applications much easier.
- **Spring Security & JWT:** Secures our APIs and manages user logins using "JSON Web Tokens".
- **Spring Data JPA & Hibernate:** Tools that let us talk to the database using Java code instead of writing raw SQL.
- **MySQL Database:** A relational database used to store our users and document records.
- **Thymeleaf:** A templating engine that allows us to create dynamic HTML web pages using data from the Java backend.
- **Google Gemini API:** The Artificial Intelligence engine we use to read text from images/PDFs.
- **Bootstrap 5:** A CSS framework used to make the web dashboard look professional and responsive.

## 📚 How to Use This Documentation

I have created a complete learning system for you. Please read the guides in the following order to build your understanding:

1. **[Step-By-Step Understanding Guide](STEP_BY_STEP_UNDERSTANDING_GUIDE.md):** Start here. It explains the high-level concepts (MVC, REST, JWT) in plain English.
2. **[Project Flow](PROJECT_FLOW.md):** Visual diagrams showing how data moves through the system.
3. **[Database Guide](DATABASE_GUIDE.md):** Explains how our data is structured and stored.
4. **[File Explanation Guide](FILE_EXPLANATION_GUIDE.md):** A detailed breakdown of what every important Java file does.
5. **[Security Guide](SECURITY_GUIDE.md):** How we protect user data and APIs using JWT.
6. **[AI Integration Guide](AI_INTEGRATION_GUIDE.md):** How we connect to Google Gemini to read documents.
7. **[IoT Guide](IOT_GUIDE.md):** How the hardware devices (ESP8266/ESP32) talk to our backend.
8. **[API Documentation](API_DOCUMENTATION.md):** A reference of all the REST API endpoints.
9. **[How to Rebuild from Scratch](HOW_TO_REBUILD_FROM_SCRATCH.md):** The ultimate tutorial to recreate this exact project yourself.

*Other helpful guides:*
- [Deployment Guide](DEPLOYMENT_GUIDE.md)
- [Screenshot Guide](SCREENSHOT_GUIDE.md)
- [Troubleshooting](TROUBLESHOOTING.md)

---
*Happy Learning! Dive into the code and don't be afraid to experiment.*

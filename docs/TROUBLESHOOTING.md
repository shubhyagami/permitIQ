# Troubleshooting Guide

If you are running the project and something breaks, don't panic! Check these common issues.

## 1. Port 8080 is already in use
**Error:** `Web server failed to start. Port 8080 was already in use.`
**Cause:** Another application (or an old frozen version of your app) is already using port 8080.
**Fix:** 
- Find the process using the port and kill it. 
- In Windows Command Prompt: `netstat -ano | findstr :8080` (Find the PID at the end), then `taskkill /F /PID <the_pid>`.
- Alternatively, open `application.yml` and change `server.port: 8080` to `8081`.

## 2. Gemini API Errors
**Error:** `500 Internal Server Error` when uploading a file. Logs show `GoogleGenAIException: Unauthorized` or `Bad Request`.
**Cause:** Your Gemini API Key is missing, expired, or incorrect.
**Fix:** Check your `.env` file in the root directory. Ensure it has:
`GEMINI_API_KEY=AIzaSy...`

## 3. Database Keeps Resetting
**Issue:** Every time you restart the Spring Boot application, all your users and documents disappear!
**Cause:** The database is MySQL, and Spring JPA is set to `create-drop`.
**Fix:** Look at `application.yml`. 
If you want data to survive restarts, ensure `spring.jpa.hibernate.ddl-auto: update` (not `create` or `create-drop`).

## 4. JWT "SignatureException" or Login Loops
**Issue:** You are logged in, but suddenly clicking a button redirects you to the login page again, or the logs show JWT errors.
**Cause:** The server was restarted. Because the JWT Secret Key (`jwt.secret`) in this project is stored in memory, restarting the server might change the secret (if generated randomly), making old cookies invalid.
**Fix:** Simply clear your browser cookies and log in again. To permanently fix, set a hardcoded secret in `application.yml`.

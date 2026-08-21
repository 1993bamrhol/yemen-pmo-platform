# Yemen PMO Platform

This repository contains a government portal platform for Yemen with an Arabic-first public frontend and a Java Spring Boot backend.

## Stack
- Java 21
- Spring Boot 3.x
- PostgreSQL
- Flyway
- React / Next.js / TypeScript
- Docker Compose
- Maven

## Local development

1. Copy the environment template:
   - `copy .env.example .env` on Windows
   - or `cp .env.example .env` on Linux/macOS
2. Review the values in `.env` and replace secrets, especially:
   - `POSTGRES_PASSWORD`
   - `SPRING_DATASOURCE_PASSWORD`
   - `SECURITY_JWT_SECRET`
   - `ADMIN_PASSWORD`
   - `APP_CORS_ALLOWED_ORIGINS`
   The JWT secret must be Base64- or Base64URL-encoded and decode to at least 32 random bytes.
3. Start the full stack:
   - `docker compose up --build`
4. Access the app:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - PostgreSQL: localhost:5432

## Initial administrator
- The username defaults to `admin` and can be changed with `ADMIN_USERNAME`.
- There is no default password. Set a strong, unique `ADMIN_PASSWORD` before the first start.
- The password is used only when the administrator is created; changing the environment variable does not reset an existing account.

## Production checklist
- Use a strong JWT secret and unique DB credentials.
- Restrict `APP_CORS_ALLOWED_ORIGINS` to the exact deployed frontend origin.
- Keep environment variables outside the source repository.
- Run database migrations automatically through Flyway.
- Terminate TLS at the reverse proxy and expose only the frontend and required API routes.
- Prefer a real Docker-enabled host for final runtime validation.

## Notes
The project was validated locally using backend tests and a frontend production build. Full Docker startup requires an environment with the Docker daemon available.

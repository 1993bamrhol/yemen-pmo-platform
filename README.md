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
3. Start the full stack:
   - `docker compose up --build`
4. Access the app:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - PostgreSQL: localhost:5432

## Default admin credentials
- Username: `admin`
- Password: `Admin@123`

## Production checklist
- Use a strong JWT secret and unique DB credentials.
- Keep environment variables outside the source repository.
- Run database migrations automatically through Flyway.
- Prefer a real Docker-enabled host for final runtime validation.

## Notes
The project was validated locally using backend tests and a frontend production build. Full Docker startup requires an environment with the Docker daemon available.

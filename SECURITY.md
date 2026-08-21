# Security policy

## Reporting a vulnerability

Do not open a public issue for a suspected vulnerability or include production data, credentials, tokens, or personal information in a report. Report the issue through the private security contact designated by the Yemen Prime Minister's Office deployment owner.

Include the affected version, a concise reproduction, the expected impact, and any relevant logs after removing secrets and personal data.

## Deployment requirements

- Generate unique database credentials and an `ADMIN_PASSWORD` for every environment.
- Set `SECURITY_JWT_SECRET` to a Base64 or Base64URL value that decodes to at least 32 cryptographically random bytes.
- Set `APP_CORS_ALLOWED_ORIGINS` to the exact HTTPS frontend origins; do not use a wildcard in production.
- Keep `.env` files and secrets outside version control and rotate any credential that may have been disclosed.
- Use HTTPS, security headers, request-rate limiting, centralized audit logs, encrypted backups, and restricted database/network access at the deployment boundary.
- Run dependency, container, and source-code security scans before each release.

This repository is a foundation, not a production accreditation. A government deployment still requires an independent security review, penetration test, privacy assessment, backup/restore exercise, and operational approval.

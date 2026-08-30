# Phase 5D.3 — Local Runtime Alignment & End-to-End Verification

**Date:** 2026-08-30

**Branch:** `main`

**Source checkpoint:** `2e3266d5e7ebee2b2335924a1c92298069eb3960`

**Scope:** Local Docker/runtime alignment and read-only end-to-end verification

**Recommendation:** **READY TO COMMIT**, with the runtime-data and streamed-404 conditions recorded below

## Executive result

The local Docker runtime was stale relative to the checked-out source. The backend image predated migrations V7–V9 and the frontend image predated the latest Entity frontend checkpoint. The persistent database was healthy but its Flyway schema history stopped at V6.

The standard project command `docker compose up -d --build` rebuilt and recreated only the application containers. No database volume was deleted, no Flyway `clean` was run, and no seed or production-like record was added. On startup, Flyway safely migrated the existing database from V6 to V9 and Hibernate initialized successfully against the migrated schema.

The aligned runtime now serves the Entity and Government Services contracts expected by Phase 5D.1 and Phase 5D.2. The real database contains no Government Service records and no editorially verified content, so an end-to-end success rendering of a real Service Detail record and populated Homepage quick services cannot be validated yet. Empty and not-found behavior is correct and no substitute data was introduced.

## Scope boundaries preserved

- No frontend, backend, database migration, or Figma source was changed.
- No Homepage activation, Unified Search, or Open Data work was started.
- No production-like service fixture or migration seed was created.
- No database reset, volume deletion, schema clean, or destructive operation was used.
- Existing `backend/**/target/**` artifacts were not cleaned and remain outside the intended documentation change.
- Unified Content feature flags and graduation states were not changed.

## Runtime mismatch: cause and evidence

### Before alignment

| Area | Observed state |
|---|---|
| Git source | Phase 5D.2 checkpoint `2e3266d5e7ebee2b2335924a1c92298069eb3960` |
| Backend Docker image | Stale image created on 2026-08-24, before V7–V9 were present in the running artifact |
| Frontend Docker image | Stale image created on 2026-08-23, before the current Entity frontend integration |
| Database container | Existing healthy PostgreSQL 16 container and persistent volume |
| Flyway history | V1–V6 only |
| V8 schema | Entity profile columns were absent |
| V9 schema | `government_services` was absent |

The mismatch was caused by application containers still running older built images. It was not caused by a failed migration or a corrupted database.

### Alignment action

The project-standard rebuild was used:

```text
docker compose up -d --build
```

This action rebuilt the backend and frontend images and recreated those two containers. The existing database container and its data volume were preserved.

### After alignment

| Service | Result |
|---|---|
| Backend image | Rebuilt locally; image ID prefix `b08088b33ef2` |
| Frontend image | Rebuilt locally; image ID prefix `e13e728c38f3` |
| Database | Same PostgreSQL 16 container/volume, healthy |
| Backend runtime | Spring Boot 3.5.5 on Java 21.0.12 |
| Frontend runtime | Next.js 16.3.2 |
| Flyway | Validated 9 migrations and advanced V6 → V9 |
| Hibernate | ORM 6.6.26.Final; `EntityManagerFactory` initialized successfully |

## Flyway and schema result

The database now records every migration V1–V9 with `success = true`:

| Version | Description | Result |
|---|---|---|
| V1 | schema | PASS |
| V2 | admin content | PASS |
| V3 | support requests | PASS |
| V4 | support requests status | PASS |
| V5 | platform foundation | PASS |
| V6 | unified content foundation | PASS |
| V7 | editorial verification provenance | PASS |
| V8 | government entity profile | PASS |
| V9 | government services | PASS |

Startup evidence showed:

- `Successfully validated 9 migrations`.
- Current schema before migration: V6.
- V7, V8, and V9 applied in order.
- `Successfully applied 3 migrations ... now at version v9`.
- Hibernate/JPA initialized after migration without schema validation failure.

No existing content was promoted by V7. Current content state remains:

| Content type | Publication state | Editorial verification | Count |
|---|---|---:|---:|
| NEWS | PUBLISHED | UNVERIFIED | 3 |
| ANNOUNCEMENT | PUBLISHED | UNVERIFIED | 3 |
| DECISION | PUBLISHED | UNVERIFIED | 3 |
| DOCUMENT | PUBLISHED | UNVERIFIED | 3 |

The `government_services` table exists and contains **0** rows. This is the expected conservative result because V9 contains no production-like service seed.

## Backend API verification

All checks used the real aligned local backend at `http://localhost:8081`.

| Check | Result | Evidence |
|---|---|---|
| Backend health | PASS | `GET /actuator/health` → 200 |
| Entity list | PASS | `GET /api/v1/entities` → 200 using the V8 response shape |
| Entity detail by ID | PASS | Existing public entity → 200 |
| Entity detail by locator | PASS | `GET /api/v1/entities/by-slug/prime-ministers-office/prime-ministers-office` → 200 with canonical path `/prime-ministers-office` |
| Missing entity | PASS | Missing locator → unified API 404 with `RESOURCE_NOT_FOUND` |
| Entity services | PASS | Existing entity → 200 with zero items |
| Service catalog | PASS | `GET /api/v1/services` → 200 with an empty paginated result |
| Service catalog entity filter | PASS | `GET /api/v1/services?entityId={entityId}` → 200 with zero items |
| Service detail by UUID, missing | PASS | Missing UUID → unified API 404 |
| Service detail by slug, missing | PASS | Missing slug → unified API 404 without internal error leakage |
| Service detail success | NOT EXERCISED IN LOCAL DATA | No `PUBLISHED + VERIFIED` service exists; no fixture was inserted into the runtime database |

The automated backend integration suite provides isolated success-path coverage with test-scoped data; the persistent local database was deliberately left unchanged.

## Frontend route verification

The rebuilt frontend was tested at `http://localhost:3000`.

| Route/state | Result | Evidence |
|---|---|---|
| `/prime-ministers-office` | PASS | Real Entity API data rendered; Arabic title present; services empty state shown |
| Generic Entity alias | PASS | Browser navigation from `/prime-ministers-office/prime-ministers-office` ended at canonical `/prime-ministers-office` |
| Entity services empty | PASS | Entity profile remained available and showed the explicit verified-services empty state |
| Missing Entity | PASS WITH HTTP NOTE | Localized unavailable/not-found presentation, no internal message, no console error |
| Missing Service | PASS WITH HTTP NOTE | Localized unavailable/not-found presentation, no internal message, no console error |
| Service Detail success | NOT EXERCISED IN LOCAL DATA | No verified service exists; no runtime fixture was created |
| Isolated services failure | COVERED BY 5D.2 + AUTOMATED TESTS | Entity data remains independent from the services request; aligned runtime currently returns a valid empty list rather than an error |

### Streamed not-found HTTP semantics

Both missing frontend routes rendered the intended Arabic not-found state and included `noindex`, but their streamed Next.js HTML response status was **200**, not a strict HTTP 404:

- `/ministries/not-a-real-entity` → HTTP 200 + `noindex` + localized Entity unavailable state.
- `/services/not-a-real-service` → HTTP 200 + `noindex` + localized Service unavailable state.

The backend APIs themselves return the required unified HTTP 404. This frontend behavior is consistent with a streamed App Router response where `notFound()` is reached after the shell begins streaming, but it remains a soft-404/SEO consideration. A product decision is required before public launch if strict HTTP 404 status is mandatory; no routing refactor was attempted in this runtime-only phase.

## Responsive, RTL, accessibility, and console checks

The Entity route was inspected in the in-app browser at 320, 360, 1024, and 1440 CSS pixels.

| Check | Result |
|---|---|
| Document language/direction | `lang="ar"`, `dir="rtl"` |
| Main landmark | `#main-content` present |
| Skip link | Present and targets `#main-content`; existing implementation unchanged |
| Entity and empty state | Visible at all tested widths |
| Horizontal overflow | None at 320, 360, 1024, or 1440 |
| Browser console | No warnings or errors during successful Entity, Entity not-found, Service not-found, and canonical navigation checks |
| Reduced motion/focus foundations | Existing approved Public Shell/Foundations remained unchanged |

No runtime service record was available for full visual verification of long service steps or populated Service Detail sections. Those cases remain covered by component design constraints and automated integration/unit behavior, not by a real local official record.

## Unified Content and editorial compatibility

The authenticated routing-status check remained unchanged after the rebuild:

| Type | configuredForUnified | shadowReady | effectiveSource | comparisonError | automatic fallbacks |
|---|---:|---:|---|---|---:|
| NEWS | true | true | UNIFIED | null | 0 |
| ANNOUNCEMENT | true | true | UNIFIED | null | 0 |
| DECISION | false | true | LEGACY | null | 0 |
| DOCUMENT | false | true | LEGACY | null | 0 |

Editorial verification behavior also remained conservative: all 12 existing published content records are still `UNVERIFIED`; none became `VERIFIED` during migration or rebuild.

## Verification commands and results

### Backend

- `mvn test`: **PASS**.
- Maven reactor: **BUILD SUCCESS** across all 14 modules.
- 31 test suites, **100 tests**, 0 failures, 0 errors, 0 skipped.
- Relevant suites:
  - `ApiV1ErrorContractIntegrationTest`: 10/10.
  - `EditorialVerificationIntegrationTest`: 4/4.
  - `GovernmentEntityProfileIntegrationTest`: 4/4.
  - `GovernmentServiceIntegrationTest`: 7/7.
  - Unified Content integration suites: PASS.
- Test Flyway validation: 9 migrations validated; test schema already at V9.
- Docker backend package/build: PASS as part of `docker compose up -d --build`.

### Frontend

- `npm run lint`: **PASS**.
- `npx tsc --noEmit`: **PASS**.
- `npm run build`: **PASS**.
- Next.js production route generation includes `/[publicPathSegment]/[slug]`, `/prime-ministers-office`, `/services`, and `/services/[slug]`.
- Browser/runtime checks: PASS subject to the no-data and streamed-404 notes above.

## Git/local/Docker discrepancy after alignment

| Layer | Final state |
|---|---|
| Git source | Current at Phase 5D.2 checkpoint |
| Docker backend | Rebuilt from current source; schema-aware through V9 |
| Docker frontend | Rebuilt from current source; Entity and Service routes present |
| Persistent database | Preserved and migrated in place to V9 |
| Source changes from alignment | None |
| Intended repository change in this phase | This documentation file only |

Pre-existing generated `backend/**/target/**` changes and historical design/review artifacts remain in the working tree and must remain excluded from any future checkpoint commit.

## Remaining blockers and readiness

### Runtime alignment

**READY.** Git, Docker application images, Flyway, Hibernate, API contracts, and frontend routes now agree on V1–V9.

### Homepage quick services activation

**HOLD on content availability.** The runtime and contracts are ready, but the service catalog contains zero `PUBLISHED + VERIFIED` records. Activating the section now would only produce an empty state. A real officially sourced service record must be created and approved through the supported administration path before populated runtime/visual validation.

### Homepage verified editorial content

**HOLD on editorial approval.** The technical verification mechanism is active, but every existing published record is still `UNVERIFIED`.

### Service Detail and Entity runtime

**READY WITH CONDITIONS.** Empty/not-found and real Entity behavior are validated. Full Service Detail visual/runtime validation remains pending one legitimate verified service. Strict frontend HTTP 404 status should also be decided before public launch; current streamed responses are soft 404s with `noindex`.

## Final recommendation

**READY TO COMMIT** this runtime evidence document only.

The local runtime mismatch is resolved without destructive database action or new production data. No feature source change is required for alignment. Do not activate Homepage service/editorial sections until genuine records meet their respective verification rules. Track the streamed frontend 200/not-found behavior as a launch-readiness decision rather than silently treating it as a strict HTTP 404.

# ANNOUNCEMENT Canary Activation Report

> **Status:** Activation gates `PASS` — local Docker canary active; scheduled monitoring not authorized
> **Canary start:** `2026-08-26T12:56:09Z` (`2026-08-26 15:56:09` Asia/Riyadh)
> **Scope:** ANNOUNCEMENT read compatibility in local Docker only
> **Baseline commit:** `5e896be3691c22f8380e83f5e5d02d47145fb741`
> **Baseline file SHA-256:** `4443C02AE297DB9D9AF13705CE2310BC2E7A64B419440F891901A6E7EF37E054`

## 1. Activation outcome

All approved ANNOUNCEMENT activation gates passed. ANNOUNCEMENT is now configured, shadow-ready, and effectively `UNIFIED` in the local Docker backend. NEWS remained `UNIFIED`; DECISION and DOCUMENT remained `LEGACY`. Five ANNOUNCEMENT compatibility smoke requests matched the frozen baseline exactly, automatic fallbacks remained zero, the live shadow report remained ready at 12/12 mappings with zero differences, and no health or database anomaly was found.

The canary clock started only after the smoke, database, log, routing, and final shadow checks passed at `2026-08-26T12:56:09Z`.

This is not production approval, ANNOUNCEMENT graduation, monitoring authorization, write cutover, or authorization for DECISION/DOCUMENT.

## 2. Baseline freeze completed before activation

The approved baseline was committed before the local flag changed:

- commit: `5e896be3691c22f8380e83f5e5d02d47145fb741`;
- subject: `docs: freeze announcement canary baseline`;
- committed scope: `docs/architecture/ANNOUNCEMENT_CANARY_BASELINE.md` only;
- frozen file SHA-256: `4443C02AE297DB9D9AF13705CE2310BC2E7A64B419440F891901A6E7EF37E054`.

The baseline was not regenerated or edited after activation.

## 3. Local activation change

Only the local Docker environment was changed:

| Setting | Activated value |
|---|---|
| `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_NEWS_ENABLED` | `true` |
| `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_ANNOUNCEMENTS_ENABLED` | `true` |
| `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_DECISIONS_ENABLED` | `false` |
| `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_DOCUMENTS_ENABLED` | `false` |
| `FEATURES_UNIFIED_CONTENT_BACKFILL_APPLY_ENABLED` | `false` |

Docker Compose rendered those exact values before deployment. Only `backend` was force-recreated with `--no-deps`; PostgreSQL and frontend were not recreated. No image build, migration, database mutation, production configuration change, or write-path change occurred.

## 4. Activation smoke matched the frozen contracts

Smoke started at `2026-08-26T12:19:17.143Z`. Exactly five counted ANNOUNCEMENT compatibility requests were issued: two list requests and three representative detail requests.

| Probe | HTTP | SHA-256 | Baseline match | Latency |
|---|---:|---|---|---:|
| `GET /api/announcements` #1 | 200 | `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` | Yes | 34.771ms |
| `GET /api/announcements` #2 | 200 | `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` | Yes | 24.234ms |
| `GET /api/announcements/1` #1 | 200 | `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` | Yes | 37.619ms |
| `GET /api/announcements/1` #2 | 200 | `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` | Yes | 21.137ms |
| `GET /api/announcements/1` #3 | 200 | `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` | Yes | 19.729ms |
| `GET /api/announcements/999` | 404 | Status contract only | Yes | 25.933ms |
| `GET /api/portal/home` | 200 | `43CCE4E9882DB254AB6AE219B038A187F074D8BE8E73A837BA7CE0E2B3263BAC` | Yes | 46.861ms |

- Counted compatibility 5xx responses: 0/5.
- Hash mismatches: 0.
- Missing numeric identifier retained 404.
- Portal home is an isolation guard only; ANNOUNCEMENT has no portal-home projection.
- Health returned `UP`/HTTP 200; initial smoke health latency was 12.351ms.

## 5. Routing isolation passed before and after smoke

The post-restart status was correct before the first compatibility probe and remained correct after smoke. Final verification at the canary start time reported:

| Content type | configuredForUnified | shadowReady | effectiveSource | Legacy requests | Unified requests | Automatic fallbacks |
|---|---:|---:|---|---:|---:|---:|
| NEWS | true | true | `UNIFIED` | 0 | 1 | 0 |
| ANNOUNCEMENT | true | true | `UNIFIED` | 0 | 5 | 0 |
| DECISION | false | true | `LEGACY` | 1 | 0 | 0 |
| DOCUMENT | false | true | `LEGACY` | 1 | 0 | 0 |

The one NEWS unified request and one DECISION/DOCUMENT legacy request came from the portal-home isolation probe. Every fallback-reason map was empty and `comparisonError=null`.

## 6. Final live shadow remained ready

The final protected read-only shadow report returned:

- `reconciliationReady=true`;
- `readyForCanary=true`;
- 12 legacy public sources and 12 mapped unified items;
- zero global differences;
- ANNOUNCEMENT: 3 legacy, 3 mapped, 3 unified published;
- ANNOUNCEMENT additional unified items: 0;
- ANNOUNCEMENT count, order, and field parity: true;
- ANNOUNCEMENT `readyForCanary=true`;
- existing portal-home projection: 3/3 sections ready.

## 7. Health and database-pressure gate passed

PostgreSQL snapshots bracketed the smoke traffic:

| Measure | Before (`12:18:27Z`) | After (`12:19:25Z`) | Delta |
|---|---:|---:|---:|
| Database connections | 3 | 3 | 0 |
| Transactions committed | 1,023 | 1,064 | +41 |
| Transactions rolled back | 0 | 0 | 0 |
| Blocks read | 428 | 428 | 0 |
| Buffer hits | 37,157 | 39,701 | +2,544 |
| Temporary files | 0 | 0 | 0 |
| Temporary bytes | 0 | 0 | 0 |
| Deadlocks | 0 | 0 | 0 |
| Waiting locks | 0 | 0 | 0 |
| Database size | 9,346,071 bytes | 9,346,071 bytes | 0 |

Post-smoke container snapshot:

- backend: 0.43% CPU, 442.2MiB memory;
- PostgreSQL: 0.00% CPU, 42.41MiB memory.

A bounded case-insensitive scan of recent backend/database logs found no `ERROR`, `FATAL`, deadlock, timeout, connection failure, or Hikari pool failure/error match.

## 8. Canary window and approval boundary

- Verified start: `2026-08-26T12:56:09Z`.
- Earliest time gate: `2026-08-27T12:56:09Z`.
- Traffic gate: at least 100 successful ANNOUNCEMENT unified compatibility requests.
- Starting unified count: 5 from activation smoke.
- Both gates are required before an exit review; neither alone is sufficient.

Current decision: **ANNOUNCEMENT CANARY ACTIVE LOCALLY — SCHEDULED MONITORING `HOLD`**.

No scheduled monitoring automation was created. No periodic probes are authorized by this activation approval. A separate explicit owner decision is required before monitoring is scheduled or performed beyond owner-requested checks.

ANNOUNCEMENT is not graduated. Do not activate DECISION or DOCUMENT, start write cutover, deploy to production, or infer production readiness from this local activation.

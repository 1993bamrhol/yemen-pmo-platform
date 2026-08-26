# ANNOUNCEMENT Canary Baseline

> **Status:** Frozen for owner review — ANNOUNCEMENT activation remains `HOLD`
> **Baseline capture window:** `2026-08-26T12:02:24Z` to `2026-08-26T12:05:52Z`
> **Contract/performance capture:** `2026-08-26T12:03:31.717Z`
> **Scope:** Local Docker read compatibility only
> **Approved plan:** `ANNOUNCEMENT_CANARY_PLAN.md`

## 1. Technical summary

Every pre-activation evidence gate passed in the current local Docker environment. ANNOUNCEMENT remained configured `false` and effectively `LEGACY` throughout capture. The legacy list and representative detail contracts were stable across 200 measured responses, with zero 5xx responses and zero hash mismatches. Live shadow comparison remained ready at 12/12 mappings and zero differences. No lock, deadlock, rollback, temporary-file, database-size, pool, or error-log pressure signal was observed.

This baseline does not activate ANNOUNCEMENT and is not an activation decision. NEWS remains `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remain `LEGACY`. No feature flag, database data/schema, write path, production environment, or monitoring automation changed.

| Pre-activation gate | Evidence | Result |
|---|---|---|
| Full backend test suite | 75 tests; 0 failures, 0 errors, 0 skipped | **PASS** |
| Backend/PostgreSQL health | Backend health HTTP 200; PostgreSQL container healthy | **PASS** |
| Four-type routing isolation | NEWS `UNIFIED`; ANNOUNCEMENT/DECISION/DOCUMENT `LEGACY` before and after capture | **PASS** |
| ANNOUNCEMENT shadow parity | 3 legacy, 3 mapped, 3 unified published; count/order/fields equal; 0 extra items | **PASS** |
| Global shadow readiness | `readyForCanary=true`; 12/12 mapped; 0 differences; no comparison error | **PASS** |
| Legacy contract freeze | List/detail HTTP 200 with frozen hashes; missing id 999 remains 404; portal-home snapshot stable | **PASS** |
| Legacy performance baseline | 100 measured samples per route after 10 warm-ups; 0 5xx and 0 hash mismatches | **PASS** |
| Database-pressure baseline | 0 waiting locks, deadlocks, rollbacks, temp files/bytes; stable database size; no matching error logs | **PASS** |
| Write/backfill boundary | Backfill apply `false`; legacy admin writes still use `AdminContentRepository`; no write redirect | **PASS** |

## 2. Frozen legacy contract baseline

Hashes are SHA-256 over the exact raw response bytes returned by the local Docker backend on port 8081.

| Contract | Expected status | Frozen SHA-256 | Bytes | Contract note |
|---|---:|---|---:|---|
| `GET /api/announcements` | 200 | `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` | 928 | Three items; numeric IDs and list order are inside the hash |
| `GET /api/announcements/1` | 200 | `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` | 332 | Representative numeric-ID detail |
| `GET /api/announcements/999` | 404 | Status contract only | 110 | Error body hash is not frozen because framework error timestamps are not a stable contract |
| `GET /api/portal/home` | 200 | `43CCE4E9882DB254AB6AE219B038A187F074D8BE8E73A837BA7CE0E2B3263BAC` | 3,868 | Full-response isolation snapshot; identical before and after performance capture |

ANNOUNCEMENT is not currently projected into `/api/portal/home`. The portal hash is therefore an isolation guard, not an ANNOUNCEMENT projection baseline.

Additional security-boundary checks:

- direct anonymous `GET /api/v1/content` returned 404;
- anonymous `GET /actuator/metrics` returned 401;
- backend actuator health returned 200.

## 3. Legacy performance and 5xx baseline

The measurement used one reused HTTP client, ten warm-up requests per route, then 100 sequential measured requests per route. Latency is client-observed end-to-end time. p50 and p95 use the nearest-rank method.

| Route | Samples | Status | Hash mismatches | 5xx | 5xx rate | Average | p50 | p95 | Max |
|---|---:|---|---:|---:|---:|---:|---:|---:|---:|
| `/api/announcements` | 100 | 100 × 200 | 0 | 0 | 0.000% | 5.237ms | 4.688ms | 8.901ms | 20.402ms |
| `/api/announcements/1` | 100 | 100 × 200 | 0 | 0 | 0.000% | 5.673ms | 4.150ms | 7.732ms | 123.540ms |

The future exit comparison permits at most a +0.5 percentage-point increase in 5xx rate. Unified p95 may not exceed the corresponding legacy p95 by more than 25%, unless both route values are below 300ms.

The baseline capture generated 222 counted ANNOUNCEMENT legacy facade requests: two contract requests, 20 warm-ups, and 200 measured requests. The missing-id request is not counted because the legacy detail lookup returns 404 before the compatibility executor records a request.

## 4. Routing remained isolated and unchanged

The protected compatibility status was captured before and after the contract/performance run. `comparisonError` was null in both snapshots, and all automatic fallback counts and fallback-reason maps were zero/empty.

| Content type | Configured | Shadow ready | Effective source | Requests before | Requests after | Unified requests after | Automatic fallbacks |
|---|---:|---:|---|---:|---:|---:|---:|
| NEWS | true | true | `UNIFIED` | 5 unified | 6 unified | 6 | 0 |
| ANNOUNCEMENT | false | true | `LEGACY` | 0 legacy | 222 legacy | 0 | 0 |
| DECISION | false | true | `LEGACY` | 1 legacy | 2 legacy | 0 | 0 |
| DOCUMENT | false | true | `LEGACY` | 1 legacy | 2 legacy | 0 | 0 |

The one-request increase for NEWS, DECISION, and DOCUMENT is the final portal-home isolation probe. It did not involve ANNOUNCEMENT and did not alter routing.

Runtime feature configuration was read directly from the backend container after capture:

| Setting | Value |
|---|---|
| `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_NEWS_ENABLED` | `true` |
| `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_ANNOUNCEMENTS_ENABLED` | `false` |
| `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_DECISIONS_ENABLED` | `false` |
| `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_DOCUMENTS_ENABLED` | `false` |
| `FEATURES_UNIFIED_CONTENT_BACKFILL_APPLY_ENABLED` | `false` |
| `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_READINESS_CACHE_TTL` | `30s` |

## 5. Live shadow parity stayed ready with zero differences

The protected shadow endpoint was called immediately before and after performance capture. Both reports were read-only and returned the same readiness result:

- global `reconciliationReady=true`;
- global `readyForCanary=true`;
- 12 legacy public sources and 12 mapped unified items;
- zero differences;
- portal-home comparison 3/3 sections ready for NEWS, DECISION, and DOCUMENT;
- ANNOUNCEMENT is correctly absent from portal-home projection.

### ANNOUNCEMENT parity

| Measure | Before | After | Result |
|---|---:|---:|---|
| Legacy count | 3 | 3 | **PASS** |
| Mapped count | 3 | 3 | **PASS** |
| Unified published count | 3 | 3 | **PASS** |
| Additional unified items | 0 | 0 | **PASS** |
| Count parity | true | true | **PASS** |
| Order parity | true | true | **PASS** |
| Field parity | true | true | **PASS** |
| Ready for canary | true | true | **PASS** |

All three mappings retained matching numeric legacy IDs, titles, summaries, dates, categories, and canonical paths:

| Legacy source key | Numeric ID | Canonical path | Differences |
|---|---:|---|---:|
| `STATIC_ANNOUNCEMENTS:ANNOUNCEMENT:1` | 1 | `/announcements/portal-first-phase-launch-2026-08-18` | 0 |
| `STATIC_ANNOUNCEMENTS:ANNOUNCEMENT:2` | 2 | `/announcements/feedback-system-update-2026-08-17` | 0 |
| `STATIC_ANNOUNCEMENTS:ANNOUNCEMENT:3` | 3 | `/announcements/official-publication-guidance-2026-08-16` | 0 |

## 6. Database-pressure baseline remained clean

PostgreSQL snapshots bracketed the contract/performance capture. These are cumulative database statistics; deltas include administrative shadow/status reads and normal local backend activity during the window, not only the static legacy ANNOUNCEMENT requests.

| PostgreSQL measure | Before (`12:02:24Z`) | After (`12:05:25Z`) | Delta |
|---|---:|---:|---:|
| Connections to database | 1 | 2 | +1 |
| Transactions committed | 694 | 772 | +78 |
| Transactions rolled back | 0 | 0 | 0 |
| Blocks read | 423 | 428 | +5 |
| Buffer hits | 24,338 | 29,705 | +5,367 |
| Temporary files | 0 | 0 | 0 |
| Temporary bytes | 0 | 0 | 0 |
| Deadlocks | 0 | 0 | 0 |
| Waiting locks | 0 | 0 | 0 |
| Database size | 9,346,071 bytes | 9,346,071 bytes | 0 |

Container resource snapshots also remained low:

| Container | CPU before | CPU after | Memory before | Memory after |
|---|---:|---:|---:|---:|
| Backend | 0.32% | 0.24% | 388.2MiB | 395.2MiB |
| PostgreSQL | 0.01% | 0.01% | 35.01MiB | 37.55MiB |

The final PostgreSQL activity snapshot showed one active inspection session and one idle application session. A case-insensitive scan of the latest 15 minutes of backend/database logs found no `ERROR`, `FATAL`, deadlock, timeout, connection-failure, or Hikari pool failure/error match.

## 7. Full backend suite passed

`mvn test` completed successfully across the 13-module reactor in 54.154 seconds.

| Module with tests | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| identity | 14 | 0 | 0 | 0 |
| content | 6 | 0 | 0 | 0 |
| bootstrap | 55 | 0 | 0 | 0 |
| **Total** | **75** | **0** | **0** | **0** |

The first Maven invocation was blocked before project execution because the sandbox could not access Maven Central for one missing POM. The approved rerun resolved the dependency and completed with `BUILD SUCCESS`; no test failed or was skipped.

## 8. Write and mutation boundaries remained closed

- The live backend environment reports backfill apply disabled.
- `ContentBackfillApplyController` is conditional on the disabled apply flag, so the apply endpoint is not registered.
- Existing `/api/admin/content` create/update/delete operations still persist through `AdminContentRepository`, the legacy admin model.
- No unified-write redirection flag or compatibility write cutover is present in this Slice.
- No POST, PUT, PATCH, DELETE, migration, database write, container restart, flag change, or production action was performed while collecting this baseline.

## 9. Evidence method and reproducibility

- Runtime: local Docker Compose project `yemen-pmo-platform`.
- Public/admin base URL: `http://localhost:8081`.
- Backend container: `yemen-pmo-platform-backend-1` (`c443c141fe16`), Compose image identity `sha256:ed827f7dac528e71f2a8f41c626e3244c1a2b05c9b6b60623129729d18ad3952`.
- Database container: `yemen-pmo-platform-database-1` (`4ee54b495182`), `postgres:16-alpine`.
- Contract hashes: exact response bytes, SHA-256.
- Performance: a reused .NET `HttpClient`, ten warm-ups per route, 100 sequential samples per route, nearest-rank percentiles.
- Routing and shadow evidence: protected read-only administrative endpoints.
- Database evidence: `pg_stat_database`, `pg_locks`, `pg_stat_activity`, `pg_database_size`, container stats, and bounded log search.
- Test evidence: Maven reactor result plus Surefire XML aggregation.

Tables are used instead of charts because this is an exact audit baseline with two endpoint distributions and before/after point snapshots; a chart would reduce lookup precision without adding a trend or segment insight.

## 10. Limitations and activation boundary

- This is local Docker evidence only and must not be represented as production evidence or production approval.
- The latency baseline is a short, sequential local measurement, not a concurrent load test or long-duration production time series.
- PostgreSQL counters are database-wide cumulative measures; the observed deltas cannot be attributed solely to ANNOUNCEMENT traffic. Pressure indicators remained zero or stable during the bounded capture.
- ANNOUNCEMENT has no current portal-home projection. Portal home is frozen only as a routing-isolation guard.
- Compatibility counters are process-local and reset on backend restart. No restart occurred during this baseline capture.
- This file freezes the evidence values for review. Pinning the reviewed baseline in a Git commit remains a prerequisite before any later activation.

## 11. Review decision

**Baseline result:** `PASS — FROZEN FOR OWNER REVIEW`.

**Activation state:** `HOLD — ANNOUNCEMENT NOT ACTIVE`.

No monitoring automation exists. A new explicit owner approval is required before changing the ANNOUNCEMENT flag or beginning its 24-hour/100-request canary window.

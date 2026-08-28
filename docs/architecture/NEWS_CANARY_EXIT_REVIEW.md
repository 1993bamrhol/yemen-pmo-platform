# NEWS Canary Exit Review

> **Status:** Owner-approved graduation — local Docker only
> **Review time:** `2026-08-26T11:09:24Z`
> **Canary start:** `2026-08-24T14:02:24Z`
> **Scope:** Local Docker NEWS read-compatibility canary only
> **Baseline:** `NEWS_CANARY_BASELINE.md`, frozen in commit `8f71ebe9d05b70014bb87090a89502f8a8b4daed`
> **Scheduled monitoring:** stopped and deleted before this review at the owner's request

## 1. Review outcome

All requested NEWS exit gates are assessed `PASS` against the frozen local-Docker legacy baseline. Before the runtime restart, the canary had already accumulated 153 unified NEWS compatibility requests over a proven healthy evidence window of 30h 39m 57s. The final live review was performed 45h 7m after activation and added five exact-match compatibility probes, giving 158 known cumulative requests across the two process lifetimes.

On `2026-08-26`, the owner explicitly approved NEWS graduation for the local Docker environment only. NEWS remains in its current unified read-routing state. This decision is not production approval, does not authorize ANNOUNCEMENT or any other content type, does not start write cutover, and required no flag change or rollback.

## 2. Exit-gate matrix

| Gate | Baseline or criterion | Canary evidence | Result |
|---|---|---|---|
| Minimum observation window | At least 24 hours after `2026-08-24T14:02:24Z` | The requirement was first met at `2026-08-25T14:32:30Z` (24h 30m 6s). Final live review at `2026-08-26T11:09:24Z` was 45h 7m after activation; 30h 39m 57s of healthy evidence was already recorded before the later Docker interruption. | **PASS** |
| Minimum NEWS traffic | At least 100 unified NEWS compatibility requests | 153 requests were recorded before the process restart; five final probes after restart produce 158 known cumulative requests. Current process counter is 5 because counters are process-local. | **PASS** |
| `SHADOW_ERROR = 0` | No `SHADOW_ERROR` automatic fallback | Historical and final status evidence reports zero automatic fallbacks, empty fallback reasons, and `comparisonError=null`. | **PASS** |
| `PROJECTION_ERROR = 0` | No `PROJECTION_ERROR` automatic fallback | Zero projection fallbacks were recorded; every captured portal-home `latestNews` hash matched and final projection is 3/3 ready. | **PASS** |
| No stabilized `SHADOW_NOT_READY` | No readiness fallback after activation stabilized | Every assessable routing observation reported NEWS `shadowReady=true`; final status after restart is also ready. The runtime-unavailable cycle issued no request and is not a readiness fallback. | **PASS** |
| DTO, numeric-ID, ordering, and portal-home contracts | Exact status and payload snapshots; missing numeric ID remains 404 | 153 captured compatibility responses matched the reviewed status/hash; 31 captured missing-id checks returned 404; final shadow retained NEWS count/order/field parity and portal-home 3/3 readiness. | **PASS** |
| Endpoint 5xx rate | Legacy 0.000%; increase no more than +0.5 percentage points | 0 of 153 compatibility responses with captured HTTP status were 5xx: canary 0.000%, delta 0.000 percentage points. | **PASS** |
| Unified p95 latency | No more than 25% above legacy unless both route p95 values are below 300ms | Final p95 values are 186.198ms, 51.155ms, and 54.333ms. Each route's legacy and canary p95 is below 300ms, so the documented exception applies. | **PASS** |
| Final live shadow comparison | Ready, 12/12 mapped, zero differences, no extra NEWS items, projection ready | Live report at `2026-08-26T11:09:24.161293949Z`: `readyForCanary=true`, 12/12 mapped, `differences=0`; NEWS count/order/field parity true, additional items 0; portal-home 3/3 ready. | **PASS** |
| No database pressure attributable to unified reads | No locks, deadlocks, temp spill, pool errors, or resource pressure attributable to NEWS reads | Final snapshot: database CPU 0.00%, 41.77MiB memory, 0 waiting locks, 0 deadlocks, 0 rollbacks, 0 temp files/bytes; backend pool started normally and no post-start database/pool error was logged. | **PASS** |
| NEWS-only routing isolation | NEWS unified; ANNOUNCEMENT, DECISION, and DOCUMENT remain legacy | Final status: NEWS `configuredForUnified=true`, `shadowReady=true`, `effectiveSource=UNIFIED`; all three other types `configuredForUnified=false`, `shadowReady=true`, `effectiveSource=LEGACY`. | **PASS** |

## 3. Final contract evidence

| Contract | Baseline status and SHA-256 | Final probes | Result |
|---|---|---|---|
| `GET /api/news` | 200; `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` | 200/200; exact hash twice; 186.198ms and 26.778ms | **PASS** |
| `GET /api/news/1` | 200; `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` | 200/200; exact hash twice; 40.595ms and 20.477ms | **PASS** |
| `GET /api/portal/home` → `latestNews` | 200; `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` | 200; exact `latestNews` hash; 48.766ms | **PASS** |
| `GET /api/news/999` | 404 status contract | 404; 36.304ms | **PASS** |

Across the full review evidence, 61 list responses, 61 detail responses, and 31 portal-home responses have captured status/hash evidence. All 153 matched the baseline. Five additional requests from monitoring cycle 21 are included in the 158-request cumulative total but excluded from contract, 5xx, and latency calculations because its harness failed before serializing their evidence.

## 4. Performance comparison

The baseline used a reused client, ten warm-ups, and 100 sequential samples per route. Canary p95 below is the nearest-rank p95 of all captured client-observed latency samples from monitoring cycles 1–20 and 22–30 plus the final live probes. Cycle 21 and activation smoke are excluded because they emitted no per-request latency.

| Route | Legacy samples | Legacy p95 | Canary samples | Canary average | Canary p50 | Canary p95 | Canary max | Gate result |
|---|---:|---:|---:|---:|---:|---:|---:|---|
| `/api/news` | 100 | 10.895ms | 60 | 51.539ms | 24.258ms | 186.198ms | 423.843ms | **PASS** — both p95 values below 300ms |
| `/api/news/1` | 100 | 9.450ms | 60 | 28.515ms | 21.919ms | 51.155ms | 154.679ms | **PASS** — both p95 values below 300ms |
| `/api/portal/home` | 100 | 7.484ms | 30 | 32.988ms | 24.097ms | 54.333ms | 164.263ms | **PASS** — both p95 values below 300ms |

Two isolated list samples exceeded 300ms during the window, but the final list p95 remained 186.198ms. The samples were periodic observations rather than a continuous 15-minute time series, and no 15-minute consecutive p95 rollback trigger was established.

## 5. Aggregate operating evidence

| Item | Final evidence |
|---|---|
| Known cumulative NEWS compatibility requests | 158: 153 before restart plus 5 final live probes after restart |
| Total automatic fallbacks | 0 |
| Fallback reasons | None; `SHADOW_ERROR=0`, `PROJECTION_ERROR=0`, `SHADOW_NOT_READY=0` in all recorded counters |
| End-to-end observation span | 45h 7m from activation to final live review |
| Proven healthy evidence before runtime interruption | 30h 39m 57s, already exceeding the 24-hour gate |
| Legacy vs canary 5xx | 0.000% vs 0.000%; delta 0.000 percentage points |
| Database snapshot | 0 waiting locks, 0 deadlocks, 0 rollbacks, 0 temp files/bytes; database 0.00% CPU and 41.77MiB memory |
| Scheduled monitor | Deleted before this review; no further automatic run is scheduled |

### Final effective source

| Content type | configuredForUnified | shadowReady | effectiveSource | Current-process requests | Fallbacks |
|---|---:|---:|---|---:|---:|
| NEWS | true | true | `UNIFIED` | 5 unified | 0 |
| ANNOUNCEMENT | false | true | `LEGACY` | 0 legacy | 0 |
| DECISION | false | true | `LEGACY` | 1 legacy via final portal-home probe | 0 |
| DOCUMENT | false | true | `LEGACY` | 1 legacy via final portal-home probe | 0 |

## 6. Evidence limitations

- This is local Docker evidence only and must not be presented as a production result.
- Monitoring cycle 21 issued exactly five compatibility requests, but its harness failed while serializing the result. Those five responses are excluded from the contract, 5xx, and latency sample calculations. Later cycles and the final review re-established exact contract matches, healthy routing, zero fallbacks, and a clean shadow report.
- Compatibility counters are process-local. Docker was unavailable at `2026-08-26T10:58:39Z` and subsequently restarted, resetting the live counter. The cumulative total therefore combines the last pre-restart counter (153) with the final post-restart counter (5); the report does not misrepresent 5 as the whole-window total.
- PostgreSQL reported an unclean host/runtime shutdown and completed automatic recovery before becoming ready. No evidence attributes that interruption to unified reads, and the final pressure checks were clean.
- The database-pressure gate uses the final PostgreSQL/Docker snapshot plus the absence of observed pool, fallback, 5xx, lock, or database errors; no dedicated long-duration database-pressure time series was retained for this local canary.
- The canary latency sample cadence differs from the 100-request sequential legacy baseline. The latency gate still passes under its explicit below-300ms exception for both baseline and canary p95 values.
- No write cutover, database mutation, migration, frontend change, or production deployment is covered by this review.

## 7. Owner decision and approval boundary

Technical gate assessment: all gates `PASS` for the local NEWS canary evidence.

Owner decision on `2026-08-26`: **NEWS GRADUATED — LOCAL DOCKER ONLY**.

- Keep NEWS on `UNIFIED` reads.
- Keep ANNOUNCEMENT, DECISION, and DOCUMENT on `LEGACY`; none is authorized for activation by this decision.
- Do not represent this result as production approval or production evidence.
- Do not start write cutover or change the database/write source.
- Review the separate ANNOUNCEMENT plan before any activation; its current decision state is `HOLD`.

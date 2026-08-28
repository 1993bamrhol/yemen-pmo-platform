# ANNOUNCEMENT Canary Exit Review

- Environment: local Docker only
- Verified activation start: `2026-08-26T12:56:09Z`
- Final evidence timestamp: `2026-08-28T10:36:02Z`
- Actual elapsed monitoring window: 45h 39m 53s
- Conservative continuous post-recovery window: 24h 33m 28s from the verified recovery observation at `2026-08-27T10:02:34Z`
- Decision boundary: technical exit review only; this document does **not** graduate ANNOUNCEMENT, authorize production, enable DECISION or DOCUMENT, or authorize write cutover

## 1. Exit prerequisites

| Prerequisite | Evidence | Result |
|---|---|---|
| At least 24 hours after `2026-08-26T12:56:09Z` | Final evidence at `2026-08-28T10:36:02Z`; 45h 39m 53s elapsed. The post-recovery observation alone also exceeded 24 hours. | **PASS** |
| At least 100 ANNOUNCEMENT unified compatibility requests | Final process-local counter: 100 unified / 0 legacy after the documented restart. Durable evidence: 145 successful counted requests across activation and monitoring. | **PASS** |

Both prerequisites are satisfied. The Exit Review was not created before the conservative live counter reached 100.

## 2. Executive outcome

All nine technical exit gates in `ANNOUNCEMENT_CANARY_PLAN.md` pass against the frozen legacy baseline. The final routing state remains NEWS and ANNOUNCEMENT on `UNIFIED`, with DECISION and DOCUMENT on `LEGACY`. The final shadow report is ready at 12/12 mappings with zero differences, all 145 durable counted ANNOUNCEMENT requests returned the frozen contracts without 5xx or fallback, and the final database/resource evidence is clean.

An operational interruption remains material context: the local Docker backend and PostgreSQL became unavailable before cycle 8, PostgreSQL subsequently performed automatic WAL recovery, and the backend process-local counters reset. Cycle 8 and the recovery/continuity cycle 9 were correctly recorded as `FAIL`. No contract, routing, shadow, fallback, 5xx, or database-pressure regression was found after recovery. To avoid relying on pre-restart traffic, monitoring continued until the new live ANNOUNCEMENT counter independently reached 100 and the post-recovery observation period exceeded 24 hours.

Technical gate passage is **not** an automatic graduation decision. ANNOUNCEMENT remains pending explicit owner approval.

## 3. Gate-by-gate decision

| Exit gate | Evidence | Result |
|---|---|---|
| `SHADOW_ERROR = 0` | Final ANNOUNCEMENT automatic fallbacks: 0; every successful status snapshot recorded 0 and an empty reason map. | **PASS** |
| `PROJECTION_ERROR = 0` | Final automatic fallbacks: 0 and fallback reasons empty. ANNOUNCEMENT is not projected into portal home; the final isolation snapshot remained byte-identical to baseline. | **PASS** |
| No stabilized `SHADOW_NOT_READY` | Every successful status/shadow observation reported ANNOUNCEMENT `shadowReady=true` and `readyForCanary=true`. Cycle 8 was unavailable rather than a not-ready event; the recovered service was immediately ready and remained ready through the final review. | **PASS** |
| Contract, numeric-ID, missing-ID, count, order, and serialization parity | All 145 counted list/detail requests returned HTTP 200 with the frozen hashes. Every documented missing-id guard returned 404. Final ANNOUNCEMENT shadow is 3 legacy / 3 mapped / 3 unified, with count/order/field parity true and 0 extra items. | **PASS** |
| Routing isolation | Final status: NEWS `UNIFIED`, ANNOUNCEMENT `UNIFIED`, DECISION `LEGACY`, DOCUMENT `LEGACY`; all are shadow-ready. Final `/api/portal/home` returned HTTP 200 and baseline SHA-256 `43CCE4E9882DB254AB6AE219B038A187F074D8BE8E73A837BA7CE0E2B3263BAC`. | **PASS** |
| 5xx delta no greater than +0.5 percentage points | Legacy baseline: 0/200 measured responses = 0.000%. Canary: 0/145 counted responses = 0.000%. Delta: +0.000 percentage points. | **PASS** |
| Unified p95 latency | List: 63.711ms versus 8.901ms legacy. Detail: 44.488ms versus 7.732ms legacy. Both baseline and unified p95 values are below 300ms for each route, so the plan's explicit sub-300ms exception applies. | **PASS** |
| Final live shadow ready and zero differences | `reconciliationReady=true`, `readyForCanary=true`, 12 legacy sources / 12 mapped items, 0 global differences; ANNOUNCEMENT parity true with 0 additional items; `comparisonError=null`; portal comparison 3/3. | **PASS** |
| No database pressure attributable to unified reads | Final snapshot: 2 connections, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, database size 9,346,071 bytes. Backend 0.20% CPU / 484.3MiB; PostgreSQL 0.00% CPU / 37.37MiB; bounded recent logs were empty. The earlier WAL recovery is disclosed as an operational interruption, with no evidence tying it to unified reads. | **PASS** |

## 4. Contract and request totals

### Counted ANNOUNCEMENT traffic

| Measure | Result |
|---|---:|
| Durable successful counted requests | 145 |
| Final conservative live unified counter after restart | 100 |
| Final live legacy counter | 0 |
| Counted 5xx | 0 |
| Hash/status mismatches | 0 |
| Automatic fallbacks | 0 |
| Fallback reasons | None |

The durable total consists of the five activation requests and 140 monitoring requests. Five successful requests from the interrupted `2026-08-26T23:19:22Z` run were preserved from counter and contract evidence but were not replayed after the interruption. The backend restart reset process-local counters; the final live value of 100 therefore provides the stricter traffic gate and avoids double-counting pre-restart traffic.

### Frozen contract results

| Contract | Expected | Canary evidence | Result |
|---|---|---|---|
| `GET /api/announcements` | HTTP 200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` | Every counted list probe matched | **PASS** |
| `GET /api/announcements/1` | HTTP 200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` | Every counted detail probe matched | **PASS** |
| `GET /api/announcements/999` | HTTP 404 | Every documented uncounted missing-id guard returned 404 | **PASS** |
| `GET /api/portal/home` | HTTP 200; SHA-256 `43CCE4E9882DB254AB6AE219B038A187F074D8BE8E73A837BA7CE0E2B3263BAC` | Final isolation guard matched exactly | **PASS** |

The list hash covers the three-item count, numeric IDs, ordering, and serialized fields. The representative detail hash covers the numeric-ID detail contract and serialization. The final live shadow independently confirms count, order, and field parity for all three ANNOUNCEMENT mappings.

## 5. Performance comparison

Latency is client-observed end-to-end time. Percentiles use the nearest-rank method, matching the frozen baseline method. The monitoring log contains individual latency evidence for 140 of the 145 durable successful requests; the five preserved interrupted-run requests have contract/counter evidence but no durable per-request latency values and are excluded from latency calculations only.

| Route | Legacy samples | Legacy avg | Legacy p50 | Legacy p95 | Unified latency samples | Unified avg | Unified p50 | Unified p95 | Unified max | Gate |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| `/api/announcements` | 100 | 5.237ms | 4.688ms | 8.901ms | 56 | 38.541ms | 29.151ms | 63.711ms | 424.219ms | **PASS** via both-p95-below-300ms exception |
| `/api/announcements/1` | 100 | 5.673ms | 4.150ms | 7.732ms | 84 | 27.894ms | 21.747ms | 44.488ms | 200.177ms | **PASS** via both-p95-below-300ms exception |

One isolated list request reached 424.219ms during cycle 2. It did not persist for 15 consecutive minutes, did not move aggregate p95 above 300ms, and had no associated contract, health, shadow, database, or resource anomaly.

## 6. 5xx comparison

| Measure | Legacy baseline | ANNOUNCEMENT canary | Delta | Limit | Result |
|---|---:|---:|---:|---:|---|
| Counted responses | 200 | 145 | — | — | — |
| 5xx responses | 0 | 0 | 0 | — | — |
| 5xx rate | 0.000% | 0.000% | +0.000 percentage points | No more than +0.5 percentage points | **PASS** |

## 7. Final live routing and counters

The final protected status was captured after the portal-home isolation request at `2026-08-28T10:36:02Z`.

| Content type | configuredForUnified | shadowReady | effectiveSource | Legacy requests | Unified requests | Automatic fallbacks |
|---|---:|---:|---|---:|---:|---:|
| NEWS | true | true | `UNIFIED` | 0 | 2 | 0 |
| ANNOUNCEMENT | true | true | `UNIFIED` | 0 | 100 | 0 |
| DECISION | false | true | `LEGACY` | 2 | 0 | 0 |
| DOCUMENT | false | true | `LEGACY` | 2 | 0 | 0 |

The NEWS/DECISION/DOCUMENT counter values include the activation and final portal-home isolation guards within the current backend process. No feature flag was changed during monitoring or review.

## 8. Final shadow and database evidence

Final protected shadow comparison at `2026-08-28T10:36:02Z`:

- read-only administrative inspection;
- `reconciliationReady=true` and `readyForCanary=true`;
- 12/12 source mappings and 0 global differences;
- ANNOUNCEMENT: 3 legacy, 3 mapped, 3 unified published, 0 additional unified items;
- ANNOUNCEMENT count/order/field parity all true;
- portal-home comparison 3/3 and projection-ready;
- status `comparisonError=null`.

Final database/backend snapshot:

- database connections: 2;
- committed transactions: 13,075 cumulative;
- rolled-back transactions: 0;
- temporary files / bytes: 0 / 0;
- deadlocks: 0;
- waiting locks: 0;
- database size: 9,346,071 bytes, unchanged from baseline;
- backend: 0.20% CPU and 484.3MiB memory;
- PostgreSQL: 0.00% CPU and 37.37MiB memory;
- bounded recent backend/database logs: no entries.

## 9. Operational incident and evidence continuity

- Cycle 8 at `2026-08-27T09:46:48Z` failed before compatibility traffic because the local Docker backend was unavailable.
- Cycle 9 documented successful service recovery, PostgreSQL automatic WAL recovery after an improper shutdown, and reset process-local counters. It was marked `FAIL` for continuity even though contracts, routing, shadow, health, and database checks passed.
- The monitor performed no automatic restart, rollback, flag change, database mutation, or replacement compatibility probes.
- Monitoring continued conservatively until both a new process-local counter of 100 and more than 24 hours of stable post-recovery evidence were available.
- Final database, health, shadow, contract, routing, resource, and log checks are clean. There is no evidence that the interruption was caused by unified ANNOUNCEMENT reads, but the incident remains part of the owner decision record.

## 10. Recommendation and approval boundary

**Technical recommendation: PASS — eligible for explicit owner graduation review in the local Docker environment only.**

This recommendation does not itself graduate ANNOUNCEMENT. Until the owner explicitly approves graduation:

- keep ANNOUNCEMENT on its current `UNIFIED` route without changing the flag;
- keep NEWS on `UNIFIED`;
- keep DECISION and DOCUMENT on `LEGACY`;
- do not start DECISION canary;
- do not start write cutover;
- do not deploy or infer production approval;
- do not perform rollback unless separately authorized.


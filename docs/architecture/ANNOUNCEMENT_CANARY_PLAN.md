# ANNOUNCEMENT Canary Plan

> **Status:** Draft for owner review — activation `HOLD`
> **Prepared:** 2026-08-26
> **Scope:** Local Docker read compatibility only
> **Predecessor:** NEWS graduated locally; see `NEWS_CANARY_EXIT_REVIEW.md`

## 1. Decision boundary

This document prepares the next type-by-type compatibility canary. It does not activate ANNOUNCEMENT, change a feature flag, create a monitor, start write cutover, modify the database, or authorize production use.

The retained starting state is:

| Content type | configuredForUnified | effectiveSource | Decision state |
|---|---:|---|---|
| NEWS | true | `UNIFIED` | Graduated in local Docker only |
| ANNOUNCEMENT | false | `LEGACY` | Proposed next canary; `HOLD` |
| DECISION | false | `LEGACY` | Not authorized |
| DOCUMENT | false | `LEGACY` | Not authorized |

Activation requires a new, explicit owner decision after reviewing this plan and its pre-activation baseline. NEWS local graduation is not authorization to activate ANNOUNCEMENT.

## 2. Objective and compatibility surface

The canary would route only existing ANNOUNCEMENT public reads through the unified store while preserving their public contract and automatic legacy fallback:

- `GET /api/announcements`
- `GET /api/announcements/{numericId}`

The existing portal-home response does not consume `AnnouncementQuery`; therefore ANNOUNCEMENT has no portal-home projection to cut over. `/api/portal/home` will be checked before activation and at exit only as an isolation regression guard, not counted as ANNOUNCEMENT traffic and not treated as an ANNOUNCEMENT projection gate.

The canary must preserve:

- HTTP status codes;
- numeric legacy identifiers;
- list item count and order;
- DTO field names, values, null behavior, and serialization;
- representative detail payloads;
- missing-id behavior, including `GET /api/announcements/999` returning 404;
- NEWS remaining `UNIFIED` and DECISION/DOCUMENT remaining `LEGACY`.

## 3. Pre-activation evidence package

No flag may change until all items below are captured in a proposed `ANNOUNCEMENT_CANARY_BASELINE.md` and reviewed:

1. Run the full backend test suite and record module totals, failures, and errors.
2. Confirm backend and PostgreSQL health in the local Docker environment.
3. Confirm compatibility status:
   - NEWS is configured, shadow-ready, and effectively `UNIFIED`;
   - ANNOUNCEMENT is not configured, is shadow-ready, and effectively `LEGACY`;
   - DECISION and DOCUMENT are not configured and effectively `LEGACY`.
4. Run a fresh live shadow comparison and require:
   - global `readyForCanary=true`;
   - 12/12 mapped and zero global differences;
   - ANNOUNCEMENT 3 legacy items and 3 unified items;
   - count, order, and compared fields equal;
   - zero additional unified ANNOUNCEMENT items;
   - `comparisonError=null`.
5. Capture frozen legacy status and SHA-256 payload hashes for:
   - `/api/announcements`;
   - `/api/announcements/1`;
   - `/api/announcements/999` status contract;
   - `/api/portal/home` as a cross-type isolation snapshot.
6. Establish legacy performance baselines for the list and representative detail routes using one reused client, ten warm-ups, and 100 sequential measured samples per route. Record average, p50, p95, maximum, and 5xx rate.
7. Capture database-pressure baseline: container CPU/memory, active and waiting locks, deadlocks, rollbacks, temporary files/bytes, connection-pool errors, and relevant database/backend errors.
8. Confirm backfill apply remains disabled, legacy administration remains the writer, and no write redirect is active.
9. Freeze the baseline in a reviewed commit before activation.

Any failure leaves activation on `HOLD`.

## 4. Proposed activation procedure — not executed

After explicit owner approval only:

1. Set `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_ANNOUNCEMENTS_ENABLED=true` in the local Docker backend only.
2. Keep NEWS enabled; keep DECISION and DOCUMENT disabled.
3. Restart/redeploy only the local backend required to apply that flag.
4. Confirm ANNOUNCEMENT is configured, shadow-ready, and effectively `UNIFIED`.
5. Confirm NEWS remains `UNIFIED`; DECISION and DOCUMENT remain `LEGACY`.
6. Run an activation smoke:
   - two list requests;
   - two representative detail requests for numeric id `1`;
   - one additional ANNOUNCEMENT compatibility request;
   - one missing-id `999` status check;
   - one portal-home isolation check.
7. Require exact baseline status/hash matches, zero automatic fallbacks, a ready zero-difference shadow report, and no health or database anomaly.
8. Record the actual start time. The 24-hour clock begins only after the post-activation checks pass.

The activation deployment must not change the NEWS, DECISION, or DOCUMENT flags, the write path, database schema/data, default application configuration, or production environment.

## 5. Monitoring protocol

ANNOUNCEMENT remains in canary until both minimums are met:

- at least 24 hours after the verified activation start; and
- at least 100 successful ANNOUNCEMENT compatibility requests observed through the unified facade.

Each scheduled observation, if separately approved, will issue exactly five counted ANNOUNCEMENT compatibility requests:

- two `GET /api/announcements` list probes;
- three `GET /api/announcements/1` detail probes.

It will additionally check missing id `999` without counting it toward successful ANNOUNCEMENT traffic. Portal home is checked at activation, after any anomaly that could affect routing isolation, and in the final exit review; it is not part of the five ANNOUNCEMENT probes.

Every observation must record:

- UTC timestamp and elapsed duration;
- backend health;
- effective source and configured/shadow state for all four types;
- cumulative ANNOUNCEMENT legacy and unified request counters;
- list/detail HTTP statuses, SHA-256 hashes, and client-observed latency;
- missing-id status;
- automatic fallback total and reasons;
- live shadow readiness, counts, ordering, differences, and comparison error;
- 5xx count and any contract anomaly;
- database/backend anomaly evidence when present.

The planned evidence log is `ANNOUNCEMENT_CANARY_MONITORING_LOG.md`. It is not created and no monitoring automation is scheduled until activation is explicitly approved.

## 6. Exit gates

After both time and traffic minimums are satisfied, the final review must compare the canary to the frozen legacy baseline and give every gate an explicit `PASS` or `FAIL`:

| Gate | Pass criterion |
|---|---|
| `SHADOW_ERROR` | 0 automatic fallbacks |
| `PROJECTION_ERROR` | 0 automatic fallbacks; ANNOUNCEMENT has no portal-home projection, so any occurrence is a failure |
| `SHADOW_NOT_READY` | No event after activation stabilizes |
| Contract compatibility | No DTO, numeric-ID, missing-ID, count, ordering, or serialization regression |
| Routing isolation | NEWS remains `UNIFIED`; DECISION and DOCUMENT remain `LEGACY`; portal-home snapshot unchanged except for independently approved data changes |
| 5xx rate | Canary increase no greater than +0.5 percentage points versus legacy baseline |
| Unified p95 | No more than 25% above route-specific legacy p95, unless both values are below 300ms |
| Final live shadow | Ready, ANNOUNCEMENT count/order/fields equal, zero differences, zero extra items, no comparison error |
| Database pressure | No locks, deadlocks, temp spill, pool errors, or resource pressure attributable to unified ANNOUNCEMENT reads |

Insufficient traffic or duration is not success; extend the observation window. A final document named `ANNOUNCEMENT_CANARY_EXIT_REVIEW.md` will be created only after both minimums are met.

Passing technical gates does not automatically graduate ANNOUNCEMENT. Graduation requires a separate explicit owner decision, remains local Docker only unless separately stated, and does not authorize DECISION, DOCUMENT, production, or write cutover.

## 7. Failure and rollback controls

The following are rollback triggers for the proposed canary:

- any contract, identifier, count, order, or content mismatch;
- any `PROJECTION_ERROR`;
- repeated `SHADOW_ERROR` or shadow readiness becoming false;
- stabilized `SHADOW_NOT_READY`;
- 5xx increase above the gate;
- p95 above the gate for 15 consecutive minutes;
- database pressure attributable to unified ANNOUNCEMENT reads;
- NEWS, DECISION, or DOCUMENT routing isolation changing unexpectedly.

On a trigger, monitoring reports `FAIL` with evidence. No automatic rollback is created by this plan. Rollback requires the operational authority agreed at activation; it changes only the ANNOUNCEMENT flag to false, restarts the backend, confirms `effectiveSource=LEGACY`, reruns contract/isolation checks, and preserves all unified data and logs.

## 8. Review checklist and proposed decision

- [ ] Baseline capture authorized.
- [ ] `ANNOUNCEMENT_CANARY_BASELINE.md` reviewed and frozen.
- [ ] Operator and rollback authority named.
- [ ] Activation window approved.
- [ ] ANNOUNCEMENT-only flag change approved.
- [ ] Monitoring schedule approved.

**Current decision:** `HOLD — PLAN READY FOR REVIEW; ANNOUNCEMENT NOT ACTIVE`.

The next action, if approved, is baseline capture only. It is not flag activation.

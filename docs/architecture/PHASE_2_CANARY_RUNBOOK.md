# Phase 2 Slice 5C — Content Canary Runbook

> **Status:** NEWS graduated in local Docker only; ANNOUNCEMENT plan pending review and activation approval
> **Date:** 2026-08-26
> **Scope:** Existing read compatibility only. No write cutover, migration, deletion, or frontend redesign.

## 1. Purpose

This runbook controls the type-by-type move of legacy public content reads to the unified store. A canary changes only the effective read source behind the existing endpoint. URLs, numeric identifiers, DTOs, and the legacy fallback remain unchanged.

Activation order is fixed unless a later architecture decision changes it:

1. `NEWS`
2. `ANNOUNCEMENT`
3. `DECISION`
4. `DOCUMENT`

Only one new type may enter canary at a time. Portal-home sections follow the same facades, so enabling news also changes `latestNews`; decisions and documents follow their own flags.

## 2. Instrumentation

Every facade request records two Micrometer meters:

- `government.content.compatibility.requests`: counter tagged by `content_type`, `operation`, `source`, and `fallback_reason`.
- `government.content.compatibility.latency`: timer tagged by `content_type`, `operation`, and `source`.

Allowed low-cardinality values are:

- operation: `list`, `detail`
- source: `LEGACY`, `UNIFIED`
- fallback reason: `NONE`, `FLAG_DISABLED`, `SHADOW_NOT_READY`, `SHADOW_ERROR`, `PROJECTION_ERROR`

The protected endpoint `GET /api/v1/admin/content-compatibility/status` also reports per type:

- configured flag, current shadow readiness, and effective source
- legacy and unified request counts since process start
- automatic fallback count and counts grouped by reason

In-process counters reset on application restart. Micrometer is the durable integration boundary for a future Prometheus/OTel registry; Slice 5C does not expose `/actuator/metrics` publicly.

Shadow readiness used on the request path is cached for 30 seconds by default. The cache is fail-closed: comparison errors route to legacy. The TTL is configurable through `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_READINESS_CACHE_TTL`.

## 3. Pre-activation gates

All gates must pass immediately before enabling a type:

1. Full backend test suite is green.
2. PostgreSQL shadow comparison reports `readyForCanary=true` for the target type, zero item differences, no extra unified items, and correct order.
3. Compatibility status reports `shadowReady=true` for the target and `effectiveSource=LEGACY` before activation.
4. Legacy list, detail, and affected portal-home projection return 200 and their reviewed payload snapshots.
5. Backfill apply remains disabled and unified writes are not redirected from legacy administration.
6. A named operator has access to change the single target flag and restart/redeploy the backend.
7. No other compatibility flag is newly enabled in the same deployment.

## 4. Activation procedure

Activation requires explicit operational approval. For the first canary only:

1. Set `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_NEWS_ENABLED=true`.
2. Leave announcement, decision, and document flags false.
3. Redeploy/restart the backend.
4. Confirm status: NEWS is configured, shadow-ready, and effectively `UNIFIED`; the other types remain `LEGACY`.
5. Exercise `/api/news`, representative `/api/news/{id}`, and `/api/portal/home`.
6. Observe for at least 24 hours and at least 100 NEWS compatibility requests before considering the next type.

Repeat the same procedure in the fixed order only after the previous type passes the exit gates.

## 5. Canary exit gates

A type may graduate from canary when all conditions hold for the observation window:

- zero `SHADOW_ERROR` and `PROJECTION_ERROR` fallbacks
- no `SHADOW_NOT_READY` event after activation stabilizes
- zero known DTO, numeric-ID, ordering, or portal-home regressions
- no increase greater than 0.5 percentage points in endpoint 5xx rate versus the legacy baseline
- unified p95 latency no more than 25% above the legacy baseline, unless both are below 300 ms
- the final live shadow report remains ready with zero differences

Absence of sufficient traffic does not count as success; extend the window.

## 6. Immediate rollback triggers

Rollback the affected type immediately if any of these occurs:

- contract, identifier, order, or content mismatch
- any `PROJECTION_ERROR` fallback
- repeated `SHADOW_ERROR` or readiness becoming false
- endpoint 5xx increase above the gate
- p95 latency exceeding the gate for 15 consecutive minutes
- database pressure attributable to unified reads

## 7. Rollback procedure

1. Set only the affected type's compatibility flag to `false`.
2. Redeploy/restart the backend.
3. Confirm its `effectiveSource=LEGACY` in the protected status endpoint.
4. Verify list, detail, and portal-home responses.
5. Preserve unified tables, mappings, revisions, and logs for diagnosis; do not reverse migrations or delete backfilled data.
6. Record the trigger, time, affected paths, metrics, and corrective action before another activation attempt.

Rollback affects reads only. Legacy administration remains the writer throughout this phase, preventing a write-source rollback problem.

## 8. Current decision

Explicit operational approval for NEWS only was granted on 2026-08-24. After all exit gates passed, the owner approved NEWS graduation on 2026-08-26 for the local Docker environment only. NEWS remains configured for unified compatibility and effectively `UNIFIED`. Announcement, decision, and document compatibility remain disabled and effective on `LEGACY` reads.

This is not production approval. The database and write path remain unchanged, no write cutover is authorized, and no later content type is authorized. `ANNOUNCEMENT_CANARY_PLAN.md` is a review-only proposal and activation remains on `HOLD` until separate explicit approval.

## 9. Pre-activation verification record

- Full backend suite: 55 bootstrap, 6 content, and 14 identity tests; 75 passed with zero failures or errors.
- Docker/PostgreSQL: Flyway remained at V6 and all four content types reported `shadowReady=true`.
- All four configured flags remained false and every effective source remained `LEGACY`.
- Generated smoke traffic appeared in the per-type legacy counters with zero unified requests and zero automatic fallbacks.
- Health, legacy list/detail endpoints, and portal home returned 200; unified public read remained closed with 404.
- `/actuator/metrics` was not anonymously accessible.

## 10. NEWS activation record

- Target: local Docker backend only.
- Start: `2026-08-24T14:02:24Z` (`2026-08-24 17:02:24` Asia/Riyadh).
- Active flags: NEWS `true`; ANNOUNCEMENT, DECISION, and DOCUMENT `false`.
- Effective sources after activation: NEWS `UNIFIED`; all other types `LEGACY`.
- Initial traffic: 3 unified NEWS compatibility requests, zero automatic fallbacks, and no fallback reasons.
- Contract smoke: news list and representative detail returned 200; missing id returned 404; list, detail, and portal-home news payloads matched their pre-activation SHA-256 baselines exactly.
- Shadow state: 12/12 mapped, zero differences; NEWS count, order, and fields matched; portal-home content projection remained ready.
- Security boundary: direct unified public read remained closed with 404 and anonymous actuator metrics remained protected with 401.
- Earliest exit review: after `2026-08-25T14:02:24Z`, provided at least 100 NEWS compatibility requests and every exit gate are satisfied.

## 11. NEWS local-graduation record

- Owner decision date: `2026-08-26`.
- Scope: local Docker environment only.
- Technical basis: `NEWS_CANARY_EXIT_REVIEW.md` assessed every required exit gate `PASS` against the frozen baseline.
- Known traffic: 158 cumulative NEWS compatibility requests across the documented process lifetimes.
- Final routing retained: NEWS `configuredForUnified=true`, `shadowReady=true`, `effectiveSource=UNIFIED`.
- Isolation retained: ANNOUNCEMENT, DECISION, and DOCUMENT `configuredForUnified=false`, `shadowReady=true`, `effectiveSource=LEGACY`.
- Approval exclusions: no production approval, no write cutover, no database change, and no authorization to activate another type.
- Next proposed step: review `ANNOUNCEMENT_CANARY_PLAN.md`; activation remains `HOLD`.

# Phase 2 Slice 5B — Compatibility Facades Record

> **Status:** Implemented and verified; all production defaults remain `LEGACY`  
> **Date:** 2026-08-24  
> **Scope:** Read compatibility only; no write cutover, redesign, deletion, or database migration

## Outcome

The existing public contracts remain stable while gaining a reversible route to the unified content store. The following paths keep their numeric identifiers and existing JSON DTOs:

- `/api/news` and `/api/news/{id}`
- `/api/announcements` and `/api/announcements/{id}`
- `/api/decisions` and `/api/decisions/{id}`
- `/api/documents` and `/api/documents/{id}`
- the content-backed sections of `/api/portal/home`

Controllers now depend on small query contracts. A primary compatibility facade selects the legacy static service or projects a published unified item back into the legacy DTO. The legacy service remains available as the immediate fallback.

## Independent flags

| Content type | Environment variable | Default |
|---|---|---|
| News | `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_NEWS_ENABLED` | `false` |
| Announcements | `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_ANNOUNCEMENTS_ENABLED` | `false` |
| Decisions | `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_DECISIONS_ENABLED` | `false` |
| Documents | `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_DOCUMENTS_ENABLED` | `false` |

Setting a flag to `true` is necessary but not sufficient. The router also requires the corresponding type in the live shadow comparison to report `readyForCanary=true`. Comparison failure, mapping failure, or projection failure causes an automatic per-request fallback to the legacy service.

## Projection rules

- Public content is read only through `PublicContentService`, so drafts, archived items, and non-published revisions remain excluded.
- `legacy_content_mappings` supplies the numeric legacy identifier.
- The unified published revision supplies title and summary/description.
- The reviewed legacy snapshot supplies the original presentation date and category, preserving the existing DTO contract exactly.
- Extra unified items are blocked by shadow readiness and are not silently exposed through a legacy list.

## Operational status endpoint

`GET /api/v1/admin/content-compatibility/status` requires platform `content.manage`. It is read-only and reports, per type:

- `configuredForUnified`
- `shadowReady`
- `effectiveSource` (`LEGACY` or `UNIFIED`)

It also reports a comparison error class when readiness cannot be calculated. The endpoint performs no writes or audit mutations.

## Verification

- Default-state integration test: all four flags are false and every effective source is `LEGACY`.
- Enabled-state integration test: after transactional backfill, all four types become `UNIFIED` and every list/detail DTO equals the existing legacy response, including numeric IDs.
- Existing controller and portal-home tests remain unchanged and pass.
- No database migration was added and no legacy source was removed or modified.
- Full backend result: 71 tests passed (51 bootstrap, 6 content, 14 identity), with zero failures or errors.
- Docker/PostgreSQL result: all four types report `shadowReady=true`, `configuredForUnified=false`, and `effectiveSource=LEGACY`; no canary was activated.

## Canary and rollback boundary

Slice 5B does not authorize canary activation. A future approved Slice 5C should enable one content type at a time, starting with news, monitor error/fallback/parity metrics, and then proceed sequentially. Rollback is changing that type's flag to `false`; unified tables, mappings, and revisions remain intact.

Legacy writes still target the legacy admin model. They must not be redirected until a separate single-writer plan prevents split-brain.

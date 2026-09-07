# Phase 5D.4 — Homepage Activation Readiness

**Date:** 2026-08-30

**Branch:** `main`

**Baseline checkpoint:** `7b27597dd689167e8f55cd4a4ea5c4dd08494da7`

**Scope:** Audit and readiness assessment only; no Frontend, Backend, Database, feature-flag, or Figma changes.

## Executive decision

**Recommendation: GO WITH CONDITIONS.**

The Homepage architecture, shared shell, responsive foundations, and section-scoped runtime states are ready. The only safe activation slice supported by the current runtime data is adding canonical links to eligible Government Entity cards. Quick Services, Latest Updates, Unified Search, and Open Data must remain inactive because their required official data or API capability is not currently available.

The decision is data-driven rather than API-existence-driven:

- one active Government Entity is publicly returned with a valid API-provided canonical path;
- the Government Services catalog contains zero public records;
- all 12 current editorial records are `PUBLISHED` but `UNVERIFIED`;
- the verified public V1 content controller is disabled in the local runtime;
- no Unified Search or Open Data domain/API exists.

No Figma example content, `site-data.ts` data, compatibility feed, or synthetic government record is acceptable as a substitute.

## Sources reviewed

- `docs/implementation/PHASE_5_DESIGN_TO_CODE_CONTRACT.md`
- `docs/implementation/PHASE_5C_2_EDITORIAL_VERIFICATION.md`
- `docs/implementation/PHASE_5C_3_GOVERNMENT_ENTITY_PROFILE.md`
- `docs/implementation/PHASE_5C_4_GOVERNMENT_SERVICES.md`
- `docs/implementation/PHASE_5D_1_SERVICE_DETAIL_FRONTEND.md`
- `docs/implementation/PHASE_5D_2_ENTITY_FRONTEND.md`
- `docs/implementation/PHASE_5D_3_LOCAL_RUNTIME_E2E.md`
- current Homepage, public layout, shared compositions, and `frontend/src/lib/api.ts`
- local Docker runtime, Backend APIs, and read-only database evidence
- approved Homepage Figma frames: Desktop `71:10`, Mobile `71:11`

## Current Homepage inventory

The current public Homepage is `frontend/src/app/(public)/page.tsx` and is composed inside the established Public Shell.

| Section | Current implementation | Current source | Runtime behavior |
|---|---|---|---|
| Public shell | Government header, navigation, mobile navigation, footer, skip link, and `main` landmark | shared components | active and responsive |
| Hero | cautious static Arabic heading and supporting text | static verified copy | active |
| Search | disabled `SearchField` with unavailable message | no API | visible but inactive |
| Quick Services | `ContentState` unavailable/empty presentation | no request is issued | inactive |
| Government Entities | Suspense plus loading/error/empty states and `GovernmentEntityCard` | `GET /api/v1/entities` | active; card is not currently linked |
| Latest Updates | `ContentState` unavailable presentation | no official V1 request is issued | inactive |
| Open Data | unavailable informational panel | no API | inactive |
| Citizen Participation | static verified copy and link to `/complaints` | real existing route | active |

The page does not currently use `site-data.ts` as a production data source. The Government Entity section is the only dynamic Homepage section currently consuming a live public API.

## Frontend adapter and route inventory

### Available and usable

- `getGovernmentEntities()` consumes `GET /api/v1/entities`.
- Entity contracts expose `canonicalPath`; the card component already accepts an optional `href`.
- `getGovernmentEntityBySlug()` supports the canonical entity profile route.
- `getGovernmentServicesForEntity()` and `getGovernmentServiceBySlug()` support the completed entity and service-detail experiences.
- `/prime-ministers-office` is an established canonical public entity route.
- `/services/[slug]` is the established service-detail route.

### Missing or unsuitable for Homepage activation

- There is no general Homepage adapter for `GET /api/v1/services`.
- There is no Frontend adapter for the official verified `GET /api/v1/content` feed.
- Existing `getNews()`, `getAnnouncements()`, `getDecisions()`, and `getDocuments()` methods use compatibility/legacy routes and are not valid sources for Homepage official updates.
- Existing content-detail pages are numeric-ID/compatibility oriented and can fall back to `site-data.ts`; they are not a safe target for official V1 slug/canonical content cards.
- The existing `/services` directory page still contains hardcoded presentation data and must not be treated as a real catalog or used as the production “view all services” destination.
- No Search API/client exists.
- No Open Data API/client exists.

## Runtime and API evidence

The local runtime was checked without mutation after Phase 5D.3 alignment to schema V1–V9.

### Health and public entity

- Backend health: HTTP `200`.
- `GET /api/v1/entities`: HTTP `200`, exactly **1** result.
- Current public entity:
  - UUID: `00000000-0000-0000-0000-000000000001`
  - Arabic name: `رئاسة مجلس الوزراء اليمني`
  - type: `PRIME_MINISTERS_OFFICE`
  - entity status: `ACTIVE`
  - entity-type status: active
  - slug: `prime-ministers-office`
  - API canonical path: `/prime-ministers-office`
- Entity detail by locator: HTTP `200`.
- The canonical Frontend route was already verified end-to-end in Phase 5D.3.
- The record has no populated `officialSourceReference` and no recorded creation actor. This does not invalidate the current public entity eligibility contract, but it leaves a content-governance confirmation condition before treating all profile prose as provenance-complete.

### Government Services

- `GET /api/v1/services?page=0&size=12`: HTTP `200`.
- Returned items: **0**.
- `totalElements`: **0**.
- Therefore no real `PUBLISHED + VERIFIED` service exists for Homepage Quick Services.

### Editorial content

Read-only database counts:

| Type | Publication state | Verification state | Count |
|---|---|---|---:|
| NEWS | PUBLISHED | UNVERIFIED | 3 |
| ANNOUNCEMENT | PUBLISHED | UNVERIFIED | 3 |
| DECISION | PUBLISHED | UNVERIFIED | 3 |
| DOCUMENT | PUBLISHED | UNVERIFIED | 3 |

Verified public editorial records: **0**.

The official public V1 content controller is conditional on `features.unified-content-read.enabled=true`. The local runtime keeps that flag disabled, so `GET /api/v1/content` returns HTTP `404`. Enabling the controller alone would still not make Latest Updates ready because the current records are all `UNVERIFIED`.

Compatibility methods and routes intentionally preserve frozen canary behavior and must not be used by Homepage. Unified-content graduation states remain unchanged:

- NEWS = `UNIFIED`
- ANNOUNCEMENT = `UNIFIED`
- DECISION = `LEGACY`
- DOCUMENT = `LEGACY`

### Search and Open Data

- Source inventory found no Unified Search controller/domain/endpoint.
- Source inventory found no Open Data controller/domain/catalog endpoint.
- Requests to `/api/v1/search` and `/api/v1/open-data` are intercepted by the general security boundary; that response is not evidence of a functional domain API.
- Entity, Services, and Content APIs are not a substitute for a Unified Search contract.

### Participation

- `/complaints` returns HTTP `200`.
- The Frontend client and Backend expose the public support-request submission path.
- Current Homepage copy promises only a channel for comments and inquiries; it does not claim unsupported tracking, service-level targets, or statistics.

## Actual verified data availability

| Domain | Actual usable data now | Evidence | Homepage implication |
|---|---|---|---|
| Government Entity | 1 active PMO entity with canonical path | live API + database | link activation is technically possible |
| Government Services | 0 public services | live API | no Quick Services cards |
| News | 0 verified records | database + V1 visibility contract | no Latest Updates cards |
| Announcements | 0 verified records | database + V1 visibility contract | no Latest Updates cards |
| Decisions | 0 verified records | database + V1 visibility contract | no Latest Updates cards |
| Documents | 0 verified records | database + V1 visibility contract | no Latest Updates cards |
| Search | no API | source inventory | unavailable state only |
| Open Data | no documented API/source | source inventory | unavailable state only |

## Readiness matrix

| Homepage section | Status | Decision basis | Permitted next action |
|---|---|---|---|
| Public Shell | **READY** | established shared shell, skip link, responsive navigation, and verified runtime | retain as-is |
| Hero | **READY** | static copy contains no metrics, unsupported service claims, or invented government facts | retain as-is |
| Government Entities | **READY WITH CONDITIONS** | real active entity and API canonical path exist; card already supports links | link eligible cards only from validated `canonicalPath`; obtain owner confirmation for provenance-incomplete profile copy |
| Quick Services | **HOLD** | contract and cards exist, but public catalog contains zero records and no Homepage list adapter exists | wait for real approved records, then add adapter and section integration |
| Latest Updates | **HOLD** | zero verified records; official V1 controller disabled; no V1 adapter; current detail routes are compatibility/numeric-ID oriented | verify real content, intentionally enable official feed after review, and complete official detail routing/adapter work |
| Unified Search | **HOLD** | no Search API or result contract implementation | keep current disabled state |
| Open Data | **HOLD** | no documented source or API | keep current unavailable state |
| Citizen Participation | **READY** | existing route and submission API; current copy is cautious and supportable | retain as-is |

## Section findings

### Government Entities

`GovernmentEntityCard` can now be linked without constructing a URL from the entity name. The API-provided `canonicalPath` for the current PMO entity is `/prime-ministers-office`, and that route is already implemented and runtime-verified.

Activation conditions:

1. Consume only `canonicalPath` returned by the public Entity API.
2. Accept only a safe internal absolute path; reject missing, external, protocol-relative, query-only, or fragment-only values.
3. Do not synthesize paths from Arabic/English names, type labels, UUIDs, or slugs in the Homepage component.
4. Preserve the current non-link card when no valid canonical path is available.
5. Preserve section-local loading, empty, and error states.

The present entity is already visible on Homepage and already has a public detail route. Adding the canonical link exposes no new record; it only connects two existing public views. The missing source reference remains a governance condition for the profile content, not a routing blocker.

### Quick Services

The Backend contract, public visibility rules, `ServiceCard`, and service-detail route are technically ready. The section is not activation-ready because the runtime has no approved service records. An API returning an empty catalog is a valid result, not permission to populate Figma examples or fixtures in production.

The current unavailable copy is now technically stale: the API exists, but its verified catalog is empty. When this section is next touched, the message should describe the absence of published verified services rather than the absence of an API. That copy correction does not justify activation by itself.

Before activation:

- a content owner must create and approve real services through the governed process;
- the Frontend needs a typed list adapter for `GET /api/v1/services`;
- cards must link only to `/services/{slug}` returned from real records;
- the legacy hardcoded `/services` catalog must be remediated or omitted as a “view all” destination;
- empty/error behavior must remain section-scoped.

### Latest Updates

The editorial verification mechanism is technically complete, but no record is verified. The Homepage cannot use compatibility routes as a substitute. The section therefore remains under both data and integration HOLD.

The current unavailable copy is also stale in one respect: the verification layer now exists. The remaining blockers are absence of verified records, the disabled official V1 reader, missing Frontend adapter, and incomplete official slug/canonical detail routing.

Before activation:

- content owners must verify real published revisions with valid provenance;
- the official public V1 reader must be intentionally enabled after compatibility review;
- the Frontend must consume only the verified V1 feed;
- content cards must target official canonical/slug routes, not numeric compatibility pages or `site-data.ts` fallback pages;
- News/Announcement canary sources and graduation states must remain unchanged unless handled in their own approved process.

### Search

Search remains `HOLD`. Separate domain APIs cannot be queried client-side and presented as a Unified Search replacement. The disabled control and unavailable explanation are the correct current behavior.

### Open Data

Open Data remains `HOLD`. No source, dataset catalog, freshness policy, provenance contract, download endpoint, or public API is documented. The current unavailable panel is safer than rendering Figma sample metrics or invented datasets.

### Hero and Citizen Participation

Both are `READY` as static verified presentations. The Hero contains no figures or unverified claims. Participation links to a real route and describes the supported interaction conservatively.

## Smallest safe activation slice

### Recommended next batch: Phase 5D.5A — Entity Canonical Links only

Scope:

1. Validate the API-provided `canonicalPath` as a safe internal path in the Frontend adapter or a narrowly scoped helper.
2. Pass the valid path to `GovernmentEntityCard.href`.
3. Keep an unlinked card when the path is missing or invalid.
4. Verify the current PMO card resolves to `/prime-ministers-office`.
5. Preserve current loading, empty, and isolated error states.
6. Correct the two stale unavailable-state descriptions only if the Homepage file is already being touched, while keeping both sections inactive.

Explicit exclusions:

- no Quick Services request or cards;
- no Latest Updates request or cards;
- no Search implementation;
- no Open Data implementation;
- no seed, fixture, Backend, Database, or feature-flag change;
- no name-derived URLs;
- no change to Figma.

Acceptance checks:

- the entity card is a semantic keyboard-focusable link with visible focus;
- no nested interactive element is introduced;
- canonical link works at 320, 360, 1024, and 1440 widths;
- missing/invalid canonical paths remain non-interactive without layout breakage;
- entity API failure does not affect the rest of Homepage;
- no horizontal overflow or console warning is introduced.

## Figma impact

Approved Homepage frames `71:10` and `71:11` present an aspirational populated state: active Search, three Quick Services, multiple Government Entities, Latest Updates cards, and a live Open Data panel.

Current classifications:

| Figma difference | Classification | Reason |
|---|---|---|
| Entity card is not linked | defect/activation gap | canonical route and API path now exist |
| Only one entity instead of multiple example cards | runtime-content variation | only one eligible real record exists |
| Quick Services cards absent | intentional due to unavailable verified data | public catalog is empty |
| Latest Updates cards absent | intentional due to unavailable verified data | zero verified records and official feed disabled |
| Search disabled | intentional due to unavailable capability | no Search API |
| Open Data unavailable | intentional due to unavailable capability | no documented data source/API |

Adding the entity canonical link moves the implementation closer to Figma without fabricating card density. Pixel/data parity for held sections is not an acceptance criterion until their real governed data exists.

## Accessibility, SEO, and failure isolation

- The existing public shell provides a working skip link and `main` target.
- Runtime inspection confirmed `lang="ar"`, `dir="rtl"`, a single main landmark, no console errors, and no horizontal overflow at 320, 360, 1024, and 1440 widths.
- `GovernmentEntityCard` uses the existing whole-card Next.js link composition when `href` is provided; keyboard focus and touch-target foundations are already available.
- The canonical entity route should be sourced exclusively from the API to avoid duplicate/name-derived URLs and preserve Entity alias/canonical behavior.
- Government Entity loading/error/empty states are already section-scoped, so an entity API failure does not collapse Hero, Participation, or other Homepage content.
- Any future Services and Latest Updates integration must use independent boundaries so partial API failures do not fail the page.

## Blockers

### Blocking Quick Services

- zero real `PUBLISHED + VERIFIED` services;
- no Homepage services-list adapter;
- legacy hardcoded `/services` catalog is not a valid production destination.

### Blocking Latest Updates

- zero `VERIFIED` editorial records;
- public V1 content reader disabled in runtime;
- no official V1 Frontend adapter;
- existing detail routes remain compatibility/numeric-ID oriented and may use `site-data.ts` fallback.

### Blocking Search

- no Search API, indexing strategy, result contract, or ranking/filter behavior.

### Blocking Open Data

- no approved source, catalog, provenance/freshness contract, or API.

### Conditional Government Entity item

- the current PMO record has no populated official source reference or creation actor; the record is technically public and routable, but the content owner should confirm the master-data provenance before declaring the complete profile content governance-closed.

## Final recommendation

**GO WITH CONDITIONS** for Phase 5D.5A — Entity Canonical Links only.

Proceed only with linking eligible Government Entity cards using validated API-provided `canonicalPath`. Keep Quick Services, Latest Updates, Unified Search, and Open Data inactive. Do not use empty API capability, compatibility routes, Figma examples, or fixtures as a substitute for real approved data.

The next readiness review for Quick Services or Latest Updates should be triggered by a material data event—at least one real approved service or editorial record—not merely by additional Frontend work.

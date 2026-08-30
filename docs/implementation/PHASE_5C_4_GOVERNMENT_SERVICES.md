# Phase 5C.4 — Government Services Domain

Status: **READY TO COMMIT**

Date: 2026-08-30

Starting checkpoint: `05dffc7ef7c40c10105f6f8cf54751e7159d6d4b` on `main`

## 1. Outcome

Phase 5C.4 adds a bounded Government Services catalog domain. It supplies the minimum trusted backend contract needed by a future service catalog, service detail page, Entity → Services section, and Homepage quick-services composition.

This phase does **not** implement a transactional e-government platform. It does not accept applications, payments, appointments, identity evidence, uploaded documents, ministry integrations, or case tracking. It also does not change Frontend pages, Figma, Unified Search, Open Data, or unified-content graduation states.

No production-like Government Service record is inserted. Public responses remain empty until an authorized administrator creates a record, publishes it, and verifies it against a reviewable official source.

## 2. Domain boundary

`GovernmentService` is an official catalog record owned by one active `GovernmentEntity`.

The domain owns:

- stable identity and public slug;
- Arabic-first names and optional descriptions;
- one owning Government Entity;
- minimal lifecycle and verification state;
- reviewable provenance;
- ordered eligibility, requirement, and step items;
- optional delivery-channel metadata;
- optional fees, processing-time text, and official external URL through a channel;
- public list/detail projections and scoped administrative mutations.

The domain deliberately does not own:

- applications or applicant accounts;
- workflow/case state;
- payments, appointments, uploads, or identity checks;
- fulfillment-system integrations;
- service usage statistics or invented availability claims;
- content relationships to News, Announcements, Decisions, or Documents;
- search indexing.

## 3. Final data contract

### 3.1 Core record

| Field | Required | Public | Rule |
|---|---:|---:|---|
| `id` | yes | yes | Server-generated stable UUID. |
| `slug` | yes | yes | Globally unique, lowercase ASCII kebab-case, immutable after creation. |
| `officialNameAr` | yes | yes as `officialName` | Trimmed Arabic-first official label; up to 255 characters. |
| `officialNameEn` | no | yes | Optional; no translated value is invented. |
| `summaryAr` | no | yes as `summary` | Optional short summary; up to 1000 characters. |
| `descriptionAr` | no | detail only | Optional long description. |
| `owningEntity` | yes | yes as compact owner | Existing Government Entity; public visibility additionally requires an active owner and active entity type. |
| `feesAr` | no | detail only | Official free-text statement; absence remains `null`. |
| `processingTimeAr` | no | detail only | Official free-text statement; absence remains `null`. |
| lifecycle state | yes | no | `DRAFT`, `PUBLISHED`, or `ARCHIVED`. |
| verification state | yes | no | `UNVERIFIED`, `VERIFIED`, or `REJECTED`. |
| provenance | required only for `VERIFIED` | detail source summary | Source type and verification timestamp; an official HTTPS reference is public, while administrative/import identifiers and actor are admin-only. |
| publication/audit timestamps | managed | selected public timestamps | Set by the server. |
| actor IDs/version | managed | admin only | Not mass assignable and not exposed through public DTOs. |

Descriptive and service-detail fields are optional because the repository currently has no approved Government Service dataset. The implementation never manufactures a fallback description, fee, time, channel, or official URL.

### 3.2 Owner projection

Public service responses expose only:

```text
ownerEntity: {
  id: UUID,
  officialName: string,
  canonicalPath: string
}
```

The canonical entity path is reused from the Phase 5C.3 entity profile contract. No assumption is made that the owner is a ministry; any supported active Government Entity type can own a service.

### 3.3 List projection

Each `ServiceSummaryResponse` contains:

```text
id, locale="ar", slug, canonicalPath,
officialName, officialNameEn?, summary?,
ownerEntity, channels[], updatedAt
```

`channels` is an enum summary only; detail-specific instructions and action URLs are returned by the detail contract.

### 3.4 Detail projection

`ServiceDetailResponse` extends the summary with:

```text
description?, eligibility[], requirements[], steps[],
fees?, processingTime?, channels[], source,
publishedAt, updatedAt
```

Missing optional sections are represented as empty arrays or `null`, not invented copy.

## 4. API endpoints

### 4.1 Public API

| Method and path | Purpose | Query |
|---|---|---|
| `GET /api/v1/services` | Public service catalog | Optional `entityId`; `page` defaults to 0; `size` defaults to 20 and is capped at 100. |
| `GET /api/v1/services/{uuid}` | Detail by stable UUID | None. |
| `GET /api/v1/services/by-slug/{slug}` | Detail by public locator | None. |
| `GET /api/v1/entities/{entityId}/services` | Entity-owned service catalog | `page` and `size`. |

Catalog ordering is deterministic: official Arabic name ascending, then UUID ascending. No speculative filters or user-selectable sorting were added.

All four endpoints return only records meeting every public gate:

1. service lifecycle is `PUBLISHED`;
2. service verification is `VERIFIED`;
3. owning entity is `ACTIVE`;
4. owning entity type is active.

An absent, non-public, unverified, rejected, archived, or owner-ineligible service is indistinguishable through detail lookup and returns the unified V1 `404` contract. An inactive or unknown `entityId` filter also returns unified `404`.

The directory envelope is:

```text
items[], page, size, totalElements, totalPages
```

### 4.2 Administrative API

| Method and path | Purpose | Permission scope |
|---|---|---|
| `POST /api/v1/admin/services` | Create a draft catalog record and ordered detail sections | `services.write` or `services.manage` on the owning entity |
| `PUT /api/v1/admin/services/{id}` | Replace editable profile/detail fields | `services.write` or `services.manage`; both old and new owner scopes are checked when ownership changes |
| `PUT /api/v1/admin/services/{id}/publication` | `PUBLISH` or `ARCHIVE` | `services.publish` or `services.manage` on the owner |
| `PUT /api/v1/admin/services/{id}/verification` | Set `UNVERIFIED`, `VERIFIED`, or `REJECTED` | `services.publish` or `services.manage` on the owner |

The existing current-actor, entity-assignment authorization, permission, and audit infrastructure is reused. No new permissions framework was created. Standard seed roles receive only service-domain permissions; no service records are seeded.

There is intentionally no administrative bulk import, delete, list, search, or transactional action in this phase.

### 4.3 Error behavior

The controllers are annotated with the shared V1 API contract and therefore use the unified error envelope.

| Status | Use |
|---:|---|
| `400` | Bean validation, invalid page/size, invalid slug, unsafe URL, invalid provenance, or component limit violation |
| `401` | Missing authenticated actor on admin operations |
| `403` | Actor lacks the required entity-scoped permission |
| `404` | Missing or non-public service/entity |
| `409` | Duplicate/changed immutable slug or invalid lifecycle transition |

## 5. Publication and verification model

### 5.1 Lifecycle

- `DRAFT`: editable administrative record, never public.
- `PUBLISHED`: technically published, but still not public until independently `VERIFIED`.
- `ARCHIVED`: withdrawn from public APIs.

### 5.2 Verification

- `UNVERIFIED`: default for every newly created record.
- `VERIFIED`: approved for public catalog use with complete provenance.
- `REJECTED`: explicit negative verification decision; not public.

`PUBLISHED` never means `VERIFIED`. Publishing or re-publishing resets verification to `UNVERIFIED`. Any profile/detail update resets verification to `UNVERIFIED`. Archiving also clears verification. This ties approval to the exact reviewed catalog state and prevents stale approval after edits.

### 5.3 Provenance

The minimal source taxonomy reuses the concepts—not the content workflow—from Phase 5C.2:

- `OFFICIAL_MANUAL_ENTRY`
- `OFFICIAL_SOURCE_REFERENCE`
- `APPROVED_IMPORT`

`VERIFIED` requires a type and reference. An official URL reference must be an absolute HTTPS URL without embedded user information. Administrative/manual and approved-import references must be stable identifiers rather than unrestricted prose. `UNVERIFIED` and `REJECTED` must have no provenance fields. Public detail exposes the reference only for `OFFICIAL_SOURCE_REFERENCE`; internal manual/import identifiers and the verification actor remain administrative data.

The database constraint and service policy enforce the same consistency rule. There is no automatic verification of seed, test, backfill, or imported data.

## 6. Database schema and migration

Migration `V9__government_services.sql` is additive and creates no records.

### 6.1 `government_services`

Stores the UUID, owner FK, immutable unique slug, names, optional descriptive fields, lifecycle, verification/provenance, publication timestamps, actors, audit timestamps, and optimistic-lock version.

Key safeguards:

- foreign key to `government_entities`;
- actor foreign keys to existing users;
- unique slug;
- lifecycle/source/verification checks;
- a consistency check requiring a published lifecycle and complete source evidence for `VERIFIED`;
- public-query and owner/name indexes.

### 6.2 `government_service_detail_items`

Stores ordered, typed items:

- `ELIGIBILITY`
- `REQUIREMENT`
- `STEP`

Each row contains a required Arabic title, optional description, and positive display order. A unique `(service, section, display_order)` constraint preserves deterministic ordering.

### 6.3 `government_service_channels`

Stores optional ordered delivery metadata:

- `ONLINE`
- `IN_PERSON`
- `PHONE`

Each channel may contain an Arabic label, instructions, and an HTTPS action URL. A unique service/order constraint preserves presentation order.

Migration characteristics:

- no deletion or mutation of prior schema/data;
- no edits to migrations V1–V8;
- no production-like service fixtures;
- safe empty initial public catalog;
- rollback, if explicitly approved before production data exists, is limited to removing the three new tables in child-first order. No automatic rollback is implemented.

## 7. Entity relationship

The implemented relationship is many services to one owning entity:

```text
GovernmentEntity 1 ─── * GovernmentService
```

One owner is sufficient for the approved screens and current data contract. A many-to-many responsibility model has no validated requirement and was not introduced. Ownership changes require permission on both the current and requested owner scopes and reset verification.

The schema is ready for a future Service → Entity query and public Entity services section without changing content relationships.

## 8. Slug strategy

Service slugs are:

- global and unique;
- lowercase ASCII kebab-case;
- independent from later Arabic/English name changes;
- immutable after creation;
- exposed through canonical paths shaped as `/services/{slug}`.

Because mutation is prohibited, Phase 5C.4 does not need an alias/history table. If a later governance requirement permits slug replacement, an additive alias table can preserve old paths; no speculative redirect subsystem is included now.

The UUID endpoint remains available as a stable machine locator.

## 9. Service-detail representation decision

Eligibility, requirements, and steps are not stored in a JSON blob. They share one typed ordered relational table because they have the same validation and presentation shape. This preserves order, permits missing sections, keeps Arabic strings directly queryable, and avoids three nearly identical tables.

Delivery channels use a separate typed ordered table because channel fields and validation differ from instructional items. Fees and processing time remain nullable scalar Arabic text because current requirements do not justify fee line items, currency calculations, or duration normalization.

This is the smallest representation that supports the approved Service Detail composition without premature workflow or commerce modeling.

## 10. Frontend mapping

No Frontend file changed. The contracts are shaped for a later adapter or direct binding:

| Frontend composition | Contract source |
|---|---|
| `ServiceCard` | `ServiceSummaryResponse` |
| Homepage quick services | first owner-approved items from `GET /api/v1/services`; no hard-coded fixture fallback |
| Entity services section | `GET /api/v1/entities/{entityId}/services` |
| Service Detail | `ServiceDetailResponse` |
| source/provenance presentation | public `source` object |
| optional/empty UI sections | nullable scalars and empty ordered arrays |

`site-data.ts` and Figma example content remain prohibited as production sources.

## 11. Security considerations

- **Mass assignment:** request DTOs do not accept UUID, lifecycle, verification, actor, timestamp, or version fields.
- **Authorization:** every admin mutation uses the existing authenticated actor and entity-scoped permission checks. Owner transfer checks both scopes.
- **Public/private separation:** public DTOs omit lifecycle, verification actor, internal actor IDs, and optimistic-lock metadata.
- **Unsafe URLs:** stored external action/source URLs must be parseable absolute HTTPS URLs and cannot contain user-info credentials. URLs remain data; the backend does not render or trust them as HTML.
- **Enum and size validation:** request enums, strings, page size, section count, and channel count are constrained.
- **Visibility:** public queries enforce both service approval and owning-entity eligibility.
- **Auditability:** create, update, publish/archive, verification decisions, and rejected invalid lifecycle decisions use the existing audit service and optional correlation ID.
- **Information leakage:** detail lookup uses a uniform public 404 for missing and non-public records.

Residual risks for later phases:

- external URLs are syntactically validated but not periodically checked for liveness or domain ownership;
- there is no scheduled re-verification/expiry policy;
- slug changes are deliberately unsupported rather than aliased;
- Arabic is the only authoritative locale in this phase; optional English is pass-through data, not a translation workflow;
- no rate-limit or search index was added; platform-level controls remain separate concerns.

## 12. Compatibility

The module and routes are additive. Existing entity and content response contracts are unchanged. The only organization service exposure is reuse of its existing canonical-path calculation.

No compatibility flag or unified-content projection was changed. Graduation/routing remains:

- NEWS: `UNIFIED`
- ANNOUNCEMENT: `UNIFIED`
- DECISION: `LEGACY`
- DOCUMENT: `LEGACY`

Services are not added to unified content and do not affect its canary counters, shadow comparisons, fallbacks, or publication state.

## 13. Tests and verification

`GovernmentServiceIntegrationTest` uses clearly test-only fixtures and covers:

- public list, UUID detail, slug detail, and Entity filter;
- verified/published visibility and ordered detail/channel output;
- unified 404 for missing and hidden records;
- draft, unverified, rejected, and inactive-owner exclusion;
- update-driven verification reset;
- immutable/unique slug and name-change stability;
- long Arabic names and descriptions;
- missing optional sections without fallback content;
- unsafe URL and invalid provenance rejection;
- anonymous `401` and unauthorized entity actor `403`;
- audit-event creation.

Verification results on 2026-08-30:

- targeted Phase 5C.4 integration suite: **7/7 PASS**;
- full Maven reactor tests: **100/100 PASS**, **BUILD SUCCESS** across 14 modules;
- Flyway validation/application: **V1–V9 PASS** on H2 test databases;
- Hibernate schema validation with the new module: **PASS**.

## 14. Deferred functionality

Explicitly deferred:

- Government Service Frontend pages and route activation;
- Homepage quick-services activation;
- Ministry/Entity UI implementation;
- applications, payments, appointments, identity checks, uploads, integrations, and case tracking;
- Unified Search and indexing;
- Open Data;
- service/content relationships;
- imports, bulk editing, deletion, analytics, rankings, and usage statistics;
- multilingual authoring workflow;
- external link monitoring and periodic re-verification.

## 15. Readiness after Phase 5C.4

| Consumer | Readiness | Evidence / remaining condition |
|---|---|---|
| Homepage quick services | **READY WITH CONDITIONS** | Public list and card-ready summary now exist. Requires real owner-approved `PUBLISHED + VERIFIED` records, Frontend binding, and explicit activation approval. |
| Ministry/Entity services section | **READY WITH CONDITIONS** | Entity-scoped endpoint and ownership relationship exist. Requires real verified records and Ministry/Entity Frontend implementation. |
| Service Detail | **READY WITH CONDITIONS** | Backend detail contract, optional sections, source evidence, slug, UUID lookup, and error behavior are complete. The Frontend page remains intentionally unimplemented. |
| Unified Search | **HOLD** | No search contract/index implementation was authorized or added. Services can become a later result source only after the search architecture is approved. |

## 16. Recommendation

**READY TO COMMIT**

The catalog domain is bounded, additive, source-aware, entity-scoped, and verified by the full backend suite. A checkpoint may include only the intended source, migration, test, and this document; generated `target/**`, historical review images, design-state artifacts, Frontend files, and unrelated changes must remain excluded.

This recommendation does not authorize Frontend activation or claim that any Government Service data is available. Public UI sections remain conditional on the creation and explicit verification of real official records.

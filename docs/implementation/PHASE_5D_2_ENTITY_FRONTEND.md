# Phase 5D.2 — Government Entity Frontend Integration

## 1. Outcome

Phase 5D.2 adds a real, server-rendered public Government Entity profile that reads only from the Phase 5C.3 Entity Profile API and the Phase 5C.4 Government Services API. It does not use `site-data.ts`, Figma copy, local fixtures, or hard-coded government records.

The implementation is **READY WITH CONDITIONS** and is recommended **READY TO COMMIT**. The route, API adapters, states, accessibility structure, and responsive composition are complete. Full Ministry visual/data validation remains conditional because the current local environment contains only the real Prime Minister's Office record and its running backend image does not yet expose the V9 services endpoint or verified services.

## 2. Final public routing

### General entity locator

- Public route: `/{publicPathSegment}/{slug}`
- Next.js route: `frontend/src/app/(public)/[publicPathSegment]/[slug]/page.tsx`
- API lookup: `GET /api/v1/entities/by-slug/{publicPathSegment}/{slug}`

Examples defined by the backend contract include `/ministries/{slug}`, `/authorities/{slug}`, `/independent-entities/{slug}`, and `/governorates/{slug}`. The frontend does not assume that every Government Entity is a ministry.

### Single-segment canonical entity

The `PRIME_MINISTERS_OFFICE` contract has the canonical path `/prime-ministers-office`, although its API locator remains `(prime-ministers-office, prime-ministers-office)`. A thin explicit route at `frontend/src/app/(public)/prime-ministers-office/page.tsx` normalizes that path into the same API locator and reuses the same profile implementation. The exception is deliberately static so unrelated single-segment namespaces are not captured by a generic entity route.

Static public routes such as `/services`, `/about`, `/contact`, and content routes retain Next.js precedence over the generic entity routes.

## 3. Canonical and alias behavior

- The frontend derives an expected canonical path only from safe lowercase, URL-safe API locator fields.
- It accepts `entity.canonicalPath` only when it exactly matches the path dictated by the returned entity type and current slug.
- An alias or non-canonical locator that resolves successfully is permanently redirected with Next.js `permanentRedirect` to the trusted current canonical path.
- `generateMetadata` uses the real Arabic official name, the real optional description, and a canonical alternate only after the same validation.
- No redirect table or frontend alias subsystem was introduced.

Runtime evidence: requesting `/prime-ministers-office/prime-ministers-office` against the real local entity API redirected to `/prime-ministers-office` and retained the entity profile.

## 4. API mapping

### Entity profile

`GET /api/v1/entities/by-slug/{publicPathSegment}/{slug}` maps to:

| API field | UI use |
|---|---|
| `officialName` | Page title, breadcrumb, `h1`, metadata title |
| `officialNameEn` | Optional LTR English name |
| `type.name` | Entity-type badge and non-link breadcrumb item |
| `description` | Optional identity summary |
| `mandate` | Optional entity mandate section |
| `websiteUrl` | Optional HTTPS-only external official link |
| `contact.email` | Optional plain public contact value |
| `contact.phone` | Optional plain public contact value |
| `contact.address` | Optional address |
| `officialSourceReference` | Optional provenance reference displayed as data, not trusted HTML |
| `updatedAt` | Optional localized update metadata when parseable |
| `parent.officialName` | Optional parent-entity metadata without an invented link |
| `canonicalPath` | Validated canonical metadata and redirect target |

Blank optional strings are normalized away before rendering. Empty headings or empty sections are not emitted.

### Entity services

`GET /api/v1/entities/{entityId}/services?page=0&size=12` maps to the existing `ServiceCard` composition:

| API field | UI use |
|---|---|
| `officialName` | Service-card title |
| `summary` | Optional service-card description |
| `ownerEntity.officialName` | Provider label, with the loaded entity name only as the same-record fallback |
| `channels[]` | Optional Arabic delivery-channel category labels |
| `slug` | Link to the existing `/services/{slug}` detail route |

The public API remains the authority for visibility and returns only services satisfying `PUBLISHED + VERIFIED + active owning entity`. The frontend does not recreate or weaken that rule.

## 5. Reused components

The phase reuses the established design system and does not duplicate primitives:

- Public Shell and its working `main#main-content`/Skip Link boundary
- `PageContainer`
- `Section`
- `SectionHeader`
- `Breadcrumbs`
- `Badge`
- `TextLink`
- `MetadataList`
- `ServiceCard`
- `CardGrid`
- `ContentState`

Only route-specific page composition, CSS Module styles, and small API types/fetchers were added.

## 6. Displayed and deferred sections

### Implemented when data exists

- Breadcrumb
- Entity type and identity
- Official Arabic name
- Optional English name
- Optional description
- Optional mandate
- Optional official website
- Optional contact/address information
- Public verified services
- Optional official source reference
- Optional update and parent metadata

### Intentionally hidden/deferred

The following Figma content is not rendered because the current entity contract does not provide authoritative data for it:

- Minister, leadership, or officeholder profile
- Government statistics or counters
- Logo/emblem imagery
- Social links
- Invented responsibilities or achievements
- Illustrative subordinate-entity cards
- Illustrative related news, announcements, decisions, documents, or resource downloads
- Any service record not returned by the verified public services API

No Figma demonstration content was promoted to production data to achieve visual density.

## 7. Runtime states

### Entity loading

The route-level `loading.tsx` presents a labeled loading state and is shared by the one- and two-segment entity routes.

### Entity 404

An Entity API `404` invokes the localized route-level not-found state. It does not expose a backend message and explains that the entity may not exist or may not be eligible for public display.

### Entity API error

Non-404 failures produce a generic, localized error state and an encoded retry link. No local/fake entity fallback is used and internal exception text is never rendered.

### Services loading

Services use an independent `Suspense` boundary and loading presentation.

### Services empty

A successful service directory with no items produces an explicit empty state explaining that no public published and verified services were returned.

### Services API error

Failure of the services endpoint produces an error state only inside the services section. The entity identity and available profile information remain visible.

This isolation was verified against the current local Docker backend: the real entity detail returned `200`, while the older running image returned `404` for the not-yet-deployed entity-services endpoint; the profile stayed visible and only the services section reported unavailability.

## 8. External-link and data safety

- `websiteUrl` is rendered only when it parses as an HTTPS URL.
- External links use `target="_blank"` with `rel="noopener noreferrer"` and state that a new window opens.
- Source references, email, phone, and address are rendered as text/data, never as HTML.
- Email and phone values use bidirectional isolation where appropriate.
- Route and API locator parts are encoded.
- No internal identifier is exposed except where the server-only API adapter uses the entity UUID to request its service directory.
- Backend error bodies are not shown to users.

## 9. Accessibility verification

- The page is RTL-first under the existing Arabic public shell.
- The entity name is the single page `h1`; section headings follow the established `SectionHeader` hierarchy.
- The profile is an `article`, the identity is a labeled section, contact information uses a semantic `dl`, and service cards remain semantic links.
- The inherited Skip Link targets the real `main#main-content`, which remains focusable with `tabIndex=-1`.
- Existing primitives preserve visible focus and at least 48px active targets; browser measurement found no visible page links/buttons below 48px at 320, 360, 1024, or 1440 widths.
- External-link behavior is explicitly announced in link text and does not rely on color alone.
- Long/mixed-direction values use logical CSS, `overflow-wrap`, and `bdi`/`dir` handling.
- Reduced-motion behavior is inherited from Foundations; this page adds no animation and explicitly avoids smooth scrolling under reduced motion.
- Browser console inspection produced no warnings or errors for the real entity, entity 404, or isolated services-error states.

## 10. Responsive verification

Browser/runtime checks were completed at 320, 360, 1024, and 1440 CSS pixels:

| Width | Entity visible | Services isolated | Horizontal overflow | Visible target below 48px |
|---:|---|---|---|---|
| 320 | PASS | PASS | none | none |
| 360 | PASS | PASS | none | none |
| 1024 | PASS | PASS | none | none |
| 1440 | PASS | PASS | none | none |

The CSS uses logical properties, `min-inline-size: 0`, wrapping badges, `overflow-wrap: anywhere`, responsive tokenized gaps, and single/two-column contact layouts. Missing description, contact, mandate, source, and update data remove their respective optional content cleanly. Full runtime testing with an extreme verified Ministry record and multiple verified services remains a data-dependent validation gate; no production-like fixture was created to simulate it.

## 11. Figma comparison

Source screens:

- Ministry Desktop `71:13`
- Ministry Mobile `71:14`

### Matched hierarchy and system behavior

- Existing Government Header/Footer and container system
- Breadcrumb and entity identity hierarchy
- Light civic-blue identity area with contained white profile surface
- Civic Blue interaction treatment and limited National Red shell accent
- Responsive single-column mobile composition
- Profile information followed by a services section
- Reused cards/states rather than page-specific duplicates

### Intentional deviations caused by unavailable verified data

- No ministry logo, minister profile, statistics, subordinate entities, related content, or resources are shown.
- The current real record is the Prime Minister's Office, not a Ministry record.
- The current running Docker backend predates the V9 services endpoint, so the verified-services card grid cannot be visually populated in this environment.

### Runtime-content variation

- Entity names, descriptions, contact blocks, mandate, metadata, and service-card count/length are API-driven.
- Optional sections change page height and density instead of preserving empty Figma placeholders.

No confirmed layout defect was found in the available real-data state. Pixel-level validation of all Figma content zones is not claimed without an authoritative Ministry record and verified services.

## 12. Verification results

- `npm run lint`: PASS
- `npx tsc --noEmit`: PASS
- `npm run build`: PASS
- Next route collection includes `ƒ /prime-ministers-office` and `ƒ /[publicPathSegment]/[slug]`
- Real local Entity API lookup: PASS (`200`)
- Canonical permanent redirect: PASS
- Localized entity 404 without internal details: PASS
- Entity API error without internal details or fake fallback: PASS
- Services failure isolation: PASS
- 320/360/1024/1440 responsive checks: PASS for the available real-data state
- RTL/main landmark/semantic heading inspection: PASS
- Browser warnings/errors: none observed
- `git diff --check` on the intended source/documentation scope: run as the final handoff gate

## 13. Current data limitation and readiness

The frontend implementation does not lift the data-governance gate by itself:

- No real Ministry record is currently present in the local public directory.
- No verified public service can be validated through the running local Docker backend until its schema/image includes Phase 5C.4/V9 and an owner-approved service record exists.
- No unsupported Figma section has a public data contract.

Therefore:

- **Government Entity / Ministry frontend:** `READY WITH CONDITIONS`
- **Recommendation:** `READY TO COMMIT`

The next approval should preserve the implementation as-is and treat creation/verification of authoritative entity and service records as a separate data-governance task, not a frontend fallback task.

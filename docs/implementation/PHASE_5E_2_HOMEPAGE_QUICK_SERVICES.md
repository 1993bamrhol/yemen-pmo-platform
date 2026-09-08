# Phase 5E.2 — Homepage Quick Services Activation

**Date:** 2026-09-08

**Branch:** `main`

**Baseline checkpoint:** `11bdd1b4658831819324bbb146b2b33fe2d6e557`

**Scope:** Homepage Quick Services activation only.

## Outcome

**Recommendation: READY TO COMMIT.**

The Homepage Quick Services section now reads the public Government Services directory and renders only records returned by that API. The Backend remains the authority for the `PUBLISHED + VERIFIED + active owning entity` visibility rule. No service data, visibility status, route, or fallback is synthesized by the Frontend.

The runtime check at `2026-09-08T14:12:55+03:00` returned exactly one public service, and the Homepage displayed exactly that one service.

## Files changed

- `frontend/src/app/(public)/page.tsx`
- `frontend/src/lib/api.ts`
- `docs/implementation/PHASE_5E_2_HOMEPAGE_QUICK_SERVICES.md`

No CSS, shared component, Backend, Database, migration, feature flag, Figma file, or production record was changed.

## API source

The new directory adapter uses:

```text
GET /api/v1/services?page=0&size=3
```

The existing Service Detail integration continues to use:

```text
GET /api/v1/services/by-slug/{slug}
```

The Homepage does not use `site-data.ts`, compatibility endpoints, fixtures, hard-coded services, or a local fallback. The requested page size of three limits Homepage composition to the established Quick Services presentation; the actual number shown is always the number of valid records returned, up to that limit.

## Runtime data evidence

The live public directory returned:

| Field | Runtime value |
|---|---|
| `totalElements` | `1` |
| returned items | `1` |
| `officialName` | `الاستعلام عن المعاملات` |
| `slug` | `transaction-status-inquiry` |
| `canonicalPath` | `/services/transaction-status-inquiry` |
| summary | `تتبع حالة معاملتك الرسمية بسهولة وأمان` |
| owning entity | `رئاسة مجلس الوزراء اليمني` |

The detail endpoint returned the same official name, slug, canonical path, and owning entity.

## Filtering and visibility contract

- The Backend public repository/query controls publication, verification, and owning-entity visibility.
- The Frontend does not duplicate or infer lifecycle or verification state.
- A returned item is rendered only when it has a non-empty official name and a valid internal `canonicalPath`.
- The canonical path is passed to `ServiceCard` unchanged after the existing strict internal-path allow-list validation.
- A missing or malformed canonical path is not reconstructed from the slug, name, owner, or any fallback.
- The Homepage does not cache or display stale/fake service data after an API failure.

## Reused design-system components

- `ServiceCard` for the service presentation and whole-card link.
- `CardGrid` for semantic list composition and responsive layout.
- `ContentState` for loading, empty, and error presentations.
- `Section` and `SectionHeader` for the existing Homepage structure.
- Foundations and semantic tokens already used by those components.

No duplicate card, grid, loader, error component, CSS module, or inline style was added. The repository-wide Frontend inline-style count remains `111`.

## Runtime states

### Loading

The service fetch is isolated in its own React `Suspense` boundary. `QuickServicesLoading` renders the existing three-item `ContentState` skeleton with the accessible label `جارٍ تحميل الخدمات الحكومية السريعة`. The loading presentation was observed during the initial streamed Homepage load.

### Success

The live runtime returned one service. The section rendered one `ServiceCard` containing the API-provided owning entity, official Arabic name, summary, and canonical service path.

### Empty

An isolated local test proxy returned a valid empty service directory while continuing to proxy the real Entity API. The section rendered:

- title: `لا توجد خدمات معتمدة حاليًا`;
- description explaining that no public published and verified service was returned;
- zero service links.

The Hero, Government Entities section, and the rest of the Homepage remained rendered. No production data was changed.

### API error

The same isolated proxy returned `503` for the Services directory only while continuing to proxy the real Entity API. The section rendered:

- title: `تعذر تحميل الخدمات الحكومية`;
- a user-safe message with no Backend internals;
- zero cached or fallback cards.

The Hero, Government Entities section, and the rest of the Homepage remained rendered. Empty and error states are therefore distinct.

The proxy and alternate Next.js server were temporary test infrastructure only; they did not enter source, migrations, or production data.

## End-to-end navigation evidence

The real local runtime flow passed:

```text
Homepage
→ الاستعلام عن المعاملات
→ /services/transaction-status-inquiry
```

Evidence:

- the Homepage accessible tree contained exactly one Quick Services link;
- its destination was the API-provided `/services/transaction-status-inquiry`;
- keyboard `Enter` on the focused card navigated to that path;
- Service Detail displayed `الاستعلام عن المعاملات` and `رئاسة مجلس الوزراء اليمني`, matching the directory response;
- browser back navigation returned normally to the Homepage;
- no 404 or redirect loop occurred.

## Responsive verification

The optimized production build was exercised with the real API:

| Viewport | Cards | Document overflow | Card target size | Result |
|---:|---:|---|---|---|
| 320px | 1 | none (`320 / 320`) | `288 × 290px` | PASS |
| 360px | 1 | none (`360 / 360`) | `328 × 262px` | PASS |
| 1024px | 1 | none (`1009 / 1009`) | `460.5 × 262px` | PASS |
| 1440px | 1 | none (`1425 / 1425`) | `370.7 × 262px` | PASS |

The reduced content count does not create a layout error: the grid presents one genuine card and leaves the remaining grid area empty rather than fabricating records.

## Accessibility verification

- Document direction remained `rtl` at all four viewport widths.
- The service card is one native link, not a simulated interactive container.
- The card contains zero nested anchors or buttons.
- Keyboard tab order reached the service card; `Enter` activated it.
- Computed focus indicator was a solid `3px` outline.
- The whole-card target exceeded the required `48px` minimum at every tested width.
- The accessible label contained the owning entity, service name, summary, and action text.
- Existing skip link, headings, landmarks, and reduced-motion behavior were unchanged.
- No application JavaScript console errors were observed in the production-runtime checks.

One pre-existing cold-browser request for the absent `/favicon.ico` returned `404`; it is unrelated to Quick Services and no favicon/layout change was made in this narrowly scoped batch.

## Figma comparison

Approved Homepage references:

- Desktop `71:10`
- Mobile `71:11`

The section reuses the approved card-grid hierarchy, Civic Blue interactions, Noto Sans Arabic foundation, RTL layout, spacing, and responsive composition.

The only material content deviation is intentional: Figma illustrates three service cards, while the verified public runtime currently contains one service. The implementation displays one card because production data has priority over the illustrative card count. No Figma sample was copied to fill the two vacant positions.

## Quality gates

| Check | Result |
|---|---|
| `npm run lint` | PASS |
| `npx tsc --noEmit` | PASS |
| `npm run build` | PASS |
| `git diff --check` on intended source | PASS; only the repository's existing LF/CRLF warning |
| Real public Services API | PASS; `1` verified public record returned |
| Homepage → Service Detail | PASS |
| Empty/Error isolation | PASS |
| 320/360/1024/1440 overflow | PASS |
| RTL and keyboard focus | PASS |
| Nested interactive elements | `0` |
| New inline styles | `0` |
| Duplicate components | `0` |

## Remaining HOLD sections

This phase does not change the readiness or behavior of:

- Latest Updates — remains `HOLD` pending real editorially verified content activation.
- Unified Search — remains `HOLD` because no trusted Search API exists.
- Open Data — remains `HOLD` because no documented public source/API exists.

Government Entities, Hero, Citizen Participation, Public Shell, and all other Homepage behavior remain unchanged.

## Risks and follow-up

- The runtime currently has only one verified public service; full multi-card data variation cannot be validated until additional official records are approved.
- The API contract has no explicit Homepage featured/priority field. The current `page=0&size=3` behavior is sufficient for the sole record, but future curation should be specified by the Backend contract rather than inferred in the Frontend.
- The existing missing favicon produces one unrelated network `404` on a cold browser profile; resolving it belongs in a separate, explicitly scoped polish batch.

## Final recommendation

**READY TO COMMIT.**

Quick Services can be removed from `HOLD`: it is backed only by the public Government Services API, renders exactly the verified runtime data, isolates loading/empty/error behavior, preserves the rest of the Homepage, and passes build, responsive, RTL, keyboard, and end-to-end checks.

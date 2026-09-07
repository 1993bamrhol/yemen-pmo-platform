# Phase 5D.5A — Entity Canonical Links

**Date:** 2026-09-07

**Branch:** `main`

**Baseline checkpoint:** `fc95f3dd3b3f746244f2f280d75f2e8117dfc8e8`

**Scope:** Homepage Government Entity canonical links only.

## Outcome

**Recommendation: READY TO COMMIT.**

Homepage Government Entity cards now become links only when the public Entity API returns a valid internal `canonicalPath`. The API value is passed through unchanged after validation. No URL is generated from an entity name, local slug, entity type, UUID, or fallback.

The current PMO record links to `/prime-ministers-office`, and the real route was verified end-to-end against the local V1–V9 runtime.

## Files changed

- `frontend/src/app/(public)/page.tsx`
- `docs/implementation/PHASE_5D_5A_ENTITY_CANONICAL_LINKS.md`

No shared component, styling, Backend, Database, migration, feature flag, or Figma file was changed.

## Existing implementation reused

- `api.getGovernmentEntities()` remains the sole data source for the Homepage entity section.
- `GovernmentEntityCard` already accepts an optional `href`.
- `LinkedContentCard` already renders:
  - a semantic Next.js `Link` when `href` exists;
  - a non-interactive `article` when `href` is absent.
- Existing card styles already provide whole-card interaction, responsive sizing, semantic tokens, RTL behavior, reduced-motion handling, and the global visible focus foundation.

No component or CSS duplication was introduced.

## Canonical path contract

The Homepage applies a narrow allow-list validation to the API value:

- it must be a string;
- it must be an internal absolute path beginning with one `/`;
- every segment must use the public lowercase URL-safe format already established by the Entity contract;
- protocol-relative, external, query-only, fragment-only, malformed, empty, and missing values are rejected.

When valid, the exact `canonicalPath` string from the API is passed to `GovernmentEntityCard.href` without reconstruction or normalization.

Current runtime example:

```text
API canonicalPath: /prime-ministers-office
Homepage href:     /prime-ministers-office
```

The implementation does not derive a path from:

- `officialName`;
- `slug`;
- `type.code` or `type.pathSegment`;
- `id`;
- any fallback constant.

## Present and missing path behavior

### Valid canonical path

- `GovernmentEntityCard` renders as one semantic anchor.
- The action label `عرض الجهة` is shown.
- The entire card is the interactive target.
- The current PMO card navigates to `/prime-ministers-office`.

### Missing or invalid canonical path

- the helper returns `undefined`;
- `GovernmentEntityCard` renders the existing non-interactive `article` variant;
- no action label is shown;
- no local route is invented;
- layout and entity information remain available.

The live API currently contains only the valid PMO canonical path, so the missing-path branch was verified through code-path review and the existing optional-`href` component contract rather than by altering runtime data.

## Runtime verification

The existing Docker runtime was used without data mutation. The local Frontend development server was started with the real Backend base URL.

| Check | Result |
|---|---|
| Homepage HTTP | `200` |
| Entity route HTTP | `200` |
| Homepage entity name | present |
| Homepage canonical anchor | exactly one |
| Anchor href | `/prime-ministers-office` |
| Entity profile heading | present |
| Keyboard activation | `Enter` navigated successfully |
| Browser URL after activation | `/prime-ministers-office` |
| Back navigation | returned naturally to `/` |
| Browser console errors | `0` |

One existing Next.js development warning about `scroll-behavior: smooth` was observed. It predates and is unrelated to the canonical-link change; no browser console error was recorded.

## Responsive and RTL verification

Browser checks used the real Homepage and current PMO API record:

| Viewport width | RTL | Link present | Horizontal overflow | Link target height |
|---:|---|---|---|---:|
| 320px | PASS | PASS | none | 350px |
| 360px | PASS | PASS | none | 318px |
| 1024px | PASS | PASS | none | 290px |
| 1440px | PASS | PASS | none | 290px |

The whole-card target is substantially larger than the required 48px minimum at every tested width.

## Accessibility verification

- The card is a native anchor, not a `div` with simulated link behavior.
- Keyboard tab order reaches `/prime-ministers-office` after the public navigation links.
- `:focus-visible` is active on the card link.
- Computed focus outline is solid and 3px wide.
- The link has an accessible description containing the entity metadata, name, summary, and action label.
- The card contains zero nested interactive elements.
- RTL remains set on the document.
- No color-only state or new motion was introduced.
- Existing skip-link and page landmarks remain unchanged.

No accessibility issue was found within the Phase 5D.5A scope.

## Build and static verification

| Command | Result |
|---|---|
| `npm run lint` | PASS |
| `npx tsc --noEmit` | PASS |
| `npm run build` | PASS |
| `git diff --check` on intended source | PASS |

The production build includes the dynamic Homepage and the established `/prime-ministers-office` entity route.

## Scope confirmation

Unchanged:

- Quick Services;
- Latest Updates;
- Search;
- Open Data;
- Citizen Participation;
- public shell and navigation;
- Entity API adapter and contracts;
- Backend and Database;
- Figma;
- all existing data.

No fake entity data, fallback URL, seed, or production-like fixture was added.

## Final recommendation

**READY TO COMMIT.**

The change is narrowly scoped, preserves the non-interactive fallback, uses only the API-provided canonical path, and passes routing, keyboard, RTL, responsive, static, and production-build checks.

# Phase 4.2 — Yemeni Civic Identity Refinement

Status:

- National Homepage: approved.
- Government Entity / Ministry and Government Service Detail: completed and pending explicit review approval.

Figma file: `cSFveyYsAe08Xr5kiZMXum`, page `04 — Key Screens` (`71:2`).

## Scope and guardrails

- Preserved the Phase 4.1 information architecture, section order, functionality, component hierarchy, responsive behavior, whitespace, and page dimensions.
- Reused the exact approved abstract Yemeni arch-rhythm motif; no new ornament language was introduced.
- Kept Civic Blue as the primary interaction color. National Red remains a very limited national accent.
- Kept motifs outside cards and task content.
- Did not modify Foundations, Components, Patterns, the approved Homepage, production code, backend, database, or project data.

## Intentional changes

### Government Entity / Ministry

- Replaced the generic entity identity with the verified name `وزارة الصحة العامة والسكان` and neutral platform copy.
- Preserved entity hierarchy by keeping the ministry name and institutional identity dominant over the platform motif.
- Reused the motif only in the entity identity surface, one transition into Key Services, and the footer brand surface.
- Kept subordinate entities, service cards, news, and resources explicitly illustrative; no unverified institutional relationship was presented as fact.
- Corrected a pre-existing overflow in the Ministry Mobile `أقسام الجهة` Accordion title using a screen-level instance override (`layoutGrow = 1`). The source Accordion component and Pattern remain unchanged, and the title is now visible within its 328px header.

### Government Service Detail

- Applied a deliberately lighter identity treatment than the Ministry page.
- Reused the motif only in the Hero whitespace and footer brand surface.
- Kept the CTA, outcome, eligibility, requirements, steps, related services, and support content visually dominant.
- Did not add motifs inside cards, facts, accordions, requirements, or support notices.

## Validation

- Ministry Desktop: `1440 × 3360` — unchanged.
- Ministry Mobile review render: `352 × 4112` (`360 × 4112` Figma frame) — unchanged.
- Service Detail Desktop: `1440 × 3384` — unchanged.
- Service Detail Mobile review render: `352 × 4408` (`360 × 4408` Figma frame) — unchanged.
- Approved Homepage dimensions remain unchanged: Desktop `1440 × 3384`; Mobile frame `360 × 4012`.
- Local variables: `191` — unchanged.
- Fonts: Noto Sans Arabic Regular, SemiBold, and Bold only; no missing fonts.
- RTL titles remain right-aligned.
- Ministry Mobile Accordion title fits its parent: `x 36 + width 276 = 312 ≤ 328`.
- New visible identity paints are bound to existing semantic tokens; no unbound visible paint was found.
- Civic Blue accents use the existing interaction tokens. National Red accents use `national/accent/default` (`VariableID:4:44`).
- Full-frame review PNGs and 2× exports were opened successfully and verified as complete, non-placeholder images.

## Review exports

### National Homepage — approved reference

- `before/homepage-desktop-before.png` — 1440 × 3384
- `before/homepage-mobile-before.png` — 352 × 4012
- `after/homepage-desktop-after.png` — 1440 × 3384
- `after/homepage-mobile-after.png` — 352 × 4012
- `after/homepage-desktop-after@2x.png` — 2880 × 6768
- `after/homepage-mobile-after@2x.png` — 704 × 8024

### Government Entity / Ministry — Before

- `before/ministry-desktop-before.png` — 1440 × 3360
- `before/ministry-mobile-before.png` — 352 × 4112
- `before/ministry-desktop-before@2x.png` — 2880 × 6720
- `before/ministry-mobile-before@2x.png` — 704 × 8224

### Government Entity / Ministry — After

- `after/ministry-desktop-after.png` — 1440 × 3360
- `after/ministry-mobile-after.png` — 352 × 4112
- `after/ministry-desktop-after@2x.png` — 2880 × 6720
- `after/ministry-mobile-after@2x.png` — 704 × 8224

### Government Service Detail — Before

- `before/service-desktop-before.png` — 1440 × 3384
- `before/service-mobile-before.png` — 352 × 4408
- `before/service-desktop-before@2x.png` — 2880 × 6768
- `before/service-mobile-before@2x.png` — 704 × 8816

### Government Service Detail — After

- `after/service-desktop-after.png` — 1440 × 3384
- `after/service-mobile-after.png` — 352 × 4408
- `after/service-desktop-after@2x.png` — 2880 × 6768
- `after/service-mobile-after@2x.png` — 704 × 8816

Phase 4.2 stops after Ministry and Service Detail and awaits explicit approval. No next phase or production implementation has started.

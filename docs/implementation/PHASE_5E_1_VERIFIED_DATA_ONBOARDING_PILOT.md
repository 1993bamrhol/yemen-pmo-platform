# Phase 5E.1 — Verified Data Onboarding Pilot

Status: **READY TO COMMIT**

Date: 2026-09-07

Starting checkpoint: `2c7d0fb74aa6c8cfbf083e6c807c9eea3ba8f15f` on `main`

## 1. Outcome

The pilot proved one complete, governed data path in the local V1–V9 runtime:

```text
Official Source
  → authenticated Admin API record
  → reviewable provenance
  → explicit publication
  → explicit verification
  → verified-only Public APIs
  → Government Entity and Service Detail UI
```

Exactly one Government Service record was created. No Government Entity was created or modified, no Flyway seed was added, and no Frontend, Backend, Database schema, feature flag, or Figma source file was changed.

The service remained hidden while it was `DRAFT + UNVERIFIED` and again after it became `PUBLISHED + UNVERIFIED`. It became publicly visible only after the separate verification action changed it to `PUBLISHED + VERIFIED` with an official HTTPS source reference.

Homepage Quick Services was not activated.

## 2. Official source decision

### 2.1 Accepted evidence

The pilot used two directly related official pages:

- [Prime Minister's Office announcement](https://pmo-ye.net/post/12593): announces the launch of an electronic transaction-tracking platform by the Prime Minister's Office and links to the platform.
- [Official transaction platform](https://pmo-ofcye.org/): identifies itself as the electronic window of the Prime Minister's Office and presents the service as `الاستعلام عن المعاملات`.

The service provenance stored in the catalog is the PMO announcement URL:

```text
sourceType: OFFICIAL_SOURCE_REFERENCE
sourceReference: https://pmo-ye.net/post/12593
```

The service channel uses the HTTPS platform URL supplied by that announcement:

```text
https://pmo-ofcye.org/
```

### 2.2 Deliberately excluded claims

Only facts directly required by the catalog record were onboarded. The pilot did not ingest or infer:

- eligibility;
- requirements;
- procedural steps;
- fees;
- processing time;
- telephone or other contact details;
- usage or performance statistics;
- an English service name;
- any transactional workflow owned by this portal.

The platform currently displays contact and statistical values, but those values were unnecessary to prove the service catalog pipeline and were not copied into the record.

## 3. Existing Government Entity assessment

The existing Entity record was reused without mutation:

| Field | Runtime value |
|---|---|
| ID | `00000000-0000-0000-0000-000000000001` |
| Arabic name | `رئاسة مجلس الوزراء اليمني` |
| Type | `PRIME_MINISTERS_OFFICE` |
| Status | `ACTIVE` |
| Canonical path | `/prime-ministers-office` |
| Official source reference | not populated |

The official service announcement supports the ownership relationship for this pilot, but it does not by itself verify every field in the existing Entity profile, especially the existing descriptive copy. The Entity record therefore was not retroactively marked or rewritten. Its missing profile provenance remains a separate content-governance item and does not change the Government Services public-visibility contract.

## 4. Service record

Exactly one local runtime record was created through `POST /api/v1/admin/services`:

| Field | Value |
|---|---|
| ID | `b7a43490-38d9-4120-ad2e-21612fdb1754` |
| Slug | `transaction-status-inquiry` |
| Canonical path | `/services/transaction-status-inquiry` |
| Official Arabic name | `الاستعلام عن المعاملات` |
| English name | absent |
| Summary | `تتبع حالة معاملتك الرسمية بسهولة وأمان` |
| Owning Entity | `00000000-0000-0000-0000-000000000001` — `رئاسة مجلس الوزراء اليمني` |
| Delivery channel | `ONLINE` |
| Channel URL | `https://pmo-ofcye.org/` |
| Eligibility / requirements / steps | empty |
| Fees / processing time / description | absent |
| Lifecycle | `PUBLISHED` |
| Verification | `VERIFIED` |
| Provenance type | `OFFICIAL_SOURCE_REFERENCE` |
| Provenance reference | `https://pmo-ye.net/post/12593` |
| First published | `2026-09-07T16:02:17.367260Z` |
| Verified | `2026-09-07T16:02:30.093885Z` |
| Verification actor | user ID `1`, username `admin` |

The database contained zero Government Service records before the pilot and exactly one afterward. No second, hidden, draft, rejected, or fixture service was created.

## 5. Administrative state transitions and auditability

All mutations used the existing authenticated Admin API and its existing entity-scoped permissions. No direct SQL mutation was used.

| Step | Endpoint | Result | Correlation ID | Audit event |
|---|---|---|---|---|
| Create | `POST /api/v1/admin/services` | `DRAFT + UNVERIFIED` | `phase5e1-official-service-onboarding` | `SERVICE_CREATED` / `SUCCESS` |
| Publish | `PUT /api/v1/admin/services/{id}/publication` | `PUBLISHED + UNVERIFIED` | `phase5e1-official-service-publication` | `SERVICE_PUBLISH` / `SUCCESS` |
| Verify | `PUT /api/v1/admin/services/{id}/verification` | `PUBLISHED + VERIFIED` | `phase5e1-official-service-verification` | `SERVICE_VERIFIED` / `SUCCESS` |

The verification audit event records actor `1`, the verification timestamp, `VERIFIED`, and `OFFICIAL_SOURCE_REFERENCE`. The authoritative source reference remains stored on the service and is exposed through the approved public detail projection.

## 6. Verified-only visibility evidence

### 6.1 Before publication

While the service was `DRAFT + UNVERIFIED`:

| Request | Result |
|---|---|
| `GET /api/v1/services` | HTTP `200`, zero items |
| `GET /api/v1/services/by-slug/transaction-status-inquiry` | unified HTTP `404` |
| `GET /api/v1/entities/{entityId}/services` | HTTP `200`, zero items |

### 6.2 Published but not verified

Immediately after `PUBLISH`, the Admin API reported `PUBLISHED + UNVERIFIED`. Public behavior remained unchanged:

| Request | Result |
|---|---|
| `GET /api/v1/services` | HTTP `200`, zero items |
| `GET /api/v1/services/by-slug/transaction-status-inquiry` | unified HTTP `404` |
| `GET /api/v1/entities/{entityId}/services` | HTTP `200`, zero items |

This proves that technical publication alone does not make a service official or public.

### 6.3 After verification

After the separate `VERIFIED` decision with official provenance:

| Request | Result |
|---|---|
| `GET /api/v1/services` | HTTP `200`, exactly one item |
| `GET /api/v1/services/{uuid}` | HTTP `200`, verified detail projection |
| `GET /api/v1/services/by-slug/transaction-status-inquiry` | HTTP `200`, verified detail projection |
| `GET /api/v1/entities/{entityId}/services` | HTTP `200`, exactly the same service |

The projections preserve the owning Entity ID and canonical path, the official Arabic service name, the ONLINE channel, official external URL, provenance type/reference, verification time, and publication time. Unsupported optional sections remain empty or `null`.

## 7. UI verification

The existing Frontend was run against the real local Backend API. No fixture or fallback data was enabled.

### Service Detail

`/services/transaction-status-inquiry` returned HTTP `200` and rendered:

- the real service title as the page `h1`;
- the owning Entity name without an invented relationship;
- the source-backed summary;
- the ONLINE delivery channel;
- the official external channel link;
- the provenance source and publish/verification dates.

Eligibility, requirements, steps, fees, processing time, English name, and description were absent from the API and therefore produced no empty headings or fabricated content.

The external action is rendered as an explicitly external link with `target="_blank"` and `rel="noopener noreferrer"`. The rendered accessibility tree exposes the external-window behavior in its accessible name.

### Entity Profile

`/prime-ministers-office` returned HTTP `200`. Its Services section rendered one `ServiceCard` linking to the API-supplied canonical service path `/services/transaction-status-inquiry`.

The browser accessibility tree confirmed the existing skip link, semantic `h1`/`h2` hierarchy, list structure, and link semantics. No new browser or server error was emitted. The pre-existing Next.js development warning about `scroll-behavior: smooth` remains unrelated to this data pilot.

## 8. Backend health and unified-content isolation

Backend health remained `UP`.

The protected read-only compatibility status after onboarding reported:

| Content type | configuredForUnified | shadowReady | effectiveSource | automatic fallbacks |
|---|---:|---:|---|---:|
| NEWS | `true` | `true` | `UNIFIED` | 0 |
| ANNOUNCEMENT | `true` | `true` | `UNIFIED` | 0 |
| DECISION | `false` | `true` | `LEGACY` | 0 |
| DOCUMENT | `false` | `true` | `LEGACY` | 0 |

`comparisonError` remained `null`. No feature flag or graduation state changed.

## 9. Files and runtime state

The only intended repository change in Phase 5E.1 is this report:

```text
docs/implementation/PHASE_5E_1_VERIFIED_DATA_ONBOARDING_PILOT.md
```

The local database now contains the single governed service record documented above. That record is intentionally not a Flyway seed and will not be transported by committing this report. Onboarding into another environment must repeat the authorized Admin API workflow against that environment using the same reviewed official evidence; it must not be converted into a production-like migration fixture.

Historical `backend/**/target/**`, review/design artifacts, `.design-system-state-yemen-gov-foundations-v1.json`, and `frontend/tsconfig.tsbuildinfo` remain outside this phase.

## 10. Remaining conditions and risks

1. The existing Entity profile still lacks an `officialSourceReference`; its descriptive prose should receive its own evidence review rather than inheriting the service source.
2. The official service platform is an external dependency. The portal must continue to treat its URL as data and retain safe external-link behavior.
3. Only the service name, short source-backed summary, ownership, and official online channel are currently supported. Missing requirements, fees, steps, and processing time are correct omissions, not data defects.
4. This pilot validates local runtime onboarding. Environment-specific publication remains an authorized operational action and is not delivered by Git.
5. Homepage Quick Services is still not activated by this phase. The catalog now has one eligible record, so a later readiness review may consider a one-card activation slice without inventing additional cards.
6. Latest Updates, Unified Search, and Open Data readiness is unchanged.

## 11. Final recommendation

**READY TO COMMIT** — commit this evidence report only.

The data-governance pipeline is proven end-to-end with one real, source-backed PMO service. The separation between publication and verification worked, unverified visibility was denied, the public Entity and Service APIs returned only the verified record, and both existing Frontend surfaces rendered the governed data without fake fallbacks. No new feature should be activated as part of this checkpoint.

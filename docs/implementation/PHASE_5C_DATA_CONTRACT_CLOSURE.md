# Phase 5C — Data Contract Closure

> **الحالة:** Proposed contract package — owner review required
>
> **التاريخ:** 2026-08-29
>
> **نقطة البداية:** `main` عند `d8973882902cefb87203d3e4bbf7a672c06713d7`
>
> **النطاق:** تدقيق وصياغة عقود فقط؛ لا production code أو database أو migrations أو Figma أو feature flags
>
> **التوصية:** **GO WITH CONDITIONS** لتنفيذ backend contract slices الصغيرة المحددة في القسم 15؛ تبقى صفحات Ministry وService Detail وUnified Search في `HOLD` حتى تنفيذ عقودها ووجود بيانات موثقة، وليس لمجرد اعتماد هذه الوثيقة.

## 1. Executive decision

الوضع الحالي ليس نقص endpoint واحدًا، بل ثلاث فجوات مستقلة يجب ألا تختلط:

1. **Government Directory موجود جزئيًا:** توجد هوية UUID ومسار type/slug وparent مباشر، لكن لا profile contract كامل، ولا paging/filter contract للدليل، ولا علاقات عامة قابلة للقراءة، ولا سجل Ministry موثّق.
2. **Government Service غير موجود:** لا module أو tables أو repository أو controller أو workflow أو records. صفحة `/services` الحالية وFigma و`site-data.ts` ليست مصادر بيانات.
3. **Technical availability لا تساوي publication approval:** واجهات المحتوى legacy-compatible تعمل، والتخزين الموحد موجود، لكن عناصر backfill/seed لا تصبح محتوى حكوميًا معتمدًا بسبب HTTP 200 أو `PUBLISHED` تقنيًا فقط.

عقد البحث يجب أن يكون projection مستقلة مشتقة من الجهات والخدمات والمحتوى المسموح نشره، لا مصدر حقيقة جديدًا ولا union داخل الواجهة. Open Data لا يملك حاليًا مصدرًا موثقًا، ولذلك يبقى `HOLD` كاملًا.

لا يرفع هذا التقرير أي `HOLD` بنفسه. ما يرفع الحظر هو: تنفيذ العقد، الاختبارات، سجل واحد موثّق على الأقل للدومين المعني، واعتماد المالك/التحرير للعرض العام.

## 2. Evidence and inspection boundary

تمت مراجعة:

- `frontend/src/lib/api.ts` وصفحات public الحالية.
- Controllers وDTOs وservices وrepositories في `organization`, `content`, `news`, `decisions`, `documents`, و`bootstrap`.
- Flyway `V5__platform_foundation.sql` و`V6__unified_content_foundation.sql`.
- Security rules وerror handling الحالي.
- وثائق Phase 2 وPhase 5 وIA المعتمدة.
- تحقق حي read-only في Docker المحلي بتاريخ 2026-08-29 بتوقيت الرياض.

نتيجة التحقق الحي المحدود:

| الفحص | النتيجة |
|---|---|
| `GET /health` | `200 OK` |
| `GET /api/v1/entity-types` | `200`؛ خمسة أنواع مسجلة |
| `GET /api/v1/entities` | `200`؛ سجل Active واحد لرئاسة مجلس الوزراء فقط |
| `GET /api/v1/content` | `404` لأن public unified-read controller غير مفعّل |
| `/api/services`, `/api/search`, `/api/open-data` | `401` من catch-all الأمني؛ لا توجد controllers لهذه المسارات، لذلك لا تعني الاستجابة وجود API |

لم تُرسل أي write request، ولم تُقرأ أو تُغيّر flags المحمية، ولم تُعدّل البيانات.

## 3. Common public API contract

تنطبق القواعد التالية على كل API جديدة أو موسعة في هذه الوثيقة.

### 3.1 Versioning and transport

- المسارات الجديدة تحت `/api/v1`.
- JSON UTF-8، وتواريخ/أوقات ISO 8601 مع offset أو UTC.
- أي تغيير يكسر shape مستخدمة حاليًا يجب أن يأخذ route/version جديدًا؛ لا تتغير DTOs الرقمية لـ`/api/news`, `/api/announcements`, `/api/decisions`, و`/api/documents` أثناء الـCanary.
- public GET لا يعرض إلا records مؤهلة للنشر. غير المنشور وغير الموثّق يعامل كغير موجود `404` في detail ولا يدخل list counts.
- `Cache-Control`, ETag، وسياسة CDN قرارات تشغيلية لاحقة؛ لا يعتمد correctness عليها.

### 3.2 Identifiers and canonical URLs

- UUID هو المعرف الدائم للجهات والخدمات والمحتوى والبيانات المفتوحة.
- numeric IDs تبقى فقط في compatibility facades الحالية إلى انتهاء خطة deprecation مستقلة.
- slug عام: lowercase ASCII بالنمط `[a-z0-9]+(?:-[a-z0-9]+)*`.
- backend هو مصدر `canonicalPath`؛ لا تعيد الواجهة اشتقاق المسار من الاسم العربي.
- تغيير slug يحتاج redirect history قبل السماح به في production. الموجود حاليًا يدعم redirects للمحتوى فقط، لا للجهات أو الخدمات.

### 3.3 Locale and Arabic

- V1 public locale الافتراضي والوحيد المضمون هو `ar`.
- كل resource يحمل `locale` صراحةً. لا يحدث fallback صامت إلى محتوى لغة أخرى.
- `locale` غير مدعوم يعيد `400 UNSUPPORTED_LOCALE`; ترجمة مطلوبة وغير موجودة تعيد `404`.
- الحقول العربية تقبل Unicode وتُحفظ دون transliteration. slug منفصل عن العنوان العربي.
- أرقام القرارات، URLs، وأسماء الملفات تبقى strings/URLs صريحة ليتم عرضها في `bdi`/`dir=auto` في الواجهة.

### 3.4 Pagination, filtering, and sorting

العقد القياسي للقوائم الجديدة/الموحدة هو zero-based ليتوافق مع `PageResponse` الحالي:

```ts
type PageResponse<T> = {
  items: T[];
  page: number;          // >= 0
  size: number;          // 1..100
  totalElements: number;
  totalPages: number;
};
```

- القيم الافتراضية: `page=0`, `size=20`، والحد الأقصى `100`.
- sorting allowlist فقط؛ قيمة غير مدعومة تعيد `400 INVALID_SORT`.
- filters غير المعروفة أو enum غير صالحة تعيد `400 INVALID_FILTER` بدل تجاهلها.
- legacy list endpoints الحالية تبقى arrays بلا paging للحفاظ على hash/ordering contracts.

### 3.5 Error contract

الوضع الحالي غير موحّد: بعض errors تستخدم Spring default body، وبعض identity errors تستخدم `{status,message,timestamp}`، و401 قد يكون بلا body. الهدف لكل `/api/v1` جديد هو `application/problem+json`:

```ts
type ApiProblem = {
  type: string;
  title: string;
  status: number;
  code: string;
  detail?: string;
  instance: string;
  traceId: string;
  violations?: Array<{ field: string; code: string }>;
};
```

السلوك القياسي:

- `400`: query/slug/locale/filter/sort غير صالح.
- `401`: admin endpoint بلا مصادقة.
- `403`: actor مصادق بلا scope/permission.
- `404`: public resource غير موجود أو غير مؤهل للنشر؛ لا يكشف وجود drafts.
- `409`: slug/version/relationship conflict في الإدارة.
- `422`: record لا يحقق publication criteria عند محاولة النشر.
- `429`: rate limit، خصوصًا Search.
- `503`: dependency/index غير جاهز، مع code ثابت؛ لا تتحول الاستجابة إلى fake/seed data.
- لا تظهر stack traces أو internal class names أو PII.

## 4. Current API inventory

| Domain | Current public endpoint | Current response | Persistence/source | Frontend binding | الحكم |
|---|---|---|---|---|---|
| Entity types | `GET /api/v1/entity-types` | array من `{id,code,name,pathSegment}` | `entity_types` | لا binding مستقل | **EXISTS** |
| Entities list | `GET /api/v1/entities` | `GovernmentEntityResponse[]`؛ Active فقط؛ alphabetical | `government_entities` | `getGovernmentEntities()` | **PARTIAL** |
| Entity detail | `GET /api/v1/entities/{uuid}` | entity + type + parent | DB | لا method | **PARTIAL** |
| Entity slug resolver | `GET /api/v1/entities/by-slug/{typeSegment}/{slug}` | entity + `canonicalPath` | DB | لا method | **PARTIAL** |
| Entity children | `GET /api/v1/entities/{uuid}/children` | Active child array | DB parent FK | لا method | **PARTIAL** |
| Entity relationships | لا public GET | — | table موجود | لا | **MISSING READ CONTRACT** |
| Government services | لا controller/DTO/model/table | — | لا source | لا | **MISSING** |
| NEWS | `/api/news[/numericId]` | legacy array/detail `{id,title,category,date,excerpt}` | legacy list أو unified projection حسب router | موجود | **TECHNICALLY AVAILABLE** |
| ANNOUNCEMENT | `/api/announcements[/numericId]` | نفس NEWS shape | legacy/unified compatibility | موجود | **TECHNICALLY AVAILABLE** |
| DECISION | `/api/decisions[/numericId]` | `{id,title,category,date,description}` | legacy/unified compatibility | موجود | **TECHNICALLY AVAILABLE** |
| DOCUMENT | `/api/documents[/numericId]` | `{id,title,category,updatedAt,description}` | legacy/unified compatibility | موجود | **TECHNICALLY AVAILABLE، incomplete document data** |
| Unified content list | `GET /api/v1/content` | paged `PublicContentResponse` | unified tables | لا binding | **CONTROLLER EXISTS، live 404** |
| Unified content detail | `/api/v1/content/{uuid}` و`/by-slug/{type}/{slug}` | unified content | unified tables | لا | **CONDITIONAL/HOLD** |
| Entity content | `/api/v1/entities/{uuid}/content` | paged unified content | unified tables | لا | **CONDITIONAL/HOLD** |
| Portal home | `GET /api/portal/home` | mixed composition | hardcoded presentation + content queries | method موجود لكن Homepage الجديدة لا تعتمد عليه | **DO NOT USE AS PRODUCTION CONTRACT** |
| Search | لا controller | — | لا index/query service | لا | **MISSING** |
| Open Data | لا controller/model/table | — | لا مصدر موثق | لا | **MISSING/HOLD** |

### 4.1 Current canary boundary

- NEWS: graduated for local Docker reads only، وeffective source موثق `UNIFIED`.
- ANNOUNCEMENT: technical exit gates passed and current local routing is `UNIFIED`، لكن repository records ما زالت تقول إن graduation يحتاج owner approval صريحًا.
- DECISION وDOCUMENT: `LEGACY`.
- لا production approval ولا write cutover لأي نوع.

أي عقد V1 جديد يجب أن يكون additive وألا يغيّر numeric IDs أو order أو payload hashes للواجهات الأربع الحالية. Editorial verification للواجهة الوطنية لا يجوز أن يعيد كتابة أو تصفية compatibility responses أثناء الـCanary.

## 5. Gap matrix

| Domain | Gap | Severity | ما يفك الحظر |
|---|---|---:|---|
| Entities | list بلا paging/filter؛ profile فقير؛ لا public relationships/contacts/slug redirects؛ سجل واحد فقط | P0 لوزارة | profile/read contracts + verified Ministry record + slug policy |
| Services | domain كامل غائب | P0 | schema + service module + read/admin workflow + verified record |
| Content | technical `PUBLISHED` لا يثبت اعتماد seed/backfill؛ attachments/type details غير معروضة | P0 للمستجدات/وزارة | editorial verification gate + typed metadata/attachments |
| Unified content | public controller خلف global flag ويعيد 404 | P0 لصفحة الجهة | rollout مستقل وآمن لا يغيّر compatibility flags |
| Legacy content | arrays غير paged، numeric-only، نصوص dates | P1 | إبقاؤها facades؛ لا توسعها؛ migrate consumers إلى V1 تدريجيًا |
| Search | لا endpoint ولا projection ولا ranking | P0 للبحث | approved result sources + projection + query contract + rate limit |
| Open Data | لا مصدر أو governance أو license/catalog | P0 للدومين، غير لازم للصفحات الأخرى | source owner + dataset governance + approved records |
| Errors | أشكال متعددة و401 فارغ | P1 | common Problem Details handler للـV1 |
| Frontend client | لا DTOs/methods للـV1 content/entity detail/services/search/datasets | P1 | typed adapters بعد backend contract tests |
| Data governance | لا حقل يميز backfill المتاح تقنيًا عن verified public content | P0 | explicit verification metadata and publication eligibility policy |

## 6. Government Entities contract

### 6.1 Existing contract to preserve

- يبقى `GET /api/v1/entities` بالشكل array الحالي حتى ترحيل Homepage المنضبط؛ لا يُحوّل فجأة إلى `PageResponse`.
- يبقى `GET /api/v1/entities/by-slug/{typeSegment}/{slug}` هو resolver العام الأساسي.
- UUID هو identity؛ `(typeSegment, slug)` هو public locator؛ `canonicalPath` هو الحقيقة للواجهة.
- public endpoints تعرض `ACTIVE` فقط.

### 6.2 Target directory endpoint — additive

`GET /api/v1/entity-directory`

Request query:

| field | required | contract |
|---|---:|---|
| `type` | no | `EntityType.code` allowlist |
| `parentId` | no | UUID |
| `locale` | no | default `ar` |
| `page`, `size` | no | common paging |
| `sort` | no | `officialName,asc` default؛ allowlist `officialName,asc`, `updatedAt,desc` |

Response: `PageResponse<EntitySummary>`.

```ts
type EntitySummary = {
  id: string;                    // UUID, required
  locale: "ar";                 // required in V1
  type: { code: string; name: string; pathSegment: string }; // required
  officialName: string;          // required
  shortName?: string;
  slug: string;                  // required
  canonicalPath: string;         // required
  summary?: string;
};
```

لا يوجد free-text `q` في MVP؛ البحث الموحد هو المسؤول عن ذلك لاحقًا.

### 6.3 Target entity profile

Resolver: `GET /api/v1/entities/by-slug/{typeSegment}/{slug}?locale=ar`.

الـresponse المستهدف additive فوق الموجود:

```ts
type EntityProfile = EntitySummary & {
  description?: string;
  mandate?: string;
  websiteUrl?: string;
  parent?: EntityReference;
  contacts?: Array<{
    type: "PHONE" | "EMAIL" | "ADDRESS" | "OFFICE_HOURS" | "WEBSITE";
    label?: string;
    value: string;
    url?: string;
  }>;
  relatedLinks?: Array<{ label: string; url: string }>;
  updatedAt: string;             // required provenance timestamp
};
```

`contacts`, `mandate`, و`relatedLinks` optional ولا تعرض إذا لم تكن موثقة. لا logo fallback حكومي ولا leadership data في MVP؛ إضافتهما يحتاج MediaAsset/Person governance مستقلًا.

### 6.4 Relationships and dependent collections

- `GET /api/v1/entities/{entityId}/children?page=&size=&sort=`: target paged form بعد route/version coordination؛ الحالي array يبقى مؤقتًا.
- `GET /api/v1/entities/{entityId}/relationships?direction=INBOUND|OUTBOUND&type=...&activeOn=YYYY-MM-DD&page=&size=`: public، ويعيد entity references فقط لعلاقات سارية بين جهات Active.
- `GET /api/v1/entities/{entityId}/services`: Service Catalog contract في القسم 7.
- `GET /api/v1/entities/{entityId}/content`: Unified Content contract في القسم 8، verified public records فقط.

### 6.5 Missing storage/behavior

- أعمدة/profile records لـ`mandate`, contacts, related links، و`updatedAt` في public DTO.
- `entity_slug_redirects` قبل السماح بتغيير slug.
- public query للعلاقات الموجودة في `entity_relationships`.
- Ministry record موثّق؛ لا تُستخدم أسماء Figma كseed.
- paging query/indexes عند توسيع الدليل.

## 7. Government Services contract

لا يوجد contract حالي. العقد التالي هو أقل نطاق يفتح Homepage quick services وService Detail دون اختراع بيانات.

### 7.1 Catalog endpoint

`GET /api/v1/services`

Request query:

| field | required | contract |
|---|---:|---|
| `entityId` | no | UUID؛ owner/provider relation |
| `category` | no | approved service-category slug |
| `audience` | no | approved taxonomy value |
| `channel` | no | `ONLINE`, `IN_PERSON`, `PHONE` |
| `featured` | no | boolean؛ لا يعمل إلا بقرار homepage placement موثق |
| `locale` | no | default `ar` |
| `page`, `size` | no | common paging |
| `sort` | no | `title,asc`, `publishedAt,desc`, أو `featuredRank,asc` عند `featured=true` |

Response: `PageResponse<ServiceSummary>`.

```ts
type ServiceSummary = {
  id: string;                    // UUID, required
  locale: "ar";                 // required
  slug: string;                  // required
  canonicalPath: string;         // required
  title: string;                 // required
  summary: string;               // required for publication
  ownerEntity: EntityReference;  // required
  categories: TaxonomyReference[]; // may be empty in storage, at least one required for featured
  audiences: string[];           // may be empty unless policy requires
  channels: Array<"ONLINE" | "IN_PERSON" | "PHONE">; // required, non-empty
  startAvailable: boolean;       // derived; never invents a URL
  updatedAt: string;             // required
};
```

Homepage quick services uses only `featured=true&size=<approved limit>` and displays empty/unavailable if no verified featured records. لا fallback من Figma أو `/services` static copy.

### 7.2 Service detail endpoint

- UUID: `GET /api/v1/services/{serviceId}?locale=ar`.
- public canonical resolver: `GET /api/v1/services/by-slug/{slug}?locale=ar`.

Response:

```ts
type ServiceDetail = ServiceSummary & {
  description: string;           // required
  entityRoles: Array<{
    role: "OWNER" | "PROVIDER" | "REGULATOR" | "SUPPORT";
    entity: EntityReference;
  }>;
  eligibility?: StructuredTextItem[];
  requirements?: StructuredTextItem[];
  steps: Array<{ order: number; title: string; description?: string }>;
  fees?: {
    type: "FREE" | "FIXED" | "VARIABLE" | "NOT_PUBLISHED";
    amount?: string;
    currency?: string;
    note?: string;
  };
  processingTime?: { value?: number; unit?: "MINUTE" | "HOUR" | "BUSINESS_DAY"; note?: string };
  channels: Array<{
    type: "ONLINE" | "IN_PERSON" | "PHONE";
    label: string;
    startUrl?: string;
    instructions?: string;
  }>;
  supportContact?: { label: string; url?: string; value?: string };
  sourceReference: string;        // required public provenance
  lastReviewedAt: string;         // required
  publishedAt: string;            // required
};
```

`StructuredTextItem` هو `{order:number,title:string,description?:string}`. `steps` يجب أن تكون non-empty للنشر إذا كانت الخدمة إجرائية. الحقول الاختيارية لا تتحول إلى عبارات افتراضية. Primary CTA يظهر فقط عند وجود `ONLINE.startUrl` صالح وموثّق؛ otherwise تعرض القنوات الفعلية بلا fake start action.

### 7.3 Status and publication behavior

- internal workflow: `DRAFT -> IN_REVIEW -> APPROVED -> PUBLISHED -> ARCHIVED` مع separation of duties.
- public list/detail يعرضان `PUBLISHED + VERIFIED` فقط، مع owner Entity `ACTIVE`.
- detail لغير المؤهل: `404`.
- invalid filter/slug: `400`؛ duplicate slug: admin `409`; missing required publication fields: admin `422`.

### 7.4 Minimum schema

- `government_services`.
- `service_revisions` أو immutable versioned detail equivalent.
- `service_entity_roles`؛ exactly one `OWNER`, many providers/support.
- `service_categories` و`service_category_assignments`.
- `service_channels`.
- structured requirements/eligibility/steps، إما جداول ordered أو JSONB مع schema validation؛ التوصية جداول للخطوات والقنوات، وJSONB مضبوط للعناصر النصية البسيطة فقط.
- `service_slug_redirects`.
- publication verification/provenance fields.
- optional homepage placement fields/table لا تُملأ إلا بقرار تحريري.

## 8. News, Announcements, Decisions, Documents, and Unified Content

### 8.1 Compatibility contract — freeze

الواجهات التالية لا تتغير في Phase 5C implementation:

- `GET /api/news[/numericId]`
- `GET /api/announcements[/numericId]`
- `GET /api/decisions[/numericId]`
- `GET /api/documents[/numericId]`
- projection داخل `/api/portal/home` إلى أن تقرر خطة deprecation مستقلة.

لا paging/filter/sort جديد عليها لأنه سيغيّر hashes/order. أي تحسين عام يذهب إلى Unified Content V1.

### 8.2 Unified list contract

Existing target: `GET /api/v1/content`.

Request:

| field | required | contract |
|---|---:|---|
| `type` | no | `NEWS`, `ANNOUNCEMENT`, `DECISION`, `DOCUMENT` |
| `entityId` | no | primary entity UUID؛ related-entity filter مؤجل إلى query منفصل/explicit flag |
| `category` | no | active category slug |
| `dateFrom`, `dateTo` | no | ISO local dates؛ inclusive calendar range |
| `locale` | no | target addition؛ default `ar` |
| `page`, `size` | no | existing common paging |
| `sort` | no | target allowlist؛ default `publishedAt,desc`; `title,asc` optional |

Response: existing `PageResponse<PublicContentResponse>` مع additions التالية في نسخة additive/compatible:

```ts
type PublicContent = {
  id: string;                    // UUID
  contentType: "NEWS" | "ANNOUNCEMENT" | "DECISION" | "DOCUMENT";
  slug: string;
  locale: "ar";
  canonicalPath: string;
  title: string;
  summary?: string;
  body: string;                  // detail required; list MAY use summary projection later via explicit endpoint
  byline?: string;
  publishedAt: string;
  updatedAt: string;
  primaryEntity: EntityReference;
  relatedEntities: Array<{ role: "CO_PUBLISHER" | "SUBJECT" | "RELATED"; entity: EntityReference }>;
  categories: TaxonomyReference[];
  attachments: PublicAttachment[];
  attributes?: DecisionAttributes | DocumentAttributes | AnnouncementAttributes;
  sourceVerification: { status: "VERIFIED"; verifiedAt: string };
};
```

Type-specific fields:

- Decision: `decisionNumber?`, `issuedOn?`, `effectiveOn?`, `legalStatus?` من `decision_details`.
- Document: `documentNumber?`, `documentDate?`, `primaryAttachmentId?` من `document_details`.
- Announcement: `expiresAt?` فقط إذا تقرر له storage؛ لا يضاف كافتراض.
- News: لا attributes لازمة للـMVP.
- `PublicAttachment`: `id`, `label`, `mimeType`, `sizeBytes?`, `downloadUrl`, `sha256?`; لا يعرض إلا `public_visible=true` وملف اجتاز security scan وفق سياسة الملفات.

### 8.3 Detail contracts

- `GET /api/v1/content/{uuid}`.
- `GET /api/v1/content/by-slug/{type}/{slug}?locale=ar`.
- unknown/invalid type أو slug: `400`; missing/unpublished/unverified: `404`.
- slug resolution في الموجود hardcoded إلى locale `ar`; target يجعل locale صريحًا.

### 8.4 Editorial verification gate

الموجود حاليًا يملك workflow status وpublished revision، لكنه لا يميز backfill المتاح تقنيًا عن record تحقق منه ناشر مخوّل كمعلومة رسمية. يضاف مستقبلًا، بصورة additive، metadata صريحة لا تعتمد على `display_metadata` النصي:

```ts
type SourceVerification = {
  status: "UNVERIFIED" | "VERIFIED" | "REJECTED";
  sourceReference?: string;
  verifiedBy?: number;
  verifiedAt?: string;
  note?: string;
};
```

Publication eligibility للواجهة الوطنية يتطلب معًا:

1. `ContentStatus=PUBLISHED` وpublished revision موجودة وغير archived.
2. `sourceVerification.status=VERIFIED`.
3. primary entity Active.
4. title/body/slug/locale صالحة.
5. metadata الإلزامية حسب النوع مكتملة.
6. attachments العامة، إن وجدت، safe وموسومة accessible label.
7. transition موثقة من actor مخوّل، مع separation of duties.

الـ12 backfill rows الحالية لا تُرقّى تلقائيًا إلى `VERIFIED`. Compatibility facades تستمر في projection الحالية ولا تتأثر بهذا gate إلى أن تنتهي خطة الـCanary/deprecation.

### 8.5 Sorting and ordering

- Unified default: `lastPublishedAt DESC, id DESC` كما في التنفيذ الحالي.
- لا يعتمد public ordering على strings العربية للتاريخ.
- category/order في legacy facade يبقى frozen.
- homepage updates تستخدم unified verified feed فقط بعد تفعيل public contract بقرار مستقل.

## 9. Unified Search contract

لا تنفيذ للبحث في هذه المرحلة.

### 9.1 Public endpoint

`GET /api/v1/search`

Request:

| field | required | contract |
|---|---:|---|
| `q` | yes | trimmed Unicode؛ 2..200 chars |
| `types` | no | comma-separated allowlist: `ENTITY,SERVICE,NEWS,ANNOUNCEMENT,DECISION,DOCUMENT,DATASET` |
| `entityId` | no | UUID facet |
| `category` | no | approved taxonomy slug |
| `dateFrom`, `dateTo` | no | content/dataset date range |
| `locale` | no | default `ar` |
| `page`, `size` | no | common paging؛ max 50 للبحث |
| `sort` | no | `RELEVANCE` default أو `PUBLISHED_AT_DESC` |

Response:

```ts
type SearchResponse = PageResponse<SearchResult> & {
  query: string;
  facets: {
    types: Array<{ value: string; count: number }>;
    entities: Array<{ id: string; label: string; count: number }>;
    categories: Array<{ slug: string; label: string; count: number }>;
  };
};

type SearchResult = {
  resourceType: "ENTITY" | "SERVICE" | "NEWS" | "ANNOUNCEMENT" | "DECISION" | "DOCUMENT" | "DATASET";
  resourceId: string;
  title: string;
  summary?: string;
  canonicalPath: string;
  locale: "ar";
  primaryEntity?: EntityReference;
  categories?: TaxonomyReference[];
  publishedAt?: string;
  highlights?: Array<{ field: "title" | "summary"; fragments: string[] }>;
};
```

### 9.2 Source of each result type

| Result type | Source of truth | Inclusion gate |
|---|---|---|
| ENTITY | Government Directory | Active + public profile |
| SERVICE | Service Catalog | Published + Verified + Active owner |
| NEWS/ANNOUNCEMENT/DECISION/DOCUMENT | Unified Content | Published + Verified + non-archived |
| DATASET | Open Data catalog | لا يدخل index حتى اعتماد الدومين والسجل والترخيص |

### 9.3 Index decision

Search يعتمد **index/projection مستقلة** (`SearchDocument`) مشتقة من المصادر أعلاه؛ unified content هو أحد المصادر وليس index لكل الأنواع. Source records تبقى الحقيقة.

- البداية المقترحة: PostgreSQL full-text + `pg_trgm`/Arabic normalization ضمن query service أو `search_documents` projection.
- لا local frontend search ولا scan لأربعة endpoints.
- reindex/reconciliation job يثبت أن كل record eligible له document واحدة وأن non-eligible records محذوفة.
- ranking contract versioned ومقاس؛ لا يعاد ترتيب النتائج يدويًا لأغراض مطابقة Figma.
- highlights sanitized، query rate-limited، ووقت التنفيذ/timeout مراقب.

## 10. Open Data contract

### 10.1 Current decision

لا يوجد مصدر بيانات، catalog، licensing policy، owner، provenance، distributions، أو API. لذلك Open Data يبقى **HOLD / DO NOT IMPLEMENT**. لا ينشأ dataset واحد من Figma أو links عامة غير مثبتة.

### 10.2 Provisional contract — not approved for implementation

عند اعتماد مصدر ومالك governance فقط:

- `GET /api/v1/open-data/datasets?ownerEntityId=&category=&format=&updatedFrom=&page=&size=&sort=updatedAt,desc`
- `GET /api/v1/open-data/datasets/{uuid}`
- `GET /api/v1/open-data/datasets/by-slug/{slug}?locale=ar`

```ts
type DatasetDetail = {
  id: string;
  locale: "ar";
  slug: string;
  canonicalPath: string;
  title: string;
  description: string;
  ownerEntity: EntityReference;
  license: { code: string; label: string; url: string };
  coverage?: { from?: string; to?: string; geography?: string };
  updateFrequency?: string;
  lastUpdatedAt: string;
  sourceReference: string;
  distributions: Array<{
    id: string;
    format: string;
    accessUrl: string;
    downloadUrl?: string;
    sizeBytes?: number;
    sha256?: string;
  }>;
};
```

Publication requires owner Entity Active، license واضح، source/provenance، last-updated، distribution واحدة قابلة للتحقق على الأقل، وملفات آمنة. العقد يبقى provisional حتى اعتماد تلك السياسة.

## 11. Domain dependencies

| Consumer | Entity | Service | Unified content | Search projection | Open Data |
|---|---:|---:|---:|---:|---:|
| Homepage entities | Required | — | — | — | — |
| Homepage quick services | Required as owner ref | Required | — | — | — |
| Homepage updates | Required as primary entity | — | Required + verified | — | — |
| Ministry profile | Required | Optional section but required for full approved screen | Required for updates/resources | — | — |
| Service Detail | Required | Required | Optional related content | — | — |
| Unified Search | Required | Required before SERVICE results | Required | Required | Optional/HOLD |
| Open Data section | Required as owner | — | Optional related reports | Optional after catalog | Required |

لا يجوز أن يؤدي فشل collection تابعة إلى إسقاط profile الأساسية. Page composition تطلب entity، services، وcontent بصورة مستقلة وتعرض partial/error states المعتمدة.

## 12. Schema and migration impact — future only

لم تُنشأ migration في Phase 5C. التأثير المتوقع:

| Area | Future additive change | Breaking risk |
|---|---|---|
| Entities | profile/contact/link fields or normalized tables؛ slug redirects؛ relationship read indexes | Low إذا بقي DTO الحالي additive |
| Services | جداول domain كاملة المذكورة في 7.4 | New domain؛ no current contract to break |
| Content governance | explicit source verification/provenance columns/table | يجب عزلها عن compatibility projections |
| Content reads | mappings لـattachments, decision/document details, related entities | Additive V1 response؛ client typing update |
| Search | `search_documents` projection + FTS/trigram indexes + reconciliation state | لا source writes؛ rebuildable projection |
| Open Data | dataset/distribution/license tables | HOLD حتى governance |

كل migration مستقبلية يجب أن تكون additive، قابلة للrollback التشغيلي دون حذف legacy data، مع Flyway integration tests وschema validation. لا write cutover للمحتوى legacy ضمن هذه الخطة.

## 13. Backend, frontend, and Figma impact

### 13.1 Backend modules

- `organization`: يظل owner لـGovernment Directory؛ يضاف profile/relationship read contract فقط.
- module جديد bounded باسم `services` أو `service-catalog`؛ القرار الاسمي يحتاج اعتماد، والتوصية `services` للوضوح مع API.
- `content`: يظل source of truth للمحتوى الموحد؛ يضاف verification/typed public projection دون تغيير compatibility façade.
- `bootstrap`: يبقى composition/wiring وcompatibility؛ لا يوضع Service business logic أو Search ranking داخله.
- Search: query/projection component مستقل داخل modular monolith أولًا؛ لا microservice قبل قياس الحاجة.
- Open Data: لا module قبل اعتماد المصدر والمالك.

### 13.2 Frontend API client

بعد backend contract tests فقط:

- فصل transport DTOs عن card/view models.
- إضافة typed methods لـentity detail/directory، entity content/services، service list/detail، unified content، ثم search.
- توسيع `ApiError` ليحمل `code`, `detail`, `traceId` مع fallback آمن للlegacy errors.
- عدم ربط presentation components بالfetch؛ page/loader adapters فقط تطلب API.
- عدم استخدام `site-data.ts` أو Figma fixtures كproduction fallback.

### 13.3 Figma screens

لا يلزم تعديل Figma لإغلاق العقود. آثار runtime:

- الأقسام الاختيارية تختفي أو تعرض ContentState؛ لا filler cards.
- Service CTA يظهر فقط مع verified start channel.
- Ministry sections تعتمد collections مستقلة وتتحمل partial failure.
- long Arabic/missing image/missing metadata تبقى ضمن behavior المعتمد.
- Accordion في Mobile Service Detail هو presentation؛ API يعيد steps مرتبة ولا يفرض طيها.
- counts، fees، processing time، leadership، contacts، والشعارات لا تعرض إلا من fields موثقة.

## 14. Security and access considerations

- public list/detail queries تفرض eligibility في repository/service، لا في frontend.
- non-public resource يعيد 404 لمنع existence disclosure.
- admin service/content/entity writes تحتاج entity-scoped permissions، مع `Service Manager`/content permissions وseparation of duties.
- owner/provider relations لا تمنح authorization تلقائيًا؛ الصلاحية تأتي من RoleAssignment scope.
- URLs تقبل `https` فقط في production policy، مع منع `javascript:` وopen redirects.
- Search: query length/rate limit/timeouts، sanitized highlights، no draft/PII indexing.
- attachments/distributions: malware scan، MIME verification، checksum، accessible label، وpublic visibility.
- Error bodies لا تكشف PII أو internal details؛ `traceId` للمراقبة.
- CORS allowlist الحالية تبقى؛ لا wildcard بسبب endpoints العامة.

## 15. Minimum viable backend work

أقل عمل يفتح الصفحات المعتمدة دون بناء Search/Open Data كامل:

1. **Contract/error foundation:** Problem Details للـV1، validation، OpenAPI/contract tests، paging/sort allowlists.
2. **Editorial source gate:** source verification/provenance additive للمحتوى؛ public eligibility query؛ عدم لمس compatibility output/flags.
3. **Unified public projection closure:** locale، related entities، attachments، decision/document attributes؛ rollout approval مستقل للـV1 public reads.
4. **Entity profile closure:** profile fields الضرورية فقط، directory query، public relationships، slug redirects؛ ثم إدخال Ministry record موثّق عبر مسار إداري معتمد.
5. **Service Catalog MVP:** module + schema + public list/detail + entity relation + workflow/admin path + contract/integration/security tests.
6. **Governed data onboarding:** سجل خدمة واحد مكتمل وموثّق على الأقل، وhomepage featured placement معتمد؛ لا seed توضيحي.
7. **Frontend adapters فقط بعد ثبات backend:** typed clients/mappers؛ لا بدء الصفحات ضمن Phase 5C.

Search يأتي بعد استقرار المصادر 2–6. Open Data ليس جزءًا من minimum work لرفع حظر Ministry/Service Detail.

## 16. Content governance considerations

يجب اعتماد مالك لكل record ومالك taxonomy، ومن يراجع ومن ينشر. الحد الأدنى:

- مصدر/مرجع قابل للمراجعة لكل entity/service/content item.
- timestamps وactor IDs للتدقيق.
- فصل creator عن approver/publisher حيث تنطبق السياسة.
- مراجعة دورية للخدمات (`lastReviewedAt`) مع expiry/escalation policy.
- عدم اعتبار backfill أو seed أو HTTP availability موافقة تحريرية.
- إيقاف/أرشفة record لا يحذف history أو يكسر URL؛ redirect/tombstone policy.
- لا تظهر service fees/مدة/متطلبات إذا كانت unknown؛ unknown ليست zero/free.
- Homepage featured content يحتاج قرار placement منفصلًا عن مجرد publication.

## 17. Implementation order and validation gates

| Slice | Scope | Validation gate |
|---|---|---|
| 5C-1 | Common V1 errors + contract tests | legacy DTO/hash tests unchanged؛ invalid input matrix pass |
| 5C-2 | Editorial verification + public content projection additions | drafts/unverified hidden؛ compatibility canaries unchanged؛ SOD tests pass |
| 5C-3 | Entity profile/directory/relationships | UUID/slug/canonical/404/paging tests؛ no unverified record exposed |
| 5C-4 | Service Catalog schema/domain/admin workflow | migration/schema tests؛ entity scope/security؛ state machine tests |
| 5C-5 | Service public list/detail + verified onboarding | filters/order/404/partial fields؛ one owner exactly؛ no fake data |
| 5C-6 | Frontend typed adapters | runtime schema/typing/error mapping؛ no page implementation |
| 5C-7 | Ministry and Service Detail owner decision | only after verified entity/service records and API evidence |
| 5C-8 | Search projection/API | reconciliation, relevance, Arabic, rate/latency/security gates |
| Future | Open Data | only after source/governance/license approval |

كل slice لها commit منفصل ومراجعة؛ لا تجمع schema للخدمات والبحث والبيانات المفتوحة في migration واحدة.

## 18. Readiness after closure

### 18.1 Immediate state after this report

| Target | Current result | السبب |
|---|---|---|
| Homepage | **READY WITH CONDITIONS** | تعمل production-safe، لكن services/search/open-data غائبة وupdates غير verified |
| Ministry | **HOLD (PARTIAL contract)** | لا profile كامل ولا Ministry record موثّق ولا entity services/content public-ready |
| Service Detail | **HOLD** | Government Service domain غائب |
| Unified Search | **HOLD** | endpoint/index/ranking غائبة ومصادر النتائج غير مكتملة |

### 18.2 Projected state after minimum backend work in section 15

| Target | Projected result | شرط الرفع |
|---|---|---|
| Homepage | **GO WITH CONDITIONS** | quick services وverified updates يمكن تفعيلهما؛ Search/Open Data يبقيان unavailable حتى slices مستقلة |
| Ministry | **GO WITH CONDITIONS** | entity profile + verified Ministry row + entity content/services؛ الأقسام بلا بيانات تختفي بوضوح |
| Service Detail | **GO WITH CONDITIONS** ثم GO لكل record مكتمل | service contract implemented + verified published record + channel/steps/provenance |
| Unified Search | **HOLD** | لا يرفع حتى تنفيذ 5C-8؛ بعده GO WITH CONDITIONS دون DATASET facet حتى Open Data |

## 19. Decisions requiring owner approval

1. اعتماد UUID كهوية دائمة، وtype/slug كـpublic locator، وضرورة slug redirects.
2. اعتماد `/api/v1/entity-directory` كread model paged مع إبقاء `/api/v1/entities` الحالي مؤقتًا.
3. اسم module الخدمات: التوصية `services`.
4. اعتماد Service workflow وpublication minimum fields، خصوصًا source reference وlast review.
5. اعتماد `SourceVerification` كبوابة منفصلة عن technical backfill availability.
6. اعتماد أن Search projection مستقلة مشتقة، وأن PostgreSQL هو التنفيذ الأولي.
7. تأكيد أن Open Data يبقى HOLD حتى مصدر/ترخيص/مالك موثق.
8. قرار إداري مستقل حول graduation لـANNOUNCEMENT، لأن Exit Review التقنية وحدها لا تحسمه في السجل الحالي.

## 20. Final recommendation

**GO WITH CONDITIONS** لتنفيذ 5C-1 إلى 5C-6 فقط، على دفعات صغيرة وبعد اعتماد القرارات في القسم 19.

- لا تبدأ Ministry قبل 5C-3 و5C-5 ووجود Ministry record موثّق.
- لا تبدأ Service Detail قبل Service Catalog public contract وسجل verified مكتمل.
- لا تبدأ Unified Search قبل استقرار entity/service/content eligibility ثم تنفيذ projection مستقلة.
- Open Data يبقى `HOLD` كاملًا.
- لا تغيّر compatibility flags أو legacy writes أو Canary routing ضمن Data Contract Closure.
- لا تعتبر بيانات Figma أو `site-data.ts` أو hardcoded Java lists أو backfill rows بيانات production معتمدة.

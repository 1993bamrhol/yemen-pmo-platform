# Phase 2 Plan — Unified Government Content

> **الحالة:** Implementation in progress — Slices 1–4 and 5A complete; no compatibility cutover  
> **التاريخ:** 2026-08-24  
> **يعتمد على:** `UNIFIED_GOVERNMENT_PORTAL_IA_V1.md`، `adr/0005-unified-government-content.md`، وPhase 1  
> **سجل التنفيذ:** راجع `PHASE_2_IMPLEMENTATION_RECORD.md`. نُفذ backfill؛ لم ينفذ compatibility cutover حتى الآن.

## 1. الهدف

تحويل الأخبار والإعلانات والقرارات والوثائق من مصادر منفصلة إلى مصدر حقيقة مركزي واحد مرتبط بجهة حكومية، مع revisions ونشر مضبوط وروابط مستقرة، مع إبقاء جميع APIs ومسارات الواجهة الحالية عاملة أثناء الانتقال.

تنتهي المرحلة عندما يستطيع محرر PMO إنشاء سجل واحد، تمريره في دورة النشر، ثم يظهر السجل نفسه في القائمة العامة وصفحة التفاصيل والصفحة الرئيسية ونتائج الفلترة دون duplication.

## 2. الجرد الحالي

### 2.1 مصادر المحتوى

| المصدر | الوضع الحالي | المشكلة |
|---|---|---|
| `admin_content` | 5 سجلات PostgreSQL: خبران وإعلان وقرار ووثيقة | metadata سطحية ولا يغذي العرض العام |
| `NewsService` | 3 أخبار ثابتة | deployment مطلوب لكل تغيير |
| `AnnouncementService` | 3 إعلانات ثابتة | لا صلة بالإدارة أو workflow |
| `DecisionService` | 3 قرارات ثابتة | لا رقم قرار أو تاريخ typed |
| `DocumentService` | 3 وثائق ثابتة | لا ملفات أو checksum أو storage metadata |
| `PortalHomeController` | محتوى وتركيبات وأرقام ثابتة | صفحة رئيسية كمصدر بيانات مستقل |
| `frontend/site-data.ts` | fallback محلي | مصدر رابع محتمل عند فشل API |

حالة `admin_content` الحالية: سجلان منشوران، سجل مسودة، سجل قيد المراجعة، وسجل مؤرشف. لا يجوز تحويل المسودة أو قيد المراجعة إلى محتوى عام أثناء backfill.

### 2.2 العقود التي يجب الحفاظ عليها

```text
GET /api/news
GET /api/news/{numericId}
GET /api/announcements
GET /api/announcements/{numericId}
GET /api/decisions
GET /api/decisions/{numericId}
GET /api/documents
GET /api/documents/{numericId}
GET /api/portal/home

GET/POST/PUT/DELETE /api/admin/content[/{numericId}]
GET /api/admin/content/summary
```

الـfrontend يعتمد حاليًا على numeric IDs وشكل response مختلف قليلًا لكل نوع. لذلك لن تُحوّل هذه العقود مباشرة إلى UUIDs.

## 3. حدود Phase 2

### داخل النطاق

- `ContentItem` مركزي للأنواع NEWS وANNOUNCEMENT وDECISION وDOCUMENT.
- ownership إلزامي عبر `primaryGovernmentEntityId`.
- immutable revisions وحفظ النسخة المنشورة بصورة مستقلة عن نسخة التحرير.
- workflow خاص بالمحتوى: Draft → Review → Approval → Publish → Archive.
- taxonomy أساسية ومرفقات metadata فقط.
- جهات إضافية مرتبطة بالمحتوى دون منحها ملكية تحرير تلقائية.
- slugs وredirect history.
- backfill للمحتوى الإداري والقوائم الثابتة الحالية.
- `/api/v1/content` وadmin APIs scoped.
- compatibility facades لكل endpoints القديمة.
- تحويل `/api/portal/home` إلى query projection للمحتوى المنشور.
- audit لكل mutation وانتقال حساس.

### خارج النطاق

- redesign أو Figma أو إعادة بناء navigation.
- محرر WYSIWYG جديد في الواجهة.
- تعميم workflow engine على الخدمات والبيانات والحالات المدنية.
- تخزين binaries داخل PostgreSQL.
- object-storage provisioning أو antivirus pipeline.
- multilingual authoring الكامل؛ النموذج فقط localization-ready.
- Unified Search index؛ Phase 2 يوفر read model صالحًا للفهرسة لاحقًا.
- إزالة الجداول أو endpoints القديمة.
- أنواع REPORT/POLICY/REGULATION/MEDIA/PAGE كواجهات تحرير كاملة؛ يحجز enum والتصميم قابلية إضافتها لاحقًا.

## 4. مبادئ النموذج

1. سجل واحد، placements متعددة.
2. الجهة الأساسية إلزامية ولا تستنتج من المستخدم أو المسار.
3. الروابط إلى جهات أخرى لا تمنح صلاحية تعديل.
4. النسخة المنشورة ثابتة حتى نشر revision لاحقة.
5. لا hard delete للمحتوى المنشور من APIs العادية؛ الحذف القديم يصبح Archive.
6. public APIs لا تعرض إلا `PUBLISHED` و`publishedRevisionId`.
7. numeric legacy ID مجرد alias؛ UUID هو الهوية الداخلية الجديدة.
8. لا merging بالعنوان وقت التشغيل؛ كل دمج legacy محدد في manifest قابل للمراجعة.

## 5. Domain Model

### 5.1 `ContentItem`

هو aggregate root ويحمل الهوية والملكية والحالة، وليس النص التحريري المتغير.

| الحقل | النوع المقترح | القاعدة |
|---|---|---|
| `id` | UUID | PK ثابت |
| `content_type` | varchar enum | NEWS, ANNOUNCEMENT, DECISION, DOCUMENT أولًا |
| `primary_entity_id` | UUID FK | إلزامي |
| `slug` | varchar(180) | lowercase Latin slug في V1 |
| `locale` | varchar(12) | `ar` افتراضيًا |
| `status` | constrained varchar | DRAFT, IN_REVIEW, APPROVED, PUBLISHED, ARCHIVED |
| `current_revision_id` | UUID FK nullable | أحدث revision تحريرية |
| `published_revision_id` | UUID FK nullable | النسخة العامة الثابتة |
| `first_published_at` | timestamptz nullable | لا يتغير بعد أول نشر |
| `last_published_at` | timestamptz nullable | يتغير مع كل نشر |
| `archived_at` | timestamptz nullable | عند الأرشفة |
| `created_at/updated_at` | timestamptz | timestamps كاملة |
| `created_by/updated_by` | bigint FK nullable | nullable فقط للاستيراد القديم |
| `version` | bigint | optimistic locking |

الـslug فريد على `(content_type, locale, slug)` لأن URL العام هو `/news/{slug}` وليس داخل namespace الجهة.

### 5.2 `ContentRevision`

كل تعديل محفوظ كنسخة immutable؛ لا update لنص revision منشأة.

| الحقل | الغرض |
|---|---|
| `id`, `content_item_id`, `revision_number` | الهوية والترتيب |
| `title`, `summary`, `body` | المحتوى العربي |
| `byline` | اسم العرض؛ لا يستخدم للصلاحيات |
| `change_note` | سبب التعديل |
| `created_at`, `created_by` | provenance |

`body` يخزن HTML محدودًا ومعقمًا وفق allowlist. يمنع scripts وinline event handlers وiframes غير المعتمدة. يمكن اشتقاق plain text لاحقًا للبحث.

### 5.3 subtype details

لا توضع الحقول القانونية المهمة في JSON عام.

- `DecisionDetails`: `content_item_id`, `decision_number`, `issued_on`, `effective_on`, `legal_status`.
- `DocumentDetails`: `content_item_id`, `document_number`, `document_date`, `primary_attachment_id`.

NEWS وANNOUNCEMENT يستخدمان core fields في هذه المرحلة. يسمح `display_metadata JSONB` فقط لبيانات عرض غير حرجة وغير مستخدمة في authorization أو workflow أو القيود القانونية.

### 5.4 `ContentEntityLink`

| الحقل | القيم |
|---|---|
| `content_item_id` | FK |
| `government_entity_id` | FK |
| `link_role` | CO_PUBLISHER, SUBJECT, RELATED |

unique على `(content_item_id, government_entity_id, link_role)`. الجهة الأساسية لا تكرر كرابط. `CO_PUBLISHER` placement دلالي فقط في Phase 2؛ الملكية التحريرية تبقى للجهة الأساسية.

### 5.5 Taxonomy

- `TaxonomyTerm`: UUID، taxonomy code، slug، Arabic label، parent اختياري، active.
- `ContentTaxonomyAssignment`: content + term.
- taxonomies الأولية: `CONTENT_CATEGORY` و`TOPIC`.

لا تُستخدم النصوص الحرة الحالية كهوية taxonomy. تحفظ قيم category القديمة في mapping manifest ثم تربط بالمصطلح المعتمد.

### 5.6 `Attachment`

metadata فقط:

- UUID، content item، revision اختياري.
- storage provider/key أو external URL.
- original filename، MIME type، size، SHA-256.
- Arabic accessible label، sort order، public flag.

لا binary columns. لا يصبح المرفق عامًا إلا إذا كان content منشورًا والمرفق معلّمًا public.

### 5.7 `ContentSlugRedirect`

يحفظ `content_type`, `locale`, `old_slug`, `content_item_id`, timestamps. يمنع إعادة استخدام slug قديم لكي لا يشير رابط حكومي تاريخي إلى سجل مختلف.

### 5.8 `LegacyContentMapping`

| الحقل | مثال |
|---|---|
| `source_system` | `ADMIN_CONTENT`, `STATIC_NEWS` |
| `source_type` | `news`, `decision` |
| `legacy_id` | `1` |
| `content_item_id` | UUID |

unique على `(source_system, source_type, legacy_id)`. يسمح بتداخل ID=1 بين news وdecision دون تصادم.

### 5.9 `ContentTransition`

سجل append-only:

- content item، from/to state، action.
- actor، timestamp، comment.
- revision التي تم اعتمادها أو نشرها.
- entity scope وcorrelation ID.

هذا workflow متخصص للمحتوى. لا نبني generic workflow engine في Phase 2؛ يمكن إسقاطه لاحقًا على bounded context العام في Phase 5.

## 6. State Machine

```text
DRAFT -> IN_REVIEW -> APPROVED -> PUBLISHED -> ARCHIVED
            |             |
            +--> DRAFT <--+

PUBLISHED --(new edit)--> PUBLISHED مع revision تحريرية جديدة غير عامة
PUBLISHED --(publish revision)--> PUBLISHED مع تبديل published_revision_id
ARCHIVED --(restore by platform policy)--> DRAFT
```

| action | من | إلى | permission |
|---|---|---|---|
| SUBMIT_REVIEW | DRAFT | IN_REVIEW | `content.write` داخل الجهة |
| REQUEST_CHANGES | IN_REVIEW/APPROVED | DRAFT | `content.review` داخل الجهة |
| APPROVE | IN_REVIEW | APPROVED | `content.approve` داخل الجهة |
| PUBLISH | APPROVED | PUBLISHED | `content.publish` داخل الجهة |
| ARCHIVE | PUBLISHED | ARCHIVED | `content.archive` داخل الجهة أو platform manage |
| RESTORE | ARCHIVED | DRAFT | platform `content.manage` فقط في Phase 2 |

الـpublished revision لا تتغير عند تحرير سجل منشور. الجمهور يستمر برؤية النسخة السابقة حتى PUBLISH جديد.

## 7. Authorization

### صلاحيات جديدة

```text
content.review
content.approve
content.publish
content.archive
```

- Editor: read/write/submit داخل الجهة.
- Reviewer: read/review داخل الجهة.
- Publisher: read/approve/publish داخل الجهة وفق قرار governance النهائي.
- Entity Admin: إدارة metadata والفريق؛ لا يحصل تلقائيًا على approve/publish إلا بدور إضافي.
- Platform Super Admin: صلاحيات مركزية كاملة.

كل content mutation يتحقق من `primary_entity_id` عبر policy من DB، لا من claims أو body فقط. تغيير الجهة الأساسية عملية platform-only وتسجل audit مستقلًا.

## 8. API V1 المقترحة

### Public

```text
GET /api/v1/content?type=&entityId=&category=&dateFrom=&dateTo=&page=&size=
GET /api/v1/content/{contentId}
GET /api/v1/content/by-slug/{type}/{slug}
GET /api/v1/entities/{entityId}/content?type=&page=&size=
```

Public endpoints تتجاهل أي محاولة لطلب draft وتعيد PUBLISHED فقط. response pagination موحد:

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

### Admin

```text
GET  /api/v1/admin/entities/{entityId}/content?status=&type=&page=&size=
POST /api/v1/admin/entities/{entityId}/content
GET  /api/v1/admin/content/{contentId}
PUT  /api/v1/admin/content/{contentId}
POST /api/v1/admin/content/{contentId}/revisions
POST /api/v1/admin/content/{contentId}/transitions
POST /api/v1/admin/content/{contentId}/attachments
```

لا يقبل create/update حقول `createdBy`, `publishedAt`, `publishedRevisionId` أو owner من body خارج endpoint entity-scoped.

## 9. Compatibility Strategy

### Public facades

- `/api/news` يستعلم NEWS المنشور ويعيد DTO القديم كـarray.
- `/api/news/{id}` يحل `STATIC_NEWS + id` عبر mapping.
- نفس النمط للإعلانات والقرارات والوثائق.
- response القديم يحتفظ بالتواريخ النصية مؤقتًا، مشتقة من typed timestamps.
- يضاف `Link: <canonical>; rel="canonical"` أو حقل غير كاسر عندما يلائم العقد.

### Admin facade

- `/api/admin/content` يقرأ ويكتب Unified Content، لا `admin_content` بعد cutover.
- numeric IDs تحل عبر `ADMIN_CONTENT` mapping.
- `DELETE` القديم ينفذ ARCHIVE ويرجع 204؛ لا حذف فعلي.
- summary يحسب الحالات الموحدة مع mapping: DRAFT/IN_REVIEW/APPROVED ضمن draft الحالي.

### Portal home

`/api/portal/home` يصبح composition query:

- latest NEWS المنشور.
- latest DECISION المنشور.
- DOCUMENT المنشور.
- أعداد فعلية بدل الأرقام الوهمية.
- hero والقنوات والخدمات تبقى configuration/static خارج Content مؤقتًا.

### Frontend fallback

يبقى fallback فقط لحالة unavailable، مع وسم واضح بأنه fallback. لا يعد مصدرًا قابلًا للتحرير ولا يدخل backfill كل مرة.

## 10. Backfill and Reconciliation

### قواعد التحويل

| legacy | unified |
|---|---|
| `منشور` | PUBLISHED |
| `مسودة` | DRAFT |
| `قيد المراجعة` | IN_REVIEW |
| `مؤرشف` | ARCHIVED |

- كل السجلات تربط بجهة PMO في backfill الأول.
- public static lists تعتبر PUBLISHED لأنها معروضة للجمهور اليوم.
- `author` القديم يحفظ كـbyline، ولا يتحول تلقائيًا إلى user account.
- `updated_at` القديم يحفظ كتاريخ provenance، مع timestamps منفصلة لعملية الاستيراد.

### منع التكرار

العناوين المتشابهة لا تدمج آليًا. ينشأ manifest explicit يحدد لكل legacy row أحد الخيارات:

- `CREATE`: سجل جديد.
- `MERGE_INTO`: ربط source إضافي بسجل موحد محدد.
- `SKIP_WITH_REASON`: للبيانات الوهمية غير المراد نشرها.

قبل cutover ينتج reconciliation report يحتوي:

- عدد المصادر، created، merged، skipped.
- كل mapping دون orphan.
- تطابق حالات النشر.
- عدم وجود duplicate canonical slugs.
- مقارنة outputs للـlegacy endpoints قبل/بعد.

## 11. Migration Slices

### Slice 1 — Schema and contracts

- enums وDTOs وstate machine tests.
- migrations additive للجداول الجديدة فقط.
- indexes وconstraints وoptimistic locking.
- لا cutover ولا backfill بعد.

### Slice 2 — Repository and public read model

- persistence وtyped projections.
- `/api/v1/content` خلف configuration flag.
- public queries لا تعيد غير المنشور.

### Slice 3 — Scoped authoring and workflow

- create/revision/transitions.
- permissions الجديدة وentity policy.
- audit success/denied/failure.
- اختبارات cross-entity وinvalid transitions.
- **الحالة: مكتملة في 2026-08-23.** تبقى النسخة المنشورة الحالية مرئية أثناء مراجعة revision أحدث، ولا تتحول القراءة العامة إلا عند النشر.

### Slice 4 — Explicit backfill

- manifest reviewed.
- PMO ownership وlegacy mappings.
- reconciliation report قبل وبعد.
- لا تعديل أو حذف `admin_content`.
- **الحالة: مكتملة في 2026-08-24.** نُفذ manifest المعتمد إلى 12 عنصرًا و14 mapping، واستُبعدت 3 سجلات seed بلا متن، ثم عُطل apply endpoint.

### Slice 5 — Compatibility cutover

- facades القديمة تقرأ unified store.
- admin legacy writes تتحول إلى unified store.
- portal home projection.
- canary/config switch يسمح بالعودة للقراءة القديمة أثناء الاستقرار.
- **Slice 5A مكتملة في 2026-08-24:** shadow comparison أعاد 12/12 وصفر فروقات على PostgreSQL؛ لم يُحوّل أي endpoint.
- **Slice 5B مكتملة في 2026-08-24:** facades ومفاتيح مستقلة لكل نوع جاهزة، مع شرط shadow وfallback تلقائي؛ بقيت جميع المفاتيح مغلقة ولم يبدأ canary.
- **Slice 5C-A مكتملة في 2026-08-24:** observability وعدادات fallback وreadiness cache وrunbook جاهزة.
- **Slice 5C-B قيد المراقبة منذ 2026-08-24:** NEWS تعمل من unified store في Docker المحلي بعد اعتماد صريح، بينما بقيت الأنواع الأخرى على legacy؛ لا انتقال تالٍ قبل اكتمال نافذة 24 ساعة وexit gates.

### Slice 6 — Stabilization

- قياس استخدام endpoints القديمة.
- query plans وpagination limits.
- content security/XSS tests.
- backup/restore وrollback rehearsal على staging.

## 12. Rollout and Rollback

الـrollout ثنائي المرحلة:

1. **Shadow/read comparison:** unified store وbackfill موجودان، والقديم ما زال يخدم الجمهور؛ تقارن النتائج دون تغيير response.
2. **Facade cutover:** configuration يوجه كل نوع على حدة إلى unified store: news ثم announcements ثم decisions ثم documents ثم admin/home.

الـrollback يعيد facade flag إلى legacy source. لا يحذف جداول unified ولا mappings ولا revisions. بعد بدء الكتابة الموحدة يمنع الرجوع إلى legacy writes إلا بخطة forward-fix أو replay واضحة حتى لا يحدث split-brain.

## 13. Test Strategy

### Unit

- state transitions والpermissions.
- slug normalization وحجز slugs القديمة.
- HTML sanitization.
- subtype validation.

### Repository/PostgreSQL

- uniqueness للslug والrevision numbers.
- published revision مستقلة عن current revision.
- filters حسب entity/type/category/date.
- constraints للمرفقات والعلاقات.

### Integration/security

- anonymous يرى PUBLISHED فقط.
- Editor A لا يقرأ draft أو يعدل Content B.
- Reviewer لا ينشر، وPublisher لا يعدل النص دون write.
- invalid transition يعيد 409 ولا يغير الحالة.
- كل denied mutation تسجل audit.
- archived item لا يظهر عامًا، وslug لا يعاد استخدامه.

### Compatibility

- DTO snapshots لكل endpoint قديم.
- numeric IDs القديمة تعيد السجل نفسه.
- admin summary قبل/بعد متوافق دلاليًا.
- portal home لا يحتوي IDs أو تواريخ وهمية.
- frontend build وصفحات التفاصيل تعمل طوال cutover.

## 14. المخاطر وضوابطها

| الخطر | الضابط |
|---|---|
| duplicate records أثناء backfill | manifest explicit + unique legacy mappings |
| تسريب draft بين الجهات | ownership إلزامي + scoped repository/policy tests |
| نشر revision غير معتمدة | publishedRevisionId يتغير فقط من transition service |
| split-brain بين القديم والجديد | مصدر كتابة واحد في كل لحظة؛ لا dual-write طويل الأجل |
| كسر numeric links | LegacyContentMapping وfacades |
| XSS في body | server-side sanitization وCSP لاحقًا |
| JSON metadata غير مضبوط | جداول subtype للحقول القانونية والقيود الحرجة |
| حذف سجل حكومي تاريخي | archive بدل hard delete وaudit append-only |
| تعارض تحرير متزامن | optimistic version + 409 conflict |

## 15. Definition of Done

- جداول المحتوى additive ومتحققة على PostgreSQL.
- كل سجل يملك PMO أو جهة حكومية أخرى صراحة.
- create/revise/review/approve/publish/archive تعمل بسياسة scoped.
- cross-entity tests وXSS tests ناجحة.
- backfill manifest وreconciliation بلا orphan أو duplicate canonical URL.
- endpoints القديمة والواجهة الحالية تعمل دون تغيير كاسر.
- portal home تقرأ المحتوى المنشور من المصدر الموحد.
- backup/restore وfacade rollback ناجحان على staging.
- لا حذف لـ`admin_content` أو static services قبل فترة قياس واعتماد منفصل.

## 16. القرارات المعتمدة

اعتمدت القرارات التالية بتاريخ 2026-08-23:

| # | القرار | الحالة |
|---:|---|---|
| 1 | الـslug فريد عالميًا داخل `(contentType, locale)` | Accepted |
| 2 | `body` بصيغة HTML محدودة ومعقمة server-side | Accepted |
| 3 | state machine متخصص للمحتوى في Phase 2، وgeneric engine في Phase 5 | Accepted |
| 4 | `DELETE` القديم يتحول إلى Archive دون hard delete | Accepted |
| 5 | المحتوى الثابت العام يستورد PUBLISHED ويربط بـPMO | Accepted |
| 6 | التكرارات تعالج عبر manifest صريح دون title-based automatic merge | Accepted |
| 7 | المرفقات metadata وexternal/storage references فقط في Phase 2 | Accepted |
| 8 | لا يعتمد أو ينشر المستخدم revision أنشأها بنفسه إلا Platform Super Admin بحالة break-glass مدققة | Accepted |

اعتماد القرارات يثبت العقد المعماري. يبدأ تنفيذ migrations وكود الإنتاج فقط بتفويض تنفيذ صريح مستقل.

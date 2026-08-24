# Phase 2 Implementation Record — Unified Government Content

> **الحالة:** In progress — Slices 1–4, 5A, 5B, and 5C-A complete; Slice 5C-B NEWS canary active in local Docker
> **التاريخ:** 2026-08-24  
> **المرجع:** `PHASE_2_UNIFIED_CONTENT_PLAN.md`

## Slice 1 — Schema and contracts

### المنفذ

- إنشاء Maven module مستقل باسم `content` ضمن modular monolith.
- تثبيت enums: `ContentType`, `ContentStatus`, `ContentAction`, و`ContentEntityLinkRole`.
- تثبيت DTO contracts الأولية للإنشاء والمراجعة والانتقالات.
- تنفيذ `ContentWorkflowPolicy` لمسار Draft → Review → Approval → Publish → Archive.
- تنفيذ قواعد Request Changes وRestore.
- فرض فصل الواجبات: منشئ revision لا يعتمدها أو ينشرها دون platform break-glass.
- إضافة Flyway migration `V6__unified_content_foundation.sql` بصورة additive.

### Schema V6

أنشأت V6 الجداول التالية دون backfill:

1. `content_items`
2. `content_revisions`
3. `content_entity_links`
4. `taxonomy_terms`
5. `content_taxonomy_assignments`
6. `content_attachments`
7. `decision_details`
8. `document_details`
9. `content_slug_redirects`
10. `legacy_content_mappings`
11. `content_transitions`

تتضمن القيود: canonical slug uniqueness، content/status/action enums، published revision requirement، revision numbering، attachment location، taxonomy uniqueness، subtype dates، وعلاقات الجهات والمستخدمين.

### التحقق الآلي

- `ContentWorkflowPolicyTest`: خمسة اختبارات ناجحة.
- bootstrap: 35 اختبارًا ناجحًا، صفر failures وصفر errors.
- Flyway طبق V1–V6 على H2 بنجاح.
- اختبارات schema أثبتت وجود 11 جدولًا، uniqueness للslug، ورفض PUBLISHED دون published revision.

### تحقق PostgreSQL/Docker

- PostgreSQL `16.15` ترقى من V5 إلى V6 بنجاح.
- Spring Boot وHibernate validation بدآ دون خطأ.
- `content_items = 0` كما هو متوقع قبل backfill.
- `admin_content = 5` دون تغيير.
- `/actuator/health = UP`.
- `/api/news` ما زال يعيد 3 سجلات.
- `/api/portal/home` ما زال يعيد 3 أخبار.

## Slice 2 — Persistence and public read API

### المنفذ

- إضافة JPA entities وrepositories للـcontent items وrevisions والتصنيفات وربطها بالجهة الأساسية.
- بناء read model عام لا يعيد إلا `PUBLISHED` والنسخة المشار إليها صراحةً في `published_revision_id`.
- إضافة pagination بحد أقصى 100، وفلاتر النوع والجهة والتصنيف ونطاق التاريخ.
- توليد canonical paths بحسب نوع المحتوى وإرجاع بيانات الجهة والتصنيفات مع كل نتيجة.
- إضافة واجهات القراءة التالية:
  - `GET /api/v1/content`
  - `GET /api/v1/content/{id}`
  - `GET /api/v1/content/by-slug/{type}/{slug}`
  - `GET /api/v1/entities/{entityId}/content`
- وضع المتحكم كاملًا خلف `features.unified-content-read.enabled`، والقيمة الافتراضية `false`.
- إبقاء الواجهات القديمة والمصادر الحالية فعالة دون backfill أو cutover.
- السماح بمسار الأخطاء القياسي `/error` كي تبقى استجابة المسار غير المسجّل 404 بدل 401 مضللة.

### ضمانات واختبارات

- اختبار التكامل يثبت عدم ظهور المسودات في القائمة أو التفاصيل.
- اختبار التفاصيل بالمعرّف والـslug، وربط الجهة والتصنيف والمسار الدائم.
- اختبار الفلاتر والتقسيم، ورفض حجم صفحة يتجاوز 100 ونطاق تاريخ معكوس.
- اختبار مستقل يثبت أن إغلاق علم الميزة يلغي تسجيل API ويعيد 404.
- اختبارات H2 تتحقق من Flyway V1–V6 وHibernate mappings.
- الاختبار النهائي: 38 اختبار bootstrap و5 اختبارات content و14 اختبار identity؛ 57 اختبارًا ناجحًا إجمالًا دون failures أو errors.
- PostgreSQL 16.15 بدأ مع Hibernate validation دون أخطاء، مع بقاء V6 أحدث migration.
- اختبار Docker: health = 200، والواجهة الجديدة المغلقة = 404، و`/api/news` = 200 مع 3 سجلات.
- تحقق البيانات: `content_items = 0` و`admin_content = 5`.

## Slice 3 — Scoped authoring and workflow

### المنفذ

- إضافة واجهات إدارية موحدة لإنشاء المحتوى، قراءته ضمن الإدارة، إنشاء revisions غير قابلة للاستبدال، وتنفيذ انتقالات workflow.
- فرض نطاق الجهة من `primaryGovernmentEntity` المحفوظ في قاعدة البيانات، وليس من claims أو مدخلات يرسلها العميل.
- إضافة صلاحيات `content.review` و`content.approve` و`content.publish` و`content.archive`، مع إبقاء الإدارة المركزية تحت `content.manage`.
- توزيع الصلاحيات الافتراضية على PMO Admin وReviewer وPublisher، ومنع مستخدم جهة من إدارة محتوى جهة أخرى.
- تسجيل audit مستقل لحالات النجاح والمنع والفشل، بما في ذلك المحاولات التي تتراجع معاملتها الأساسية.
- تطبيق allowlist sanitizer على HTML قبل حفظ revision.
- تطبيق فصل الواجبات: منشئ النسخة لا يعتمدها أو ينشرها؛ والاستثناء break-glass صريح، مركزي فقط، ويتطلب سببًا غير فارغ ويُسجل في audit.
- السماح بإنشاء revision جديدة لمحتوى منشور وإدخالها للمراجعة مع استمرار عرض `published_revision_id` القديمة للعامة، ثم تبديل النسخة العامة فقط عند `PUBLISH`.
- إبقاء API القراءة الموحد خلف feature flag المغلق افتراضيًا، وعدم تغيير أي legacy endpoint أو frontend.

### الواجهات الإدارية الجديدة

- `GET /api/v1/admin/entities/{entityId}/content`
- `POST /api/v1/admin/entities/{entityId}/content`
- `GET /api/v1/admin/content/{contentId}`
- `POST /api/v1/admin/content/{contentId}/revisions`
- `POST /api/v1/admin/content/{contentId}/transitions`

### التحقق

- اختبارات التكامل تغطي التعقيم، العزل بين الجهات، revisions المتتابعة، الانتقالات غير الصحيحة، فصل الواجبات، break-glass، وعدم تسرب المسودات.
- اختبار دورة تحديث المحتوى المنشور يثبت بقاء النسخة القديمة عامة أثناء مراجعة النسخة الجديدة، ثم التبديل الذري عند النشر.
- الاختبار الكامل: 42 اختبار bootstrap و6 اختبارات content و14 اختبار identity؛ 62 اختبارًا ناجحًا دون failures أو errors.
- Docker/PostgreSQL: health = 200، وAPI القراءة المغلق = 404، والمسار الإداري دون توثيق = 401، و`/api/news` ما زال يعيد 3 سجلات.
- تحقق قاعدة البيانات: Flyway V6، و`content_items = 0`، و`admin_content = 5`، وأربع صلاحيات workflow، و`content_transitions = 0` قبل backfill.

### غير منفذ عند إغلاق Slice 3

- backfill manifest أو تنفيذ backfill/reconciliation.
- compatibility cutover أو تحويل legacy writes.
- أي تغيير frontend.

## Slice 4 — Manifest, reconciliation, and backfill

- إنشاء manifest V1 صريح يغطي 17 مصدرًا حاليًا بقرارات 12 `CREATE` و2 `MERGE_INTO` و3 `SKIP_WITH_REASON`.
- تثبيت snapshot للنوع والعنوان والحالة والتصنيف والكاتب والتاريخ والملخص لكشف source drift.
- استبعاد ثلاثة سجلات seed إدارية بلا متن موثوق بدل إنشاء محتوى وهمي.
- إضافة reconciliation يكتشف المصادر الناقصة والتكرار وتعارض الحالات والـslugs والـorphan mappings ووجود جهة PMO.
- إضافة واجهة قراءة إدارية محمية: `GET /api/v1/admin/content-backfill/reconciliation`.
- إضافة apply operation معاملاتي وidempotent خلف feature flag مغلق افتراضيًا وتأكيد صريح.
- نتيجة PostgreSQL: 12 content items و12 revisions و14 mappings و13 taxonomy assignments، مع صفر orphan وصفر slug collision.
- إعادة التطبيق أعادت no-op دون أي duplicate، وبقي `admin_content = 5` دون تغيير.
- تعطيل apply endpoint بعد التنفيذ والتحقق من إعادته 404، مع إبقاء unified public read مغلقًا.
- الاختبار الكامل: 48 اختبار bootstrap و6 content و14 identity؛ 68 اختبارًا ناجحًا دون failures أو errors.

راجع `PHASE_2_BACKFILL_MANIFEST_REVIEW.md` لتفاصيل القرارات ونتيجة التنفيذ.

## الخطوة التالية

## Slice 5A — Shadow comparison

- إضافة تقرير إداري read-only يقارن 12 مصدرًا عامًا بالسجلات الموحدة عبر mappings الصريحة.
- مقارنة العدد والترتيب والعنوان والملخص والتاريخ والتصنيف والمسار، وكشف أي unified items إضافية.
- مقارنة أقسام الصفحة الرئيسية الثلاثة المعتمدة على NEWS وDECISION وDOCUMENT، مع استبعاد الأقسام الثابتة صراحةً.
- اكتشاف وإصلاح عدم توافق PostgreSQL في الفلاتر الاختيارية باستبدال JPQL ذي معاملات null بـJPA Specifications ديناميكية.
- نتيجة PostgreSQL: 12/12 mapped، صفر فروقات، وكل الأنواع الأربعة و3/3 من أقسام home جاهزة للـcanary.
- الاختبار الكامل: 49 bootstrap و6 content و14 identity؛ 69 اختبارًا ناجحًا.
- لم يتغير أي legacy endpoint، وبقي unified read مغلقًا.

راجع `PHASE_2_SHADOW_COMPARISON_RECORD.md` للتقرير الكامل.

## الحالة التشغيلية الحالية

Slice 5C-B: فُعّل NEWS canary فقط في Docker المحلي عند `2026-08-24T14:02:24Z` بعد اعتماد صريح؛ الأنواع الثلاثة الأخرى ما زالت على legacy.

## Slice 5B — Compatibility facades without cutover

- إضافة query contracts في وحدات الأخبار والإعلانات والقرارات والوثائق، وتحويل المتحكمات والصفحة الرئيسية للاعتماد عليها دون تغيير المسارات أو DTOs.
- إضافة facades في bootstrap تعيد إسقاط UUID/slug الموحد إلى المعرفات الرقمية والتواريخ والتصنيفات القديمة عبر `legacy_content_mappings`.
- إضافة أربعة feature flags مستقلة، كلها `false` افتراضيًا وفي Docker: news، announcements، decisions، documents.
- حماية fail-safe: لا يكفي تفعيل المفتاح؛ يجب أن يكون تقرير shadow لذلك النوع جاهزًا، وأي خطأ في المقارنة أو الإسقاط يعيد القراءة القديمة تلقائيًا.
- إضافة نقطة قراءة إدارية محمية: `GET /api/v1/admin/content-compatibility/status` تعرض configured flag وshadow readiness وeffective source لكل نوع.
- ربط أقسام latestNews وdecisions وdocuments في الصفحة الرئيسية بنفس facades، دون تغيير الأقسام الثابتة.
- اختبار التكامل بالتفعيل المؤقت بعد backfill أثبت تطابق العقود الأربعة والقوائم والتفاصيل والمعرفات، مع `effectiveSource=UNIFIED`.
- الوضع التشغيلي النهائي لهذه الشريحة يبقى `LEGACY` لكل الأنواع؛ لا canary ولا تغيير في الكتابة.
- الاختبار الكامل: 51 bootstrap و6 content و14 identity؛ 71 اختبارًا ناجحًا دون failures أو errors.
- تحقق Docker/PostgreSQL: الأنواع الأربعة `shadowReady=true`، وكل flags مغلقة و`effectiveSource=LEGACY`؛ health وlegacy news يعيدان 200.

راجع `PHASE_2_COMPATIBILITY_FACADES_RECORD.md` للتفاصيل وخطة التفعيل والرجوع.

## Slice 5C-A — Canary observability and runbook

- إضافة عداد وtimer عبر Micrometer لكل نوع وعملية ومصدر فعلي وسبب fallback، بوسوم ثابتة منخفضة cardinality.
- إظهار request/fallback counters وأسبابها في واجهة الحالة الإدارية المحمية.
- إضافة logging آمن لحالة فشل unified projection دون بيانات محتوى أو مستخدم.
- تخزين نتيجة shadow readiness لمدة 30 ثانية افتراضيًا بدل تشغيل المقارنة الكاملة مع كل طلب، مع fail-closed إلى legacy عند الخطأ.
- توثيق gates التفعيل والنجاح والrollback وترتيب الأنواع في `PHASE_2_CANARY_RUNBOOK.md`.
- اختبار مسار unified ومسار `SHADOW_NOT_READY` fallback والتحقق من meters والعدادات.
- لم يُفعّل أي canary، وبقيت كل flags مغلقة.
- الاختبار الكامل: 55 bootstrap و6 content و14 identity؛ 75 اختبارًا ناجحًا دون failures أو errors.
- تحقق Docker/PostgreSQL: كل الأنواع `shadowReady=true` و`effectiveSource=LEGACY`، وظهرت طلبات smoke في legacy counters مع صفر automatic fallbacks.

## Slice 5C-B — NEWS canary activation

- تفعيل `FEATURES_UNIFIED_CONTENT_COMPATIBILITY_NEWS_ENABLED=true` في حاوية backend المحلية فقط.
- تثبيت أعلام ANNOUNCEMENT وDECISION وDOCUMENT على `false` صراحةً؛ بقي مصدرها الفعلي `LEGACY`.
- أصبحت NEWS `configuredForUnified=true` و`shadowReady=true` و`effectiveSource=UNIFIED`.
- تطابقت list وdetail و`portalHome.latestNews` مع baseline قبل التفعيل، وأعيدت 404 للمعرّف المفقود كما قبل التحويل.
- بعد smoke الأولي: 3 unified requests، صفر automatic fallbacks، و12/12 mapping مع صفر فروقات.
- بدأت نافذة المراقبة في `2026-08-24T14:02:24Z`. لا يُفعّل نوع آخر قبل مرور 24 ساعة، وصول NEWS إلى 100 طلب على الأقل، واجتياز exit gates.
- التفعيل محلي وليس نشرًا على production، ولا يغيّر default flags أو قاعدة البيانات أو مسار الكتابة.

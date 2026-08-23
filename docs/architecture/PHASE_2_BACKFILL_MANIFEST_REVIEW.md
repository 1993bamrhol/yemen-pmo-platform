# Phase 2 — Backfill Manifest and Execution Record V1

> **الحالة:** Applied successfully; apply endpoint disabled  
> **التاريخ:** 2026-08-24  
> **الـmanifest:** `backend/bootstrap/src/main/resources/backfill/unified-content-v1.json`  
> **النطاق:** Backfill فقط؛ لم يحدث compatibility cutover أو حذف للمصادر القديمة.

## 1. القرار المعتمد

يغطي manifest جميع المصادر الحالية وعددها 17 بقرار صريح لكل مصدر:

| القرار | العدد |
|---|---:|
| `CREATE` | 12 |
| `MERGE_INTO` | 2 |
| `SKIP_WITH_REASON` | 3 |
| العناصر الموحدة الناتجة | 12 |

دُمج المصدران العامان التاليان مع سجلين إداريين منشورين متطابقين:

| المصدر | الهدف الموحد |
|---|---|
| `STATIC_NEWS:NEWS:1` | `pmo-news-digital-government-services-priorities-2026-08-16` |
| `STATIC_DECISIONS:DECISION:1` | `pmo-decision-visual-identity-2026-08-12` |

## 2. قرارات SKIP

اعتمد استبعاد ثلاثة سجلات seed سطحية بدل اختلاق متن لها:

| المصدر | الحالة القديمة | السبب المثبت في manifest |
|---|---|---|
| `ADMIN_CONTENT:ANNOUNCEMENT:2` | DRAFT | metadata بلا متن؛ تجاوزه إعلان عام منشور وأكثر اكتمالًا |
| `ADMIN_CONTENT:DOCUMENT:4` | ARCHIVED | metadata مؤرشفة بلا متن موثوق أو مرفق |
| `ADMIN_CONTENT:NEWS:5` | IN_REVIEW | metadata قيد المراجعة بلا متن تحريري، ولا يجوز ترقيتها أثناء الترحيل |

لا تنشأ mappings للعناصر المستبعدة لأنها لا تملك `content_item_id`. تبقى قراراتها موثقة في manifest ويستمر dry-run في التحقق من عدم تغير مصادرها.

## 3. ضمانات التنفيذ

- فحص reconciliation إجباري داخل المعاملة وقبل أول كتابة.
- قفل صف جهة رئاسة مجلس الوزراء لمنع تنفيذين متزامنين.
- تأكيد صريح يطابق schema version والعبارة `APPLY_UNIFIED_CONTENT_V1`.
- idempotency عبر `legacy_content_mappings`؛ إعادة التشغيل لا تنشئ سجلات جديدة.
- transaction واحدة لكل العملية، مع rollback عند partial/conflicting mapping.
- إنشاء revision أولى مع HTML معقم، byline القديم، وتاريخ المصدر ضمن provenance.
- حفظ `display_metadata` الذي يحدد manifest version وcanonical key وكل legacy source وتاريخه وتصنيفه.
- إنشاء وربط `CONTENT_CATEGORY` taxonomy terms المعتمدة.
- ربط جميع العناصر بجهة رئاسة مجلس الوزراء.
- عدم تعديل أو حذف `admin_content` أو القوائم الثابتة أو legacy endpoints.

## 4. نتيجة PostgreSQL

| المؤشر | النتيجة |
|---|---:|
| `content_items` | 12 |
| `content_revisions` | 12 |
| PUBLISHED مع `published_revision_id` | 12 |
| `legacy_content_mappings` | 14 |
| `content_taxonomy_assignments` | 13 |
| `admin_content` | 5 دون تغيير |
| orphan mappings | 0 |
| canonical slug collisions | 0 |

التشغيل الأول أعاد `executed=true` مع 12 عنصرًا و14 mapping. التشغيل الثاني أعاد `executed=false` وتعرف على 12 عنصرًا و14 mapping موجودة، ما يثبت idempotency على PostgreSQL الفعلي.

## 5. واجهات التشغيل

### Reconciliation

`GET /api/v1/admin/content-backfill/reconciliation`

- قراءة فقط.
- تتطلب صلاحية platform-scoped باسم `content.manage`.
- تبقى متاحة لكشف source drift أو مشاكل mappings.

### Apply

`POST /api/v1/admin/content-backfill/apply`

- تتطلب `content.manage` على نطاق المنصة، schema version، وعبارة تأكيد ثابتة.
- لا تسجل إلا عندما تكون `features.unified-content-backfill-apply.enabled=true`.
- فُعّلت مؤقتًا للتنفيذ ثم عُطلت؛ تحقق التشغيل النهائي أن المسار يعيد 404.

## 6. التحقق

- 48 اختبار bootstrap و6 اختبارات content و14 اختبار identity؛ 68 اختبارًا ناجحًا دون failures أو errors.
- feature-flag test يثبت غياب apply endpoint افتراضيًا.
- اختبارات المعاملة تثبت الإنشاء، التصنيفات، المراجعات، عدم المساس بـlegacy، وإعادة التشغيل كـno-op.
- `/actuator/health = 200` بعد تعطيل apply.
- API القراءة الموحد ما زال مغلقًا افتراضيًا ويعيد 404.

## 7. بوابة Slice 5

الـbackfill مكتمل، لكن المصادر القديمة ما زالت تخدم الجمهور. قبل compatibility cutover يجب بناء shadow comparison يقارن payloads والترتيب والتواريخ والروابط لكل من الأخبار والإعلانات والقرارات والوثائق والصفحة الرئيسية، ثم تحويل نوع واحد في كل مرة خلف feature flags مستقلة.

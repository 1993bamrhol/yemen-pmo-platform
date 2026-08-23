# Phase 2 — Shadow Comparison Record

> **الحالة:** Complete — ready to build type-scoped canary facades  
> **التاريخ:** 2026-08-24  
> **مهم:** لم يحدث cutover؛ جميع legacy endpoints ما زالت تقرأ المصادر القديمة.

## 1. نطاق المقارنة

تقارن الخدمة كل مصدر عام قديم بالسجل الموحد المرتبط به عبر `legacy_content_mappings`، دون الاعتماد على تشابه العناوين. تشمل المقارنة:

- وجود mapping ووجود نسخة منشورة قابلة للقراءة.
- العنوان والملخص والتاريخ والتصنيف.
- canonical path الصحيح بحسب نوع المحتوى.
- عدد عناصر القائمة وترتيبها.
- العناصر الموحدة الإضافية التي قد تغيّر payload بعد cutover.
- أقسام الصفحة الرئيسية التي تعتمد مباشرة على الأخبار والقرارات والوثائق.

لا تدخل hero والإحصاءات والقنوات والخدمات والبيانات الثابتة الأخرى في content parity؛ تظهر صراحة ضمن `excludedStaticSections`.

## 2. واجهة التقرير

`GET /api/v1/admin/content-shadow-comparison`

- read-only وتعمل داخل transaction للقراءة فقط.
- تتطلب صلاحية platform-scoped باسم `content.manage`.
- لا تسجل audit event ولا تعدل counters أو mappings.
- تعيد `readyForCanary=false` عند mapping ناقص أو فرق حقل أو ترتيب أو عنصر موحد إضافي.

## 3. نتيجة PostgreSQL

| النوع | Legacy | Mapped | Unified published | إضافي | العدد | الترتيب | الحقول | الجاهزية |
|---|---:|---:|---:|---:|---|---|---|---|
| NEWS | 3 | 3 | 3 | 0 | مطابق | مطابق | مطابق | جاهز |
| ANNOUNCEMENT | 3 | 3 | 3 | 0 | مطابق | مطابق | مطابق | جاهز |
| DECISION | 3 | 3 | 3 | 0 | مطابق | مطابق | مطابق | جاهز |
| DOCUMENT | 3 | 3 | 3 | 0 | مطابق | مطابق | مطابق | جاهز |

النتيجة الإجمالية:

- `legacyPublicSources = 12`
- `mappedUnifiedItems = 12`
- `differences = 0`
- `reconciliationReady = true`
- `readyForCanary = true`
- أقسام الصفحة الرئيسية المقارنة: 3، والمتطابقة: 3.

## 4. إصلاح توافق PostgreSQL

كشف التشغيل الفعلي مشكلة لم تظهر في H2: استعلام JPQL القديم استخدم شروطًا من نمط `:optionalDate is null`، ولم يتمكن PostgreSQL من استنتاج نوع معامل timestamp الفارغ.

استُبدل الاستعلام الثابت بـJPA `Specification` تبني predicates للفلاتر الموجودة فقط. النتيجة:

- لا تمر معاملات null غير محددة النوع إلى PostgreSQL.
- بقيت فلاتر النوع والجهة والتصنيف والتاريخ والتقسيم كما هي.
- بقي `EntityGraph` لتحميل revision والجهة اللازمة للـread model.
- نجحت اختبارات القراءة وshadow comparison على H2، ثم نجح التقرير نفسه على PostgreSQL.

## 5. التحقق

- 49 اختبار bootstrap و6 content و14 identity؛ 69 اختبارًا ناجحًا دون failures أو errors.
- التقرير الفعلي على PostgreSQL أعاد صفر فروقات وجاهزية كاملة للـcanary.
- `/actuator/health = 200`.
- unified public read ما زال مغلقًا ويعيد 404.
- `/api/news` و`/api/announcements` و`/api/decisions` و`/api/documents` ما زالت تعيد 3 عناصر لكل نوع.
- apply endpoint بقي معطلًا.

## 6. بوابة Slice 5B

الخطوة التالية ليست تحويلًا مباشرًا. تُبنى compatibility facades تحافظ حرفيًا على legacy DTOs والمسارات الرقمية، مع feature flag مستقل لكل نوع وقيمة افتراضية `legacy`. لا يُفعّل أي نوع إلا إذا أعاد shadow comparison الخاص به `readyForCanary=true`، ويظل الرجوع الفوري إلى المصدر القديم ممكنًا دون تعديل البيانات.

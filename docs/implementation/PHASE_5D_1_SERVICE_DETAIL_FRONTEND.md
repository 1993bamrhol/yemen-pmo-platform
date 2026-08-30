# Phase 5D.1 — Service Detail Frontend Integration

> **التاريخ:** 2026-08-30
>
> **الفرع:** `main`
>
> **نقطة البداية:** `de992220ec8820ca3fc1bba7aceabc3842ff34cc`
>
> **النطاق:** Frontend فقط؛ لا Backend أو Database أو Figma أو deployment
>
> **التوصية:** **READY TO COMMIT**
>
> **جاهزية الشاشة:** **READY WITH CONDITIONS**

## 1. النتيجة

أضيفت صفحة تفاصيل خدمة عامة حقيقية، مربوطة حصريًا بعقد Government Services الذي أُنجز في Phase 5C.4. لا تحتوي الصفحة على service fixture أو fallback محلي أو بيانات من `site-data.ts` أو أمثلة Figma.

تُعرض فقط الاستجابة التي يسمح بها الـpublic API؛ أي سجل غير `PUBLISHED + VERIFIED` أو مالكه غير نشط لا يصل إلى الصفحة أصلًا وفق عقد Backend. لا يحاول Frontend تجاوز هذا القرار أو استبدال الاستجابة ببيانات أخرى.

## 2. Route

- Route العام النهائي: `/services/[slug]`.
- مثال locator بنيوي فقط: `/services/{service-slug}`.
- الاختيار يمدد `/services` الموجود ولا يغيّر أي URL قائم.
- يتطابق مع `canonicalPath` المثبت في Phase 5C.4.
- لم يتغير مسار `/services` نفسه ولم يُنفذ Service Catalog في هذه الدفعة.

## 3. API source

المصدر الوحيد:

```http
GET /api/v1/services/by-slug/{slug}
```

أضيف إلى `frontend/src/lib/api.ts`:

- `GovernmentServiceDetail`
- `GovernmentServiceDetailItem`
- `GovernmentServiceChannel`
- `GovernmentServiceOwner`
- `GovernmentServiceSource`
- `api.getGovernmentServiceBySlug(slug)`

يُشفّر الـslug قبل إدخاله في URL. لا يقرأ Frontend رسائل Backend الداخلية ولا يعرضها للمستخدم. يستخدم `ApiError.status` لتمييز 404 فقط، وتتحول بقية أخطاء الشبكة/HTTP إلى رسالة عامة آمنة.

## 4. Field mapping

| UI | API field | السلوك |
|---|---|---|
| عنوان الخدمة | `officialName` | `h1` وحيد للصفحة |
| الجهة المقدمة | `ownerEntity.officialName` | نص فقط؛ لا رابط قبل جاهزية Entity frontend route |
| الملخص | `summary` | يظهر في Hero |
| وصف الخدمة | `description` | قسم مستقل فقط إذا كان موجودًا ومختلفًا عن الملخص |
| الأهلية | `eligibility[]` | قائمة مرتبة؛ تختفي عند الفراغ |
| المتطلبات | `requirements[]` | قائمة مرتبة؛ تختفي عند الفراغ |
| الخطوات | `steps[]` | `ol` دلالية بالترتيب؛ تختفي عند الفراغ |
| الرسوم | `fees` | Fact اختياري |
| مدة الإنجاز | `processingTime` | Fact اختياري |
| قنوات التقديم | `channels[]` | أسماء القنوات وبطاقاتها بالترتيب؛ تختفي عند الفراغ |
| رابط القناة | `channels[].actionUrl` | يظهر فقط إذا كان HTTPS صالحًا، مع `target=_blank` و`rel=noopener noreferrer` وبيان فتح نافذة جديدة |
| المصدر | `source.type` و`source.reference` | provenance عام من العقد؛ reference يُعرض كنص لا كـtrusted HTML |
| تواريخ النشر والتحديث والاعتماد | `publishedAt`, `updatedAt`, `source.verifiedAt` | `time` مع قيمة machine-readable وتنسيق عربي |

لا يُشتق حقل “المستفيدون” من نصوص الأهلية، ولا تُخترع category أو stats أو contact/support data.

## 5. Reused components

- Public Shell الموجود عبر `(public)/layout.tsx`.
- `PageContainer`.
- `Section`.
- `SectionHeader`.
- `Breadcrumbs`.
- `Badge`.
- `TextLink`.
- `Button` في unexpected runtime error boundary فقط.
- `MetadataList`.
- `ContentState` لحالات loading و404/API error/unexpected error.

لم يُعد بناء أي Primitive أو Shell component، ولم تُنشأ `ServiceCard` جديدة لأن الصفحة لا تعرض Related Services دون API مثبت.

## 6. Page composition

ترتيب النجاح:

1. Breadcrumbs.
2. Hero: اعتماد الخدمة، الاسم، الجهة المالكة، الملخص، والقناة الرسمية عند توفر HTTPS action URL.
3. معلومات سريعة: مدة الإنجاز والرسوم وقنوات التقديم المتوفرة فقط.
4. وصف الخدمة عند توفره.
5. الأهلية والمتطلبات، كل جزء مستقل واختياري.
6. خطوات التقديم كقائمة مرتبة ظاهرة.
7. طرق تقديم الخدمة وقنواتها الاختيارية.
8. المصدر وتواريخ النشر/التحديث/الاعتماد.

لا يظهر أي heading فارغ. غياب صورة لا يؤثر لأن عقد الخدمة لا يطلب صورة، ولم تُضف صورة أو شعار حكومي وهمي.

## 7. Loading, 404, and error behavior

| الحالة | السلوك |
|---|---|
| Loading | `loading.tsx` مع `ContentState`, `aria-busy`, وlabel عربي |
| 404 | `notFound()` عند `ApiError(404)` ثم route-local `not-found.tsx`; لا يميز للمستخدم بين missing وnon-public |
| API/network error | رسالة عربية عامة، retry لنفس URL، ولا fallback غير موثق |
| Unexpected render error | route-local client error boundary مع زر retry؛ لا يعرض error message أو digest |
| Missing optional sections | لا يرسم القسم أو عنوانه |

## 8. Accessibility

- `lang=ar` و`dir=rtl` موروثان من root layout.
- Public Shell يحتفظ بـSkip Link إلى `main#main-content`.
- `h1` واحد في حالات النجاح و404/error؛ الأقسام تستخدم outline دلاليًا.
- الخطوات `ol` وليست عناصر disclosure وهمية أو `div` تفاعلية.
- الروابط التفاعلية تستخدم Primitives الحالية ذات touch target 48px.
- External links تعلن نصيًا أنها تفتح نافذة جديدة وتستخدم خصائص الأمان المناسبة.
- Breadcrumbs تستعمل `aria-current=page`.
- loading/error announcements تعتمد `ContentState` الحالي.
- التركيز المرئي و`prefers-reduced-motion` يرثان Foundations/Primitives المعتمدة، ولا تضيف الصفحة حركة جديدة.
- لا تعتمد أي معلومة على اللون وحده.

## 9. Responsive verification

تحقق Browser runtime على الصفحة الفعلية لحالة API unavailable:

| viewport | inner width | horizontal overflow | main/RTL |
|---:|---:|---|---|
| 320 | 320 | لا | `main-content`, RTL |
| 360 | 360 | لا | `main-content`, RTL |
| 1024 | 1024 | لا | `main-content`, RTL |
| 1440 | 1440 | لا | `main-content`, RTL |

كما تم التحقق من:

- main retry link بارتفاع 48px.
- لا console warnings أو errors في المقاسات الأربعة.
- التفاف عناصر الحالة العربية دون overflow.
- CSS يستخدم `min-inline-size: 0` و`overflow-wrap: anywhere` في العناوين والقيم والبطاقات.
- متطلبات وخطوات وقنوات النجاح auto-height ولا تستخدم ارتفاعات Figma الثابتة، ولذلك تستوعب النص العربي الطويل بنيويًا.

التحقق البصري الكامل لعناوين وخطوات عربية طويلة في **success state** مؤجل حتى وجود سجل رسمي موثق، التزامًا بمنع fixtures الإنتاجية أو fake API fallback.

## 10. Figma comparison

المصدر:

- Desktop `71:16`.
- Mobile `71:17`.

### Preserved

- task-first hero والـbreadcrumb hierarchy.
- 1160px Desktop container و328px content عند 360px.
- Civic Blue للتفاعل، National Red محصور في Public Shell identity.
- التدرج من Hero إلى facts ثم eligibility/requirements ثم steps ثم metadata.
- Mobile single-column وDesktop multi-column حيث يسمح المحتوى.
- الخطوات الأساسية ظاهرة وليست مخفية افتراضيًا.

### Intentional due to unavailable verified data

- لم يُنسخ عنوان الخدمة أو الرسوم أو الزمن أو الأهلية أو الخطوات التوضيحية.
- Related Services محذوفة لأن 5C.4 لا يوفّر علاقة أو endpoint لها.
- Support/contact CTAs التوضيحية محذوفة لعدم وجود بيانات دعم رسمية في العقد.
- لا owner link قبل تنفيذ Entity frontend route.
- لا empty placeholder داخل نجاح الخدمة؛ القسم الاختياري يختفي نظيفًا.

### Runtime-content variation

- عدد Facts والقنوات وطول الصفحة يتبعان الحقول الحقيقية.
- خطوات Desktop وMobile تستخدم قائمة مرتبة ظاهرة بدل Accordion تفاعلي؛ هذا يطبق قرار Design-to-Code المعتمد بألا تُخفى الخطوات الأساسية.
- يعاد استخدام motif المعماري الموجود في Public assets بدل تنزيل asset detached جديد من Figma.

### Defects

- لا يوجد defect مثبت في حالات loading/API unavailable/responsive التي أمكن تشغيلها.
- لا يمكن إصدار حكم pixel-level على success state قبل وجود record حقيقي `PUBLISHED + VERIFIED` بجهة مالكة نشطة.

## 11. Current data limitation

مصدر Backend في المستودع يتضمن V9 وعقد الخدمات عند checkpoint المحدد، لكن بيئة Docker المحلية القائمة وقت المراجعة لم تكن قد طبقت V9: جدول `government_services` غير موجود في قاعدة Docker الحالية، والـcontainer القائم أقدم من checkpoint. لم تُعد الحاويات ولم تُشغّل migration ولم تُضف أي بيانات لأن ذلك خارج تفويض Phase 5D.1.

الشرط المتبقي للتحقق الكامل:

1. تشغيل Backend checkpoint/V9 في بيئة اختبار معتمدة بقرار منفصل.
2. إدخال سجل حكومي حقيقي بمصدر موثق.
3. جعله `PUBLISHED + VERIFIED` مع جهة مالكة نشطة عبر المسار الإداري المعتمد.
4. إعادة visual comparison للنجاح على 320/360/1024/1440 مقابل `71:16` و`71:17`.

## 12. Verification results

- `npm run lint`: **PASS**.
- `npx tsc --noEmit`: **PASS**.
- `npm run build`: **PASS**؛ route `/services/[slug]` ظاهر كـdynamic SSR route.
- `git diff --check -- frontend/src`: **PASS** مع LF/CRLF warning المعتاد فقط.
- Browser runtime: **PASS** للحالة المتاحة فعليًا (API unavailable) على المقاسات المطلوبة.
- RTL/main/skip-link target/48px/no horizontal overflow/no console warnings: **PASS** ضمن الحالة المختبرة.
- Full success visual/data validation: **PENDING VERIFIED RECORD**.

## 13. Files changed

- `frontend/src/lib/api.ts`
- `frontend/src/app/(public)/services/[slug]/page.tsx`
- `frontend/src/app/(public)/services/[slug]/loading.tsx`
- `frontend/src/app/(public)/services/[slug]/not-found.tsx`
- `frontend/src/app/(public)/services/[slug]/error.tsx`
- `frontend/src/app/(public)/services/[slug]/ServiceDetail.module.css`
- `docs/implementation/PHASE_5D_1_SERVICE_DETAIL_FRONTEND.md`

لا توجد تغييرات مقصودة في Backend أو Database أو Figma أو feature flags. بقيت `backend/**/target/**` والـhistorical design artifacts الموجودة مسبقًا خارج نطاق العمل ولم تُنظف.

## 14. Readiness and recommendation

**Service Detail: READY WITH CONDITIONS**

الكود، route، API adapter، optional sections، error isolation، accessibility، responsive behavior، وproduction build جاهزة. الشرط المتبقي ليس frontend implementation defect؛ إنه توفر record حقيقي موثق وبيئة Backend مطبّق عليها V9 لإتمام success-state visual validation.

**Recommendation: READY TO COMMIT**

أي checkpoint لاحق يجب أن يضم فقط الملفات السبعة أعلاه، وألا يضم `target/**` أو design/review artifacts أو أي ملف generated.

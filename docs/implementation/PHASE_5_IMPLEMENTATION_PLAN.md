# Phase 5 — Implementation Planning & Design-to-Code Contract

> **الحالة:** Proposed — للمراجعة والاعتماد قبل أي تنفيذ  
> **التاريخ:** 2026-08-27  
> **المشروع:** `yemen-pmo-platform`  
> **قرار التصميم السابق:** Phase 4.3 — `READY FOR HANDOFF`  
> **التوصية التنفيذية الحالية:** **HOLD لبدء تنفيذ الصفحات الثلاث كاملة**؛ مع إمكانية اعتماد Slice 0 تحضيرية فقط بعد إغلاق بوابات البيانات والمسارات الواردة في القسم 16.  
> **حدود هذه الوثيقة:** تخطيط وعقد تصميم-إلى-كود فقط. لم تُعدّل الواجهة أو الـbackend أو قاعدة البيانات أو feature flags أو Figma، ولم يُنشأ commit أو deployment.

## 1. مصادر الحقيقة ونطاق الفحص

### 1.1 مرجع Figma المعتمد

- الملف: `cSFveyYsAe08Xr5kiZMXum`
- الصفحة: `04 — Key Screens` (`71:2`)
- Homepage Desktop: `71:10` — `1440 × 3384`
- Homepage Mobile: `71:11` — `360 × 4012`
- Ministry Desktop: `71:13` — `1440 × 3280`
- Ministry Mobile: `71:14` — `360 × 4112`
- Service Detail Desktop: `71:16` — `1440 × 3224`
- Service Detail Mobile: `71:17` — `360 × 4408`

تم فحص الملف قراءةً فقط عبر Figma Plugin API، بما يشمل الصفحة `02 — Components` (`22:2`)، الصفحة `03 — Patterns` (`47:2`)، الشاشات الست، الـVariables والـStyles المحلية، وأسماء الـinstances المرتبطة بكل شاشة.

### 1.2 وثائق المستودع المقروءة

- `docs/review/phase4.2/README.md`
- `docs/review/phase4.3/README.md`
- `.design-system-state-yemen-gov-foundations-v1.json`
- `docs/architecture/UNIFIED_GOVERNMENT_PORTAL_IA_V1.md`
- ADRs الخاصة بحدود الجهة، URL/identity، والمحتوى الحكومي الموحد.
- سجلات Phase 1 وPhase 2 ذات الصلة بالجهات والمحتوى وواجهات التوافق.

### 1.3 قاعدة تفسير البيانات

هذه الوثيقة تفرق صراحةً بين:

- **بيانات متاحة فعليًا:** لها schema وendpoint وتشغيل حالي يمكن التحقق منه.
- **بيانات prototype:** موجودة في Java lists أو `site-data.ts` أو `/api/portal/home` hardcoded ولا تُعد مصدرًا حكوميًا موثوقًا.
- **بيانات Figma توضيحية:** تحمل `مثال` أو `توضيحي` أو يقرر تقرير Phase 4 أنها presentation-only.
- **بيانات مفقودة:** لا يوجد لها domain model أو API حالي.

لا يجوز نقل الفئتين الثانية والثالثة إلى production data أو seed رسمي.

## 2. Current frontend assessment

### 2.1 البنية والتقنيات

| جانب | الوضع الحالي | التقييم |
|---|---|---|
| Framework | Next.js App Router `^16.3.2`، React `^19`، TypeScript strict | أساس صالح لإعادة الاستخدام |
| Rendering | الصفحة الرئيسية Server Component؛ Header وAdmin والنماذج Client Components عند الحاجة | صالح، لكن يلزم فصل أوضح بين composition وinteractive islands |
| Root locale | `<html lang="ar" dir="rtl">` | **REUSE** كأساس RTL-first |
| Shared UI | `Header`, `Footer`, `Hero`, `SectionHeading` فقط | قليل جدًا مقارنةً بـ31 Component و9 Patterns |
| Styling | ملف عالمي واحد `globals.css` بحوالي 645 سطرًا + 111 استخدامًا لـinline styles | يحتاج refactor تدريجي لا rewrite شامل |
| Data client | `frontend/src/lib/api.ts`، `fetch`, `cache: no-store`، timeout 1.5s server/10s client | **REUSE/MODIFY**؛ يلزم عقود typed وحالات section-level |
| Fallbacks | `frontend/src/lib/site-data.ts` ومحتوى ثابت واسع | خطر تسرب prototype إلى production |
| Frontend tests | لا توجد ملفات test/spec/story؛ CI ينفذ lint/build فقط | مفقود |
| Visual regression | لا توجد Playwright/Chromatic/Storybook configuration | مفقود |
| Route states | لا توجد `loading.tsx` أو `error.tsx` أو `not-found.tsx` مخصصة | مفقود |

### 2.2 المسارات الحالية

المسارات العامة الموجودة:

- `/`
- `/about`
- `/services`
- `/contact`
- `/complaints`
- `/news/[id]`
- `/announcements/[id]`
- `/decisions/[id]`
- `/documents/[id]`
- `/login`
- `/admin`

المسارات المطلوبة للشاشات المعتمدة وغير الموجودة:

- `/ministries/[slug]`
- `/services/[serviceSlug]`

كما لا توجد route حقيقية لـ`/search` أو دليل الجهات `/government` أو قوائم المحتوى الوطنية الجديدة.

### 2.3 تقييم الـlayout والمكونات الحالية

- `layout.tsx` صالح كأساس، لكن metadata ما زالت تحمل هوية رئاسة مجلس الوزراء، لا هوية المنصة الوطنية.
- `Header.tsx` يعرض علامة PMO وتنقلًا قديمًا؛ بحثه يرسل إلى `/#search-results`، وقائمة الجوال مبنية بـ`<details>` دون focus trap أو Escape أو scroll lock أو استعادة focus.
- `Footer.tsx` PMO-specific ويغطي جزءًا صغيرًا فقط من Government Footer المعتمد.
- `Hero.tsx` يعرض hero إحصائيًا ثنائي العمود، بينما Figma يعتمد Search-led Hero وCivic Blue.
- `SectionHeading.tsx` قابل لإعادة الاستخدام بعد إزالة eyebrow الثابت وضبط API المحتوى.
- الصفحة الرئيسية الحالية تتكون من أقسام PMO أكثر عددًا واختلافًا عن IA المعتمدة، وتكرر أشكال cards/pills.
- `/services` قائمة ثابتة غير قابلة للنقر إلى تفاصيل ولا ترتبط بالـbackend.
- صفحات التفاصيل الحالية للأخبار والإعلانات والقرارات والوثائق بسيطة ويمكن إبقاؤها خارج نطاق الصفحات الثلاث مع adapters مؤقتة.
- Admin الحالي خارج نطاق الشاشات الست؛ لا ينبغي إدخاله في refactor البصري لهذه المرحلة.

### 2.4 تقييم نظام التصميم الحالي في الكود

| موضوع | الكود الحالي | عقد Figma | القرار |
|---|---|---|---|
| Primary action | أحمر `#B21F2D` وgradients | Civic Blue؛ الأحمر وطني محدود | **MODIFY** |
| Typography | Tahoma / Segoe UI / Arial | Noto Sans Arabic؛ Inter للـLTR | **MODIFY** |
| Container | `1180px` | nodes معتمدة `1160px`؛ Foundation `container/max=1200px` | **MODIFY** بقيمة مشتقة موثقة |
| Breakpoints | `980px`, `520px` | `768px`, `1200px`, `1440px` | **MODIFY** |
| Touch target | 44px غالبًا | 48px mobile، 44px desktop | **MODIFY** حسب token |
| Focus | outline عام جيد مبدئيًا | semantic focus color، 3px ring و3px offset | **REUSE/MODIFY** |
| Motion | reduced-motion موجود | 9 motion tokens و0ms reduced | **REUSE/MODIFY** |
| Radius/shadow | ad hoc وكثيف نسبيًا | restrained radii/elevation | **MODIFY** |
| Logical properties | خليط من physical وRTL-specific values | RTL/LTR منطقي | **MODIFY** |

### 2.5 تقييم تكامل البيانات الحالي

- `api.ts` يجمع news/announcements/decisions/documents و`/api/portal/home` وطلبات الدعم والإدارة.
- بحث الصفحة الرئيسية الحالي filter على العناصر الخمسة التي حُمّلت للصفحة؛ ليس Unified Search ولا يبحث في الجهات أو الخدمات أو البيانات.
- `portalHomeFallback` و`site-data.ts` مصدر presentation fallback، وليس عقد بيانات وطنيًا.
- timeout الخادم 1.5 ثانية قد يحول أي بطء عابر إلى fallback غير موثوق؛ يجب ألا يؤدي ذلك إلى عرض حقائق وهمية.
- `cache: no-store` على كل public read يحافظ على freshness لكنه يمنع الاستفادة من Next caching/revalidation؛ قرار caching يجب أن يكون حسب نوع البيانات لا global.

## 3. Figma contract inventory

### 3.1 Foundations

- 8 Variable Collections.
- 191 Variables:
  - Color Primitives: 50
  - Color Semantic: 45
  - Spacing: 14
  - Radius: 6
  - Layout: 18
  - Motion: 9
  - Typography: 33
  - Data Visualization: 16
- 15 Text Styles.
- 3 Elevation Styles.
- 3 Grid Styles: Desktop 12، Tablet 8، Mobile 4.

القيم الحاكمة للتنفيذ:

- `action/primary/bg-default` → Civic Blue 700 `#005A96`.
- `national/accent/default` → National Red 700 `#8D1B2D`.
- `text/primary` → Neutral 900 `#161B22`.
- `border/focus` → Civic Blue 500 `#167FC4`.
- Mobile margin/gutter: `16px`.
- Tablet margin `32px`، gutter `24px`.
- Desktop margin `40px`، gutter `24px`.
- Breakpoints: `320`, `768`, `1200`, `1440`.
- Touch targets: mobile `48px`، desktop `44px`.
- Motion: `120/200/300/500ms`، reduced motion `0ms`.
- Arabic family: `Noto Sans Arabic`.

### 3.2 Components

تم التحقق من 31 Component Set و184 variants. الحالات الأساسية المستخدمة في المكتبة هي Default، Hover، Focus، Active، Disabled، Loading، Error حيث تنطبق، إضافة إلى axes وظيفية مثل Tone/Size/Checked/Selected/On/Device.

### 3.3 Patterns

تم التحقق من 9 Pattern sets، ولكل منها Desktop وMobile:

1. Global Government Header
2. Government Search & Discovery
3. Service Discovery
4. Government Entity Identity
5. Content Discovery
6. Open Government & Data
7. Citizen Engagement
8. Common Page Framework
9. Government Footer

## 4. Figma Foundations → code mapping

| Foundation | الموجود | التصنيف | عقد التنفيذ |
|---|---|---|---|
| Color primitives | متغيرات قليلة ad hoc | **MODIFY** | إضافة CSS custom properties بأسماء Figma code syntax؛ components تستخدم semantic aliases فقط |
| Semantic colors | غير ممثلة بالكامل | **CREATE** | طبقة `tokens.css` مع Light mode المعتمد؛ لا dark mode مخترع |
| Spacing | أرقام متفرقة | **CREATE** | 14 spacing variables حرفيًا؛ يمنع hardcoded spacing الجديد إلا لسبب موثق |
| Radius | قيم 10/12/16 متعددة | **CREATE** | 6 radius variables؛ اختيار semantic usage داخل component CSS |
| Layout/grid | container 1180 وbreakpoints قديمة | **MODIFY** | 4/8/12 columns، breakpoints 768/1200، mobile-first |
| Motion | transition عام + reduced motion | **REUSE/MODIFY** | ربط durations/easing بالـtokens؛ إلغاء motion غير الضروري عند preference |
| Typography | Tahoma/Segoe/Arial | **MODIFY** | تحميل Noto Sans Arabic بصورة موثوقة؛ Inter فقط داخل LTR utility contexts |
| Data visualization | لا charts في الصفحات الثلاث | **DO NOT IMPLEMENT / DESIGN-ONLY** | لا تنفذ palette/chart abstractions حتى يوجد use case وبيانات حقيقية |
| Text styles | headings/body ad hoc | **CREATE** | typography utility/component recipes مستندة إلى 15 style، لا نسخ أسماء Figma كعناصر HTML |
| Elevation styles | shadow واحد كثيف | **MODIFY** | 3 مستويات فقط؛ الاستخدام مقتصد وفق الشاشات |
| Grid styles | لا grid contract | **CREATE** | CSS grid utilities للـ4/8/12 columns أو page-level recipes |

### 4.1 قرار 1160px مقابل `container/max=1200`

فحص الشاشات أثبت أن Header وHero وكل section containers وFooter في Desktop بعرض `1160px`، بينما Foundation variable `layout/container/max` يساوي `1200px`. لا ننشئ primitive جديدًا ولا نعدّل Figma. عقد الكود المقترح:

```css
--yegov-composition-max: calc(
  var(--yegov-layout-container-max) -
  (2 * var(--yegov-spacing-spacing-lg))
);
```

حيث `spacing/lg = 20px`، فتكون النتيجة `1160px`. هذه قيمة composition مشتقة وموثقة، وليست Foundation جديدة. يجب إثباتها بالـvisual regression قبل تعميمها.

## 5. Component reuse matrix

التصنيف هنا متعلق بنطاق الشاشات الثلاث، وليس حكمًا على قيمة المكوّن في المنصة مستقبلًا.

| Figma component | الموجود في الكود | التصنيف | عقد التنفيذ |
|---|---|---|---|
| Button/Primary | `.button--primary` | **MODIFY** | React primitive أو polymorphic action؛ Civic Blue؛ loading name ثابت؛ لا disabled anchor |
| Button/Secondary | `.button--secondary` | **MODIFY** | surface/border semantic؛ link مقابل button حسب الفعل |
| Link | `next/link` + styles عامة | **MODIFY** | يبقى `next/link` للمسارات؛ external anchor واضح؛ focus و48px عند touch context |
| Icon Button/Primary | غير موجود | **CREATE** | `button` مع accessible name؛ 48/44px |
| Icon Button/Secondary | search summary قديم | **CREATE** | زر حقيقي للبحث/القائمة، لا `<summary>` كبديل عام |
| Input | native inputs inline | **MODIFY** | Field wrapper موحد للlabel/helper/error/loading |
| Search | form في Header وفلتر homepage | **CREATE** | SearchField واحد responsive؛ لا يدعي Unified Search قبل وجود العقد |
| Select | native select inline | **MODIFY** | native-first في V1؛ لا custom listbox بلا حاجة |
| Textarea | native inline | **MODIFY** | wrapper مشترك وحالات validation |
| Checkbox | غير مستخدم بالشاشات | **DO NOT IMPLEMENT / DESIGN-ONLY** | يؤجل حتى use case فعلي |
| Radio | غير مستخدم بالشاشات | **DO NOT IMPLEMENT / DESIGN-ONLY** | يؤجل |
| Switch | غير مستخدم بالشاشات | **DO NOT IMPLEMENT / DESIGN-ONLY** | يؤجل؛ لا يستخدم submit action |
| Tabs/Item | لا Tabs runtime | **DO NOT IMPLEMENT / DESIGN-ONLY** | Entity navigation روابط routes وليست tabs |
| Breadcrumb/Item | غير موجود كمكوّن | **CREATE** | `<nav aria-label>` + ordered list + `aria-current=page` |
| Pagination/Page Item | لا قوائم paginated في الصفحات الست | **DO NOT IMPLEMENT / DESIGN-ONLY** | يؤجل حتى list routes |
| Pagination/Directional Item | غير موجود | **DO NOT IMPLEMENT / DESIGN-ONLY** | يؤجل |
| Accordion | `<details>` في Header فقط | **CREATE** | Disclosure مستقل semantic؛ انظر القسم 10.7 |
| Alert/Notice | `.notice` | **MODIFY** | مكوّن رسمي responsive؛ `status` أو `alert` حسب الإلحاح |
| Badge/Status | `.pill` | **MODIFY** | غير تفاعلي؛ النص يحمل المعنى |
| Modal/Dialog | Admin overlay غير semantic | **DO NOT IMPLEMENT / DESIGN-ONLY** | خارج الصفحات الثلاث؛ لا تصلح Admin في هذه المرحلة |
| Tooltip | غير موجود | **DO NOT IMPLEMENT / DESIGN-ONLY** | لا تضف tooltip لمعلومة أساسية |
| Header/Brand | brand PMO | **MODIFY** | هوية وطنية مؤقتة؛ الشعار النهائي asset dependency |
| Header/Utility Item | لا primitive مستقل | **CREATE** | accessibility/language utility links |
| Navigation/Item | links حالية | **MODIFY** | active من pathname؛ `aria-current`; logical layout |
| Navigation/Menu Trigger | mobile `<summary>` | **CREATE** | button + expanded/controls + focus management |
| Footer/Legal Item | روابط قليلة | **CREATE** | legal links حقيقية أو غير معروضة؛ لا `#` placeholders |
| Footer/Section | markup ثابت | **MODIFY** | data-driven navigation/contact group |
| Service Card | generic info card | **CREATE** | anchor واحد كامل؛ لا nested actions؛ API data فقط |
| Government Entity Card | غير موجود | **CREATE** | canonicalPath من API؛ entity type/summary |
| News/Content Card | card/list-card متكررة | **CREATE** | عقد مركزي NEWS/ANNOUNCEMENT/DECISION/DOCUMENT |
| Data/Metric | stat قديم | **DO NOT IMPLEMENT / DESIGN-ONLY** | لا تعرض أرقامًا دون source/update/unit حقيقية |

## 6. Pattern → code mapping

| Figma pattern | mapping في الكود | التصنيف | النطاق |
|---|---|---|---|
| Global Government Header | `Header.tsx` | **MODIFY** | مشترك في الصفحات العامة الثلاث |
| Government Search & Discovery | أجزاء داخل Header/Home | **CREATE** | Hero search + results route مستقبلًا؛ لا fake results |
| Service Discovery | cards/pills ثابتة | **CREATE** | composition مشترك للـHomepage والدليل مستقبلًا |
| Government Entity Identity | غير موجود | **CREATE** | Ministry page؛ يستقبل GovernmentEntity view model |
| Content Discovery | loops مختلفة في Home | **CREATE** | Section + ContentCard موحدان |
| Open Government & Data | نص ثابت فقط | **CREATE** كـcomposition بلا أرقام | روابط حقيقية فقط؛ البيانات نفسها **HOLD** |
| Citizen Engagement | `/complaints` + static copy | **MODIFY** | homepage entry links؛ لا tracking غير موجود |
| Common Page Framework | `SectionHeading` جزئي | **CREATE** | Container/Breadcrumbs/PageHeader/AsyncSection |
| Government Footer | `Footer.tsx` | **MODIFY** | Footer وطني مشترك؛ placeholders لا تصبح روابط مزيفة |

## 7. Shared implementation architecture

يجب منع نسخ Header/Footer/cards/async states بين الصفحات. البنية المقترحة عند التنفيذ:

```text
frontend/src/
  app/
    (public)/
      layout.tsx
      page.tsx
      ministries/[slug]/page.tsx
      services/[serviceSlug]/page.tsx
  components/
    foundations/
      Container.tsx
      Section.tsx
      Stack.tsx
    actions/
      Button.tsx
      AppLink.tsx
      IconButton.tsx
    feedback/
      Alert.tsx
      AsyncSection.tsx
      EmptyState.tsx
      Skeleton.tsx
    navigation/
      Breadcrumbs.tsx
      MobileNavigation.tsx
    shell/
      GovernmentHeader.tsx
      GovernmentFooter.tsx
    cards/
      ServiceCard.tsx
      GovernmentEntityCard.tsx
      ContentCard.tsx
    patterns/
      GovernmentSearch.tsx
      ServiceDiscovery.tsx
      EntityIdentity.tsx
      ContentDiscovery.tsx
  lib/
    api/
      client.ts
      entities.ts
      content.ts
      services.ts   # لا ينشأ قبل وجود عقد backend حقيقي
    view-models/
    fixtures/       # test/review only؛ لا يستوردها production app
  styles/
    tokens.css
    base.css
    components.css أو CSS Modules حسب القرار المعتمد
```

هذا تصور تنظيمي، وليس إذنًا بإنشاء الملفات الآن.

## 8. Route/page mapping

| الشاشة | route | الموجود | القرار |
|---|---|---|---|
| Homepage | `/` | موجودة | **MODIFY** تدريجيًا بعد جاهزية search/content decisions |
| Ministry | `/ministries/[slug]` | غير موجودة | **CREATE**؛ البيانات الأساسية جزئية فقط |
| Service Detail | `/services/[serviceSlug]` | غير موجودة | **CREATE بعد API**؛ حاليًا **HOLD** |

### 8.1 Homepage structure

الترتيب المعتمد حرفيًا:

1. Global Government Header
2. Search Hero
3. Services Discovery
4. Government Entities
5. Recent Official Content
6. Open Government and Engagement
7. Government Footer

المكونات المشتركة: Header، Footer، Container، SectionHeading، Search، Link، Button، ServiceCard، GovernmentEntityCard، ContentCard، Badge، Alert/AsyncSection.

### 8.2 Ministry structure

الترتيب المعتمد:

1. Global Government Header
2. Entity Identity + Breadcrumbs + entity navigation + subordinate entities
3. Key Services
4. News and Announcements
5. Institutional Resources
6. Contact and Source
7. Government Footer

يجب أن تكون الصفحة template واحدة لكل `entityType/pathSegment + slug`. لا ينشأ component أو route خاص بوزارة الصحة.

### 8.3 Service Detail structure

الترتيب المعتمد:

1. Global Government Header
2. Breadcrumbs + Service Hero + Primary CTA
3. Key Facts
4. Eligibility and Requirements
5. Steps
6. Related Services
7. Support and Metadata
8. Government Footer

لا يجوز إظهار Primary CTA حقيقي دون `startUrl` أو قناة تقديم موثقة من API. عند غيابه تعرض حالة واضحة غير تفاعلية، لا زرًا وهميًا.

## 9. Data/API mapping

### 9.1 تحقق تشغيلي read-only

في الفحص المحلي:

- backend health أعاد HTTP `200`.
- `GET /api/v1/entities` أعاد جهة واحدة فقط: رئاسة مجلس الوزراء اليمني.
- `GET /api/v1/content` أعاد `404` لأن public unified-content read controller ما زال خلف feature flag غير مفعّل.
- لم يتم تغيير أي flag أثناء الفحص.

### 9.2 Global shell وHomepage

| جزء الواجهة | المصدر الحقيقي الحالي | الفجوة/القرار |
|---|---|---|
| Platform brand | لا API؛ قيمة product configuration | يسمح config موثق، لا PMO branding hardcoded |
| Main navigation | IA V1 معتمدة | static route config مقبول إذا كانت routes الموجودة فقط قابلة للنقر |
| Search Hero | لا Unified Search API ولا `/search` route | **MISSING — HOLD**؛ لا تعرض بحثًا يوحي بوظيفة غير موجودة |
| Quick services | لا service domain/API | **MISSING**؛ كل بطاقات Figma أمثلة فقط |
| Browse by need | لا service categories API | **MISSING** |
| Government entities | `GET /api/v1/entities` | **AVAILABLE**؛ البيانات الحالية PMO فقط |
| Homepage ministry card | لا Health entity في البيانات الحالية | Figma presentation-only؛ لا seed تلقائي |
| Recent official content | legacy compatibility endpoints موجودة؛ unified API خلف flag | يمكن القراءة من facade الحالي، لكن السجلات static/prototype وليست facts production-approved |
| Open government | لا datasets/projects/reports domain/API | **MISSING**؛ روابط فقط عند وجود destination حقيقي |
| Citizen engagement | `POST /api/support/requests` و`/complaints` UI | متاح للإرسال الأساسي؛ لا tracking code/routing/SLA عام |
| Portal composition | `GET /api/portal/home` | hardcoded PMO-oriented؛ **لا يُعتمد** كعقد Homepage الوطني النهائي |

### 9.3 Ministry

| قسم | المصدر | الحالة |
|---|---|---|
| Identity/basic overview | `GET /api/v1/entities/by-slug/{type}/{slug}` | **AVAILABLE جزئيًا**: الاسم، النوع، الوصف، websiteUrl، parent |
| Subordinate entities | `GET /api/v1/entities/{id}/children` | **AVAILABLE** للعلاقة parent المباشرة فقط |
| Entity services | لا API | **MISSING** |
| Entity news/content | `GET /api/v1/entities/{id}/content` | موجود في الكود لكن خلف unified read flag ويعيد 404 حاليًا |
| Leadership | لا schema/API | **MISSING** |
| Decisions/documents | unified content model يدعمها نظريًا | public API غير متاح حاليًا؛ legacy endpoints غير entity-filtered |
| Data | لا domain/API | **MISSING** |
| Contact | `websiteUrl` فقط | البريد/الهاتف/العنوان/الساعات **MISSING** |
| Ministry of Health record | غير موجود في DB/API الحالي | لا تستخدم بيانات Figma كـproduction entity |

### 9.4 Service Detail

لا يوجد حاليًا `GovernmentService` domain أو tables أو repository أو public controller أو frontend client. جميع الحقول التالية مفقودة:

- service slug/name/summary
- owning entity
- beneficiary/eligibility
- delivery mode
- requirements/documents
- fees
- processing time
- ordered steps
- `startUrl`
- support channel
- related services
- authoritative source/update timestamp

بالتالي شاشة Service Detail كلها **DESIGN-ONLY** إلى أن يعتمد عقد backend منفصل ويُنفذ ويُملأ ببيانات جهة معتمدة. هذه الوثيقة لا تخترع endpoint أو DTO بديلًا.

### 9.5 المحتوى الذي لا يجوز تحويله إلى production data

- كل ما يحمل `مثال` أو `توضيحي` في الشاشات الست.
- service cards والرسوم والمدة والأهلية والمتطلبات والخطوات في Service Detail.
- subordinate entities والخدمات والأخبار وبيانات الاتصال التوضيحية في Ministry.
- الأرقام والمؤشرات والقنوات الرسمية في `PortalHomeController`.
- القوائم الثابتة في `NewsService`, `AnnouncementService`, `DecisionService`, `DocumentService` ما لم تمر بحوكمة محتوى ومصدر رسمي.
- كل fallback داخل `site-data.ts`.

## 10. Accessibility and runtime behavior contract

### 10.1 قاعدة الحالات التفاعلية

| الحالة | السلوك الملزم |
|---|---|
| Default | دلالة العنصر واضحة دون الاعتماد على اللون أو الأيقونة وحدهما |
| Hover | pointer فقط؛ لا يخفي معلومات ولا يكون الوسيلة الوحيدة لاكتشاف الفعل |
| Focus | `:focus-visible` semantic ring 3px + offset 3px؛ لا يزال مرئيًا فوق كل surfaces |
| Active | press/selected/current منفصلة عن focus؛ `aria-current` للوجهة الحالية |
| Disabled | native `disabled` للأزرار والحقول؛ الروابط غير المتاحة لا تُعرض كرابط disabled |
| Error | رسالة مرتبطة بـ`aria-describedby` و`aria-invalid`; summary عند تعدد أخطاء form |
| Loading | يمنع التكرار؛ يحتفظ accessible name؛ يعلن status مرة دون live-region spam |

### 10.2 Loading

- loading page shell لا يحجب Header/Footer.
- كل data-fed section لها skeleton مطابق تقريبًا للهيكل، `aria-hidden=true`.
- يوجد نص حالة visually hidden مثل «جارٍ تحميل الخدمات» داخل `role=status` عند الحاجة.
- لا يتحول timeout إلى بيانات fallback حكومية وهمية.
- Primary action لا ينتقل إلى loading إلا بعد فعل المستخدم.

### 10.3 Empty

- empty حالة ناجحة بلا records، وليست error.
- تعرض عنوانًا وشرحًا مختصرًا وفعلًا حقيقيًا فقط إن وجد route صالح.
- Homepage يمكنه إخفاء section غير متاح بعد اتفاق product/content؛ لا يملأها بأمثلة.
- صفحات الدليل/الجهة تعرض Empty State رسميًا كي لا يبدو endpoint معطلًا.

### 10.4 Error، offline وpartial data

- network failure و5xx يعرضان Alert/Notice داخل section مع retry scoped.
- 404 للوزارة أو الخدمة ينتقل إلى `not-found` حقيقي، لا generic error.
- Offline message لا يعتمد على `navigator.onLine` وحده؛ يؤكده فشل request.
- الصفحة تسمح بنجاح جزئي: فشل content لا يمنع identity أو shell.
- كل section تحمل حالة مستقلة: `idle/loading/success-empty/success-data/error`.
- لا تعرض بيانات قديمة دون label يوضح أنها cached ووقت تحديثها.

### 10.5 Keyboard وskip link

- أول عنصر focusable في document هو skip link إلى `#main-content`.
- ترتيب Tab يطابق ترتيب DOM والقراءة RTL، لا ترتيبًا بصريًا مصنوعًا بـCSS.
- card interactive = anchor واحد؛ لا nested buttons/links.
- Enter يفعل links، وEnter/Space يفعلان buttons/disclosures.
- لا roving tabindex في navigation العادية أو accordion.
- يجب اختبار zoom 200% و400% وإعادة تدفق المحتوى.

### 10.6 Mobile navigation وfocus management

- trigger هو `<button>` مع `aria-expanded` و`aria-controls`.
- عند الفتح ينتقل focus إلى أول عنصر منطقي أو عنوان drawer حسب implementation المختار.
- Escape يغلق ويعيد focus إلى trigger.
- click على overlay أو route selection يغلق؛ لا تغلق القائمة بسبب click عشوائي داخلها.
- يمنع background scrolling؛ يصبح المحتوى خلفها غير قابل للوصول (`inert` أو dialog semantics مناسبة).
- focus يبقى محصورًا إن كانت drawer modal.
- عند route navigation يوضع focus على `<h1 tabindex="-1">` أو main landmark بعد اكتمال الانتقال، مع تجنب announcements مزدوجة.

### 10.7 Accordion semantics وقرار Service Detail

القرار المقترح للتنفيذ:

- كل step disclosure مستقل؛ يسمح بفتح أكثر من step.
- trigger `<button>` داخل heading، مع `aria-expanded` و`aria-controls`، وregion يحمل `aria-labelledby` عند جدوى ذلك.
- focus يبقى على trigger بعد الفتح/الإغلاق.
- السهم decorative ويعكس expanded state فقط، وليس اتجاه RTL/LTR.
- runtime default: أول خطوة مفتوحة والباقي مغلق.
- لقطة Mobile المعتمدة تعرض نصوص الخطوات كاملة لأغراض المراجعة ولا تحدد state machine؛ visual test fixture يمكنه إجبار `all-open` دون تغيير runtime production default.
- المحتوى الضروري لبدء الخدمة لا يوضع حصريًا داخل step مغلق قبل Primary CTA؛ eligibility/requirements تبقى ظاهرة في أقسامها.

هذا القرار يحتاج اعتمادًا مع الخطة قبل التنفيذ لأنه يحسم سؤالًا تركه Phase 4.3 مفتوحًا.

### 10.8 RTL/LTR

- CSS uses logical properties: `margin-inline`, `padding-inline`, `inset-inline`, `border-inline`.
- root Arabic remains `dir=rtl`; English utility surfaces may use local `dir=ltr`.
- dynamic mixed text uses `<bdi>` أو `dir=auto` عند الحاجة.
- أرقام الهواتف، URLs، codes، file sizes والتواريخ اللاتينية تعزل عن bidi المحيط.
- Previous/Next وdirectional arrows تعكس دلاليًا؛ search/menu/status icons لا تُقلب آليًا.
- لا تستخدم `row-reverse` لتعويض DOM غير صحيح.

### 10.9 Extreme Arabic content

اختبارات إلزامية:

- عنوان card من 2–3 أسطر ثم 5 أسطر.
- اسم جهة بطول 80–120 حرفًا.
- عنوان H1 بطول 140 حرفًا.
- وصف 2–3 أضعاف النص المعتمد.
- كلمات/URLs غير قابلة للكسر.
- badge طويل، تاريخ طويل، metadata متعدد.
- 320px viewport، 200% text size، و400% zoom.

القاعدة: wrap افتراضيًا؛ لا ellipsis للمحتوى الحكومي الأساسي. يسمح line clamp فقط لقوائم discovery مع رابط إلى صفحة تكشف النص الكامل، وبقرار component موثق.

## 11. Responsive strategy

### 11.1 Breakpoints

- `320–767px`: Mobile، 4 columns، margin/gutter 16.
- `768–1199px`: Tablet، 8 columns، margin 32/gutter 24.
- `1200px+`: Desktop، 12 columns، gutter 24.
- `1440px+`: wide viewport مع composition max ثابت، لا تمديد cards بلا حد.

### 11.2 قواعد composition

- desktop composition max = 1160px مشتقة كما في القسم 4.1.
- mobile content width في Figma غالبًا 328px داخل frame 360px، أي margin 16px.
- grids تتحول 3→2→1 أو 2→1 حسب pattern؛ لا تعتمد فقط على `auto-fit` إذا غيّر ترتيب التصميم المعتمد.
- sections auto-height؛ لا تستخدم heights الثابتة من Figma في production إلا للحد الأدنى/asset ratio.
- header mobile وdesktop composition منفصلان فوق primitives مشتركة، لا CSS squeeze لنسخة desktop.

### 11.3 Tablet

لا توجد شاشة Tablet عالية الدقة معتمدة. يستخدم Grid/Tablet الرسمي وقواعد إعادة التدفق، ثم يعرض review screenshot مستقل قبل دمج التنفيذ. لا تُخترع زخارف أو IA جديدة للـTablet.

## 12. Implementation sequence

لا يبدأ أي slice دون اعتماد هذه الخطة والـgate السابق له.

### Slice 0 — Decisions and readiness

1. اعتماد HOLD/GO والـroute names.
2. اعتماد 1160px derived composition rule.
3. اعتماد Accordion runtime contract.
4. تحديد font delivery: self-hosted Noto Sans Arabic أو آلية build موثوقة.
5. اعتماد policy: production never imports design/test fixtures.
6. تحديد مصير Search Hero قبل Unified Search: hide/disabled explanatory state أو انتظار API؛ لا زر fake.
7. إغلاق data owners للـMinistry وService.

### Slice 1 — Foundations/tokens

- إضافة token layer بصورة additive.
- ربط semantic colors والtypography والspacing/radius/layout/motion.
- إبقاء legacy selectors مؤقتًا حتى ترحيل route واحدة في كل مرة.
- unit checks لقيم tokens وreduced motion.

### Slice 2 — Shared primitives

- Button، Link، IconButton، Field/Input/Search، Alert، Badge، Breadcrumb، Accordion.
- tests للحالات والـkeyboard والـARIA قبل استخدامها في pages.

### Slice 3 — Header/Footer

- GovernmentHeader وGovernmentFooter من patterns المعتمدة.
- mobile navigation behavior كامل.
- shared public layout دون تغيير Admin.

### Slice 4 — Shared cards/patterns

- ServiceCard، GovernmentEntityCard، ContentCard.
- Section/Container/AsyncSection وdiscovery compositions.
- لا fixture في production import graph.

### Slice 5 — Homepage

- ترحيل section-by-section خلف integration branch عادية، لا feature flag جديد بلا قرار معماري.
- استخدام data حقيقية فقط؛ الأقسام غير المدعومة تُعامل وفق policy المعتمد.
- Search لا يشحن كوظيفة زائفة.

### Slice 6 — Ministry

- إنشاء route template ديناميكي.
- يبدأ فقط بجهة موجودة في `/api/v1/entities`.
- sections التي لا يملك API بيانات لها تظهر empty/unsupported state معتمدًا أو تؤجل.

### Slice 7 — Service Detail

- **HOLD مستقل** حتى وجود GovernmentService public contract وrecord موثوق واحد على الأقل.
- لا يبنى ضد object محلي مستوحى من Figma.

### Slice 8 — Responsive/accessibility hardening

- 320/360/768/1024/1200/1440.
- keyboard، screen reader smoke، zoom/reflow، forced colors، reduced motion، bidi.

### Slice 9 — Visual regression and handoff validation

- مقارنة الشاشات الست.
- إغلاق الفروق المقصودة في سجل deviations.
- لا يعلن completion قبل موافقة review.

## 13. Testing strategy

### 13.1 Unit/component

- إضافة test runner بعد الاعتماد، وليس في هذه المرحلة.
- اختبار rendering والدلالة لكل state.
- اختبار links مقابل buttons ومنع nested interactive elements.
- اختبار view-model mappers بعقود API حقيقية.
- اختبار extreme Arabic strings وRTL/LTR.

### 13.2 Integration

- Mock Service Worker أو fetch mock محصور في tests لسيناريوهات:
  - success/data
  - success/empty
  - slow/loading
  - 404
  - 500
  - offline/network error
  - partial section failure
- contract fixtures مشتقة من DTOs الحالية فقط، لا facts حكومية مخترعة.
- production build graph check يمنع import من `fixtures/`.

### 13.3 End-to-end/accessibility

- Playwright Chromium على الأقل، ثم WebKit/Firefox smoke عند الاستقرار.
- `@axe-core/playwright` أو equivalent للـautomated checks، مع manual keyboard review.
- flows: skip link، menu open/close/Escape، search submit، entity navigation، accordion، CTA unavailable state، 404.
- CI الحالي يحتاج jobs جديدة؛ الموجود الآن lint/build فقط.

### 13.4 Backend contract tests المطلوبة قبل integration

- Entity by slug 200/404.
- Entity children empty/non-empty.
- Entity content feature availability دون تغيير flag من frontend.
- Service detail contract عندما يُنشأ معماريًا.
- Unified Search contract عندما يُنشأ معماريًا.

## 14. Visual regression strategy

### 14.1 Goldens

المصادر الحالية:

- `docs/review/phase4.3/after/*@2x.png`
- Figma node IDs الستة هي source of truth الهندسي.

### 14.2 Harness

- Playwright fixed Chromium version في CI.
- viewports: Desktop `1440px` وMobile `360px`؛ full-page.
- `deviceScaleFactor=2` لمقارنة 2× عند الإمكان.
- locale `ar-YE` أو locale ثابت مع `dir=rtl`، timezone ثابت، font assets محلية، animations disabled.
- network responses deterministic من review fixtures المعزولة، وموسومة صراحةً بأنها ليست production data.
- لا timestamps عشوائية ولا layout shifts بسبب font loading.

### 14.3 طبقات المقارنة

1. **Geometry:** frame width، section order، container widths، spacing landmarks.
2. **Component snapshots:** الحالات الأساسية والـresponsive widths.
3. **Full-page visual diff:** الشاشات الست.
4. **Semantic diff:** headings/landmarks/tab order/ARIA منفصل عن pixel diff.
5. **Content stress diff:** extreme Arabic لا يقارن بالنص المرجعي، بل بالoverflow/reflow constraints.

### 14.4 Mobile export normalization

الشاشات في Figma بعرض 360px، بينما ملفات Phase 4.3 Mobile الحالية بعرض 704px عند 2×، أي review render داخلي 352px. لذلك:

- viewport contract يبقى 360px.
- pixel comparison يطبق crop موثقًا ومتمركزًا بعرض 352px فقط إذا استُخدمت exports الحالية.
- اختبار منفصل يثبت عدم overflow داخل كامل 360px.
- البديل الأفضل هو direct re-export read-only من node IDs بعرض 360/720 دون تعديل frames، ثم اعتماد goldens الجديدة قبل التنفيذ.
- لا يسمح للـdiff tool بعمل stretch للصورة؛ stretch يخفي أخطاء geometry.

### 14.5 Thresholds والقبول

- الهدف الأساسي pixel-exact في grid/container/spacing/typography.
- يوضع threshold صغير ثابت فقط لاختلاف antialiasing، لا لتجاوزات layout.
- كل intentional deviation يسجل: node، سبب، owner، approval، screenshot before/after.
- فشل visual regression يمنع merge لroute المهاجرة، لكنه لا يمنع legacy routes غير الداخلة في النطاق.

## 15. Code Connect أو equivalent mapping

### 15.1 النتيجة

- لا توجد `.figma.ts/.figma.js` أو Code Connect configuration في المستودع.
- محاولة قراءة Code Connect map للصفحة `22:2` أعادت أن الميزة تتطلب Dev/Full seat على Organization أو Enterprise؛ الخطة الحالية للحساب لا تتيحها رغم توفر ملف Figma والوصول للـPlugin API.
- لم يُنشأ أو يُرسل أي mapping، ولم تبدأ مزامنة آلية.

### 15.2 البديل المؤقت

- هذه الوثيقة هي mapping contract المعتمد مؤقتًا.
- بعد إنشاء components الفعلية، يمكن إضافة manifest versioned مثل `docs/implementation/FIGMA_CODE_MAP.json` يربط `figmaNodeId` بمسار component واسم export وvariant props.
- لا يبدأ Code Connect إلا بعد توفر الخطة المناسبة، استقرار component API، ومراجعة بشرية للـmapping.
- لا ينبغي أن يحول غياب Code Connect دون تنفيذ يدوي منضبط؛ لكنه يمنع ادعاء sync آلي.

## 16. Risks, blockers, and gates

| الأولوية | الخطر/المانع | الأثر | بوابة الإغلاق |
|---|---|---|---|
| P0 | لا GovernmentService domain/API | Service Detail كلها بلا بيانات | عقد backend معتمد + record موثوق + tests |
| P0 | لا Unified Search API/route | أهم CTA في Homepage غير قابل للتنفيذ بصدق | قرار product أو API حقيقي |
| P0 | Health Ministry غير موجودة في entity registry | لا يمكن نشر الشاشة المعتمدة كصفحة حية | إدخال جهة معتمد عبر حوكمة البيانات، لا من Figma |
| P0 | prototype/static content قد يبدو رسميًا | نشر معلومات غير موثقة | content provenance policy + منع fixture imports |
| P1 | entity DTO محدود | Ministry sections كثيرة بلا بيانات | تحديد scope أولي أو توسيع domain في مرحلة منفصلة |
| P1 | unified content public API مغلق حاليًا | entity-filtered content غير متاح | قرار Phase 2 مستقل؛ Phase 5 لا يغير flags |
| P1 | لا frontend tests/visual harness | regressions عالية الاحتمال | اعتماد وإضافة test tooling قبل page slices |
| P1 | Header mobile الحالي غير modal-safe | keyboard/focus failure | primitive + E2E accessibility tests |
| P1 | 1160/1200 contract مشتق | احتمال pixel drift | اعتماد القاعدة واختبار geometry |
| P1 | mobile goldens 352-render داخل frame 360 | false diffs | normalization أو direct re-export معتمد |
| P2 | Code Connect غير متاح بالخطة الحالية | mapping يدوي قابل للانحراف | manifest versioned + مراجعة؛ upgrade لاحقًا |
| P2 | Tahoma في production مقابل Noto Figma | تغيّر metrics/height | قرار font delivery قبل visual baselines |
| P2 | `cache:no-store` وtimeouts عامة | flicker/partial failure | data-specific caching policy |
| P2 | 111 inline styles وملف CSS واحد | صعوبة صيانة وتعارض | refactor additive أثناء ترحيل components فقط |

## 17. Explicit GO / HOLD recommendation

### القرار

**HOLD — لا تبدأ تنفيذ Homepage وMinistry وService Detail كحزمة كاملة الآن.**

السبب ليس عدم جاهزية Figma؛ Phase 4.3 جاهزة للتسليم. السبب أن الكود والبيانات لا يملكان بعد عقودًا حقيقية لأهم وظيفتين في التجربة: Unified Search وGovernment Service، كما أن بيانات الوزارة المعتمدة بصريًا غير موجودة في السجل الحكومي الحالي. البدء الآن سيجبر التنفيذ على أحد خيارين غير مقبولين: اختراع APIs أو تحويل أمثلة Figma إلى حقائق production.

### GO محدود يمكن اعتماده لاحقًا

يمكن إعطاء **GO منفصل لـSlice 0 ثم Slice 1–4 فقط** بعد اعتماد هذه الوثيقة والقرارات التالية:

1. قبول 1160px composition rule المشتق.
2. قبول Accordion runtime contract.
3. اعتماد font delivery.
4. اعتماد سياسة عدم استيراد fixtures في production.
5. حسم behavior المؤقت للبحث قبل API.
6. تثبيت أن Service Detail تبقى HOLD مستقلًا.

بعد ذلك:

- Homepage: **HOLD** حتى حسم البحث والأقسام بلا بيانات.
- Ministry: **Conditional GO** لجهة موجودة فعليًا وبالأقسام المدعومة فقط.
- Service Detail: **HOLD** حتى وجود domain/API/record حقيقي.

## 18. Non-goals and change boundary

لم تنفذ هذه المرحلة أيًا من التالي:

- production UI أو components فعلية
- CSS أو route changes
- backend/API/schema/migration changes
- feature flag changes
- database writes أو seeds
- Figma edits
- Code Connect mapping writes
- deployment
- commit أو push

الخطوة التالية بعد مراجعة المالك هي اعتماد أو تعديل التوصية والبوابات فقط. لا يبدأ التنفيذ تلقائيًا.

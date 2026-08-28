# Phase 5 — Public Routing + Data Readiness Check

> **تاريخ الفحص:** 2026-08-28
>
> **نقطة الأساس:** `main` عند `9f5a05a8c23680c98e804fc67bce5626a9e38154`
>
> **النطاق:** فحص read-only للمسارات العامة وحدود الـlayout وجاهزية بيانات الشاشات المعتمدة
>
> **القرار:** **GO WITH CONDITIONS** لدفعة Routing معزولة فقط؛ **HOLD** لتنفيذ Homepage كاملة أو Ministry أو Service Detail أو Unified Search.

## 1. حدود الفحص ومصادر الأدلة

لم يغيّر هذا الفحص production code أو routes أو backend أو database أو feature flags أو Figma. لم يبدأ Batch 5 ولم يُنفذ commit أو push.

مصادر الأدلة الأساسية:

- `frontend/src/app/**`
- `frontend/src/components/layout/**`
- `frontend/src/components/navigation/**`
- `frontend/src/components/shell/**`
- `frontend/src/components/content/**`
- `frontend/src/lib/api.ts`
- `frontend/src/lib/site-data.ts`
- Controllers وDTOs وmigrations في `backend/**`
- `docker-compose.yml` و`render.yaml` و`netlify.toml`
- `docs/review/phase4.3/README.md` ولقطة Homepage المعتمدة
- وثائق Phase 5 السابقة وسجل ANNOUNCEMENT Canary النهائي

فحص التشغيل المحلي read-only أكد:

- Backend: `UP` على `http://localhost:8081`.
- PostgreSQL: healthy.
- Frontend: يعمل على `http://localhost:3000`.
- `government_entities`: سجل واحد.
- `content_items`: 12 سجلًا منشورًا؛ 3 لكل من NEWS وANNOUNCEMENT وDECISION وDOCUMENT.
- `content_attachments`: صفر.
- لا يحتوي المستودع عنوان staging نهائيًا مثبتًا أو قيم Netlify/Render الفعلية؛ لذلك لا تُستنتج جاهزية staging من ملفات IaC وحدها.

## 2. Current routing map

لا توجد Route Groups أو nested layouts حاليًا. `frontend/src/app/layout.tsx` هو layout وحيد يضبط `lang="ar"` و`dir="rtl"` والخط وCSS والmetadata، ثم يعرض `children` مباشرة.

| Route | الفئة | rendering/data | shell الحالي | الملاحظة |
|---|---|---|---|---|
| `/` | Public | Server؛ portal home + أربع قوائم + local fallbacks/search | `Header`/`Footer` القديمان يدويًا | يحتاج نقلًا ذريًا إلى public boundary؛ لا يُعاد تصميمه في دفعة routing |
| `/about` | Public | Static؛ نصوص داخل الصفحة | `Header`/`Footer` القديمان يدويًا | المحتوى لم يخضع للتحقق التحريري |
| `/services` | Public prototype | Static؛ خدمات hardcoded | `Header`/`Footer` القديمان يدويًا | ليس Service Directory production |
| `/complaints` | Public | Client؛ `POST /api/support/requests` | `Header`/`Footer` القديمان يدويًا | المسار الوظيفي الوحيد المرتبط بخدمة citizen engagement حقيقية |
| `/contact` | Public prototype | Static؛ بيانات اتصال مثال وform بلا submit | `Header`/`Footer` القديمان يدويًا | لا يجوز إبقاء بيانات المثال كحقائق رسمية |
| `/news/[id]` | Public | Server؛ API + fallback من `site-data.ts` | بلا Header/Footer | shell غير متسق، وfallback غير صالح production |
| `/announcements/[id]` | Public | Server؛ API + fallback من `site-data.ts` | بلا Header/Footer | shell غير متسق، وfallback غير صالح production |
| `/decisions/[id]` | Public | Server؛ API + fallback من `site-data.ts` | بلا Header/Footer | shell غير متسق، وfallback غير صالح production |
| `/documents/[id]` | Public | Server؛ API + fallback من `site-data.ts` | بلا Header/Footer | shell غير متسق ولا يوجد attachment/download حقيقي |
| `/login` | Auth | Client؛ `POST /api/auth/login` | بلا public shell | يجب أن يبقى خارج public boundary |
| `/admin` | Admin | Client؛ admin/support APIs | `Header`/`Footer` القديمان يدويًا | يجب ألا يرث Government Public Shell |

مسارات غير موجودة:

- لا يوجد Ministry أو Government Entity page route.
- لا يوجد Service Detail route.
- لا يوجد Search results route مستقل.
- لا توجد `loading.tsx` أو `error.tsx` عامة/مخصصة، ولا public `not-found.tsx`.

## 3. Proposed public layout boundary

### القرار المعماري

إنشاء Route Group واحد مستقبلاً باسم `app/(public)` هو أقل تغيير آمن يحقق العزل المطلوب ويحفظ عناوين URL. Route Groups لا تدخل في URL، لذلك تبقى `/about` و`/services` والمسارات الديناميكية كما هي.

الحد المقترح:

```text
app/
├── layout.tsx                  # document only: html/body/font/globals
├── (public)/
│   ├── layout.tsx             # SkipLink + GovernmentHeader + main + GovernmentFooter
│   ├── page.tsx               # /
│   ├── about/page.tsx
│   ├── services/page.tsx
│   ├── complaints/page.tsx
│   ├── contact/page.tsx
│   ├── news/[id]/page.tsx
│   ├── announcements/[id]/page.tsx
│   ├── decisions/[id]/page.tsx
│   └── documents/[id]/page.tsx
├── admin/page.tsx              # خارج public group
└── login/page.tsx              # خارج public group
```

### لماذا Route Group مطلوب هنا؟

- يركّب Public Shell مرة واحدة من دون pathname branching داخل root layout.
- يمنع تحميل/توريث mobile navigation وpublic footer في `/admin` و`/login`.
- يحفظ URLs والروابط الحالية بلا redirects.
- يجعل Skip Link يشير إلى `main` حقيقي موحد في كل صفحة عامة.
- يتيح لاحقًا إضافة public loading/error/not-found boundaries دون التأثير على الإدارة والمصادقة.

### ما غير المطلوب الآن؟

- لا حاجة إلى `(admin)` أو `(auth)` Route Groups في الدفعة التالية؛ يوجد route واحد لكل منهما ولا توجد layouts مشتركة تبرر النقل.
- لا حاجة إلى conditional shell مبني على `usePathname` في root layout؛ هذا يضيف client boundary وهدرًا ومخاطر hydration إلى كل التطبيق.
- لا حاجة إلى wrapper يدوي في كل صفحة؛ سيعيد التكرار الذي بُني Public Shell لإزالته.

### عقد `main` وSkip Link

يجب أن يمتلك `(public)/layout.tsx` عنصر `<main id="main-content" tabIndex={-1}>` الوحيد. عند النقل:

- تزال `Header` و`Footer` القديمتان من الصفحات العامة فقط.
- تتحول عناصر `<main>` الداخلية إلى fragment أو `Section`/`PageContainer` غير دلالي كـmain.
- لا يُنشأ أكثر من `<main>` واحد في الصفحة.
- يبقى `/admin` كما هو مؤقتًا؛ لا تُحذف `Header.tsx` أو `Footer.tsx` ما دام admin يعتمد عليهما.

## 4. Migration impact and safety

| الأثر | الحجم | الضابط المطلوب |
|---|---:|---|
| نقل 9 page entries إلى `(public)` | متوسط، path-preserving | تنفيذ ذري والتحقق من route manifest قبل/بعد |
| إزالة shell اليدوي من 5 صفحات | منخفض | عدم تغيير محتوى الصفحة أو data fetching |
| إضافة shell إلى 4 detail routes | منخفض وإيجابي | فحص heading/main/skip-link وعدم ازدواج landmarks |
| `/admin` و`/login` | لا تغيير مقصود | route regression tests تثبت عدم وجود public shell |
| CSS/visuals للصفحات القديمة | لا تغيير مقصود | لا تبدأ Homepage migration أو inline-style refactor ضمن دفعة routing |
| metadata | لا تغيير ضمن الدفعة | يبقى root metadata إلى قرار content/brand مستقل |

قبول دفعة routing المستقبلية يتطلب:

1. جميع URLs الحالية تعيد الحالة نفسها قبل/بعد النقل.
2. Government Public Shell يظهر على public routes فقط.
3. `/admin` و`/login` لا يحتويان GovernmentHeader/GovernmentFooter.
4. main landmark واحد وSkip Link يعمل في كل public route.
5. Mobile navigation keyboard/focus/scroll-lock behavior يبقى ناجحًا.
6. lint وTypeScript وbuild وroute inventory و320px overflow checks تنجح.

## 5. Approved Homepage section/data matrix

ترتيب الشاشة المعتمدة: Public Header → Search/Hero → Quick Services + needs taxonomy → Government Entities → Latest Official Updates → Open Government & Participation → Government Footer.

| القسم المعتمد | UI الموجود القابل لإعادة الاستخدام | مصدر البيانات الحقيقي المتاح | الفجوة/المحتوى الممنوع | الجاهزية |
|---|---|---|---|---|
| Public Header/Nav | `GovernmentHeader`, `PrimaryNavigation`, `MobileNavigation`, `SkipLink` | routes الحالية فقط | العلامة الحالية تطويرية وليست شعارًا حكوميًا معتمدًا؛ لا تُخترع جهة/رابط | **READY WITH CONDITIONS** |
| Search/Hero | `SearchField`, layout primitives | لا Search API ولا route contract | البحث الحالي في `/` يفلتر news/announcement/decision/document محليًا ويوحي ببحث أوسع؛ لا يجوز اعتباره Unified Search | **HOLD** |
| Quick Government Services | `ServiceCard`, `CardGrid`, content states | لا Government Service API | `PortalHomeController` و`site-data.ts` و`/services` تحتوي خدمات توضيحية؛ بطاقات Figma موسومة مثالًا | **HOLD** |
| Explore by need/category | primitives موجودة | لا service taxonomy أو need taxonomy | فئات الهوية/التعليم/الصحة وغيرها في Figma ليست production taxonomy | **HOLD** |
| Government Entities | `GovernmentEntityCard` | `GET /api/v1/entities` وentity detail/slug/children؛ سجل PMO واحد | لا توجد أي وزارة في البيانات؛ بطاقتا Figma ليستا قابلتين للنشر كحقائق | **PARTIAL** |
| Latest Official Updates | `ContentCard`, `CardGrid`, `ContentState` | APIs الأربعة legacy-compatible؛ 12 published unified rows محليًا | النصوص الحالية seed/compatibility content غير مثبتة تحريريًا؛ لا صور أو attachments | **READY WITH CONDITIONS** تقنيًا، **HOLD** تحريريًا |
| Open Government/Data | layout/content compositions | لا dataset/report/catalog API أو routes موثقة | لا يجوز اختراع أرقام، تقارير أو روابط بيانات مفتوحة | **HOLD** |
| Citizen Participation | `ContentCard`/links + `/complaints` | `POST /api/support/requests` متاح | SLA والخصوصية وقنوات الاتصال الحالية غير موثقة؛ `/contact` form وهمي | **READY WITH CONDITIONS** لمسار الشكوى فقط |
| Government Footer | `GovernmentFooter` | روابط public الموجودة | لا أرقام/عناوين/سياسات غير منشورة؛ العلامة مؤقتة | **READY WITH CONDITIONS** |

### ملاحظات على Homepage الحالية

- تستدعي `/api/portal/home` والقوائم الأربع بالتوازي، ثم تخلط الاستجابات مع `site-data.ts` عند الفشل.
- تنفذ local search عبر query parameter `q` على أربعة أنواع محتوى فقط؛ هذا لا يطابق وعد البحث الحكومي المعتمد.
- تعرض خدمات وstats وقنوات وبيانات ومبادئ وmedia items من composition hardcoded في backend أو fallback محلي.
- `PortalHomeController` لا يبني Homepage من مصدر CMS موحد؛ يجمع ثلاث قوائم حقيقية الشكل مع كتل ثابتة.
- لا يجوز تحويل المطابقة البصرية إلى اعتماد بيانات؛ كل قسم يجب أن يملك source/state contract مستقلًا.

## 6. Backend public API inventory

### 6.1 Contracts and runtime evidence

| Capability | Public route(s) | response shape | Local 2026-08-28 | Staging | Production suitability |
|---|---|---|---|---|---|
| Health | `GET /actuator/health`, `GET /health` | actuator status / text health | `UP` | غير قابل للتحقق من URL مثبت في repo | تشغيلي فقط |
| Entity types | `GET /api/v1/entity-types` | `[{id, code, name, pathSegment}]` | 200؛ خمسة أنواع | غير متحقق | العقد صالح، لكن ليس content catalog |
| Entity registry | `GET /api/v1/entities` | `GovernmentEntityResponse[]` | 200؛ جهة واحدة | غير متحقق | **جزئي**؛ schema مناسب، coverage غير كافٍ |
| Entity detail | `GET /api/v1/entities/{uuid}` | entity + type + parent | controller موجود؛ public | غير متحقق | صالح بعد إدخال/اعتماد جهة حقيقية |
| Entity by slug | `GET /api/v1/entities/by-slug/{type}/{slug}` | entity + canonical path | controller موجود؛ public | غير متحقق | أفضل عقد route للصفحة المؤسسية، لكن لا Ministry data |
| Entity children | `GET /api/v1/entities/{uuid}/children` | entity list | controller موجود؛ public | غير متحقق | لا توجد علاقات/children مثبتة حاليًا |
| NEWS | `GET /api/news`, `GET /api/news/{numericId}` | `{id,title,category,date,excerpt}` | 200؛ 3 عناصر | غير متحقق | contract مستقر محليًا؛ المحتوى غير موثق للنشر الرسمي |
| ANNOUNCEMENT | `GET /api/announcements`, `GET /api/announcements/{numericId}` | `{id,title,category,date,excerpt}` | 200؛ 3 عناصر | غير متحقق | contract مستقر محليًا؛ المحتوى غير موثق للنشر الرسمي |
| DECISION | `GET /api/decisions`, `GET /api/decisions/{numericId}` | `{id,title,category,date,description}` | 200؛ 3 عناصر | غير متحقق | contract legacy حالي؛ لم يتخرج unified محليًا |
| DOCUMENT | `GET /api/documents`, `GET /api/documents/{numericId}` | `{id,title,category,updatedAt,description}` | 200؛ 3 عناصر | غير متحقق | لا attachment/download؛ ليس مكتبة وثائق مكتملة |
| Portal home | `GET /api/portal/home` | hero, metrics, channels, news, statements, decisions, services, documents, media, principles | 200 | غير متحقق | **غير صالح production بصيغته الحالية** بسبب hardcoded presentation facts |
| Unified content list | `GET /api/v1/content` + filters/paging | `PageResponse<PublicContentResponse>` | 404؛ public-read flag مغلق | غير متحقق | HOLD؛ لا يجوز تغيير flag ضمن Phase 5 UI |
| Unified content detail | `GET /api/v1/content/{uuid}`, `/by-slug/{type}/{slug}` | UUID content + entity/categories/body | controller conditional وغير متاح محليًا | غير متحقق | HOLD حتى سياسة public read مستقلة ومعتمدة |
| Entity content | `GET /api/v1/entities/{entityId}/content` | paged unified content | controller conditional وغير متاح محليًا | غير متحقق | HOLD؛ تحتاجه Ministry updates/resources |
| Government services | لا route/controller | — | لا عقد؛ `/api/services` يقع في catch-all الأمني ويعيد 401، لا API خدمة | غير متحقق | **HOLD** |
| Unified search | لا route/controller | — | لا عقد؛ `/api/search` يقع في catch-all الأمني ويعيد 401، لا Search API | غير متحقق | **HOLD** |
| Citizen request | `POST /api/support/requests` | submitted request metadata | public controller موجود | غير متحقق | قابل للاستخدام بعد اعتماد privacy/SLA/content policy |

مهم: 401 على `/api/services` و`/api/search` لا يعني أن endpoint موجود؛ لا يوجد controller لهما، وSpring Security يعترض المسار غير المعروف قبل الوصول إلى 404.

### 6.2 Current compatibility state

وفق آخر Exit Review موثق:

- NEWS: `UNIFIED` محليًا.
- ANNOUNCEMENT: `UNIFIED` محليًا في آخر دليل تشغيلي تمت مراجعته.
- DECISION: `LEGACY`.
- DOCUMENT: `LEGACY`.
- هذا وضع Docker محلي، وليس اعتماد staging أو production أو write cutover.

هذا الفحص لا يعيد تقرير التخرج ولا يحسم حالته الإدارية؛ يسجل فقط مصدر القراءة الفعلي اللازم لتخطيط الواجهة.

ينبغي للواجهة الاستمرار في استهلاك عقود `/api/news|announcements|decisions|documents` المتوافقة بدل الربط المباشر بـ`/api/v1/content` قبل قرار public-read منفصل.

## 7. Data classification

### 7.1 Real contracts/storage available now

- Entity schema وpublic entity routes حقيقية ومتصلة بقاعدة البيانات.
- يوجد سجل PMO واحد active فقط؛ لا توجد Ministry records.
- Unified storage يحتوي 12 published items و12 revisions، لكنه backfill من المصادر الحالية وليس دليلًا على اعتماد المحتوى تحريريًا.
- Compatibility APIs الأربعة تعمل محليًا بعقود مستقرة.
- Support submission endpoint موجود ويكتب الطلبات فعليًا.

### 7.2 Missing data/contracts

- Government Service model/API/records/eligibility/steps/channels.
- Search index/query/ranking/filter/pagination contract.
- Ministry records والعلاقات والموارد والخدمات التابعة.
- Open-data datasets، reports، metrics provenance و`asOf` timestamps.
- Official contact values، service hours، SLA، privacy statements والسياسات القانونية المنشورة.
- Media assets وdocument attachments؛ جدول attachments حاليًا فارغ.

### 7.3 Illustrative or prohibited as production data

- كل exports في `frontend/src/lib/site-data.ts`.
- constants داخل `/about`, `/services`, `/contact` وأجزاء claims داخل `/complaints` ما لم تُعتمد من مالك المحتوى.
- Hero/stats/channels/service cards/services/statements/media/principles hardcoded في `PortalHomeController`.
- محتوى Figma الموسوم `مثال` أو `توضيحي` وأسماء الخدمات/الجهات/الحقائق غير المسندة.
- القوائم legacy Java والـ12 unified backfill rows لا تُعتبر حقائق حكومية منشورة لمجرد أنها تصل عبر API.
- لا تستخدم بيانات admin seed كpublic content؛ `admin_content` نموذج إدارة قديم منفصل عن public publication workflow.

## 8. Page readiness matrix

| Target | Routing/UI readiness | Data readiness | النتيجة | ما الذي يفك الحظر؟ |
|---|---|---|---|---|
| Homepage | Public Shell وprimitives/compositions جاهزة؛ route موجود | search/services/open-data غير موجودة؛ entities جهة واحدة؛ updates غير معتمدة تحريريًا | **PARTIAL** | تنفيذ public boundary أولًا، ثم عقود/مصادر معتمدة لكل section وسياسة صريحة لإخفاء الأقسام غير الجاهزة |
| Ministry | لا route؛ EntityCard موجود | entity API مناسب جزئيًا لكن لا Ministry row؛ entity-content public endpoint غير متاح؛ لا services/resources | **PARTIAL — final page HOLD** | اعتماد entity route contract، إدخال وزارة موثقة، تفعيل/توفير read contract آمن لمحتواها، وعدم اختراع services |
| Service Detail | لا route؛ ServiceCard presentation فقط | لا service model/API/data contract | **HOLD** | Government Service domain + public contract + بيانات جهة معتمدة + eligibility/steps/source governance |
| Unified Search | SearchField UI فقط؛ البحث المحلي الحالي ليس contract | لا search API/index/ranking/filter contract | **HOLD** | عقد backend موثق، scope واضح، empty/error/offline semantics، ونتائج حقيقية قابلة للربط |

## 9. Blockers and risks

1. **False functionality:** local Homepage search يعرض وظيفة محدودة كأنها بحث موحد.
2. **Illustrative-data leakage:** fallback والـPortalHome hardcoded قد يحولان الأمثلة إلى واجهة production بلا warning.
3. **Shell contamination:** تركيب Public Shell في root layout سيؤثر على admin/login.
4. **Nested landmarks:** نقل الصفحات دون إزالة `<main>` الداخلي سيكسر semantic structure وSkip Link.
5. **Registry coverage:** schema موجود لكن جهة واحدة لا تكفي لشاشة Ministry أو دليل جهات.
6. **Service domain absent:** بطاقات presentation لا تعوض غياب API وحقائق الخدمة.
7. **Public unified-read coupling:** فتح `/api/v1/content` ليس قرار UI ويُمنع ربطه بتغيير feature flag.
8. **Staging unknown:** `render.yaml` و`netlify.toml` يصفان النشر لكن لا يثبتان URL/health/env/CORS حيًا.
9. **Editorial trust:** storage وHTTP 200 لا يثبتان أن النص حكومي معتمد.
10. **Existing dirty worktree:** توجد تغييرات ووثائق وأصول خارج هذا الفحص؛ يجب عزل أي دفعة لاحقة وعدم staging الشامل.

## 10. Recommended next batch

### Batch 5A — Public Routing Boundary only

النطاق المقترح بعد اعتماد المالك:

1. إنشاء `app/(public)/layout.tsx` فقط كـPublic Shell boundary.
2. نقل public page entries الحالية إلى group مع بقاء URLs.
3. إزالة shell القديم المكرر من public pages وتوحيد main/skip-link semantics.
4. إبقاء `/admin` و`/login` في مكانهما وبلا Government Public Shell.
5. عدم تغيير page content أو data fetchers أو fallbacks أو visual composition ضمن هذه الدفعة.
6. إضافة route/shell/accessibility regression coverage صغير أو harness مناسب.
7. التحقق من build route table وDesktop/Mobile/RTL/keyboard/overflow.

لا تشمل Batch 5A:

- Homepage visual implementation.
- Ministry أو Service Detail أو Search.
- API adapters جديدة أو feature flags.
- استبدال المحتوى التوضيحي.
- refactor شامل للصفحات أو inline styles.
- backend/database/Figma/deployment.

بعد نجاح Batch 5A يلزم **Data Adapter + Editorial Source Gate** مستقل قبل السماح بتنفيذ أي section من Homepage.

## 11. Final recommendation

**GO WITH CONDITIONS**

- **GO** لدفعة `Batch 5A — Public Routing Boundary only` لأنها path-preserving، تفصل public عن admin/auth، وتستخدم Public Shell المعتمد دون تغيير البيانات أو التصميم.
- **HOLD** لتنفيذ Homepage كاملة؛ يمكن لاحقًا تنفيذ sections فرعية فقط حين يكون مصدرها معتمدًا وحالة الغياب صريحة.
- **HOLD** لصفحة Ministry النهائية؛ backend contract جزئي لكن لا توجد وزارة أو خدمات/موارد مرتبطة.
- **HOLD** لـService Detail وUnified Search لغياب العقود والبيانات.
- لا يجيز هذا القرار تغيير backend أو database أو feature flags أو Figma، ولا يجيز deployment أو commit/push.

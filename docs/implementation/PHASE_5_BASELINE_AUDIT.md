# Phase 5 — Frontend Baseline Audit

> **الحالة:** Baseline frozen for review  
> **التاريخ:** 2026-08-27  
> **النطاق:** تدقيق read-only للواجهة الحالية وربط فجواتها بالشاشات الست المعتمدة في Figma  
> **التوصية:** **GO WITH CONDITIONS** — يسمح فقط بالتهيئة التدريجية وبناء الأساس والمشتركات بعد اعتماد الشروط في القسم 13؛ تبقى Service Detail وأي بيانات حكومية غير متوفرة في حالة HOLD.

## 0. حدود التدقيق ومصادر الأدلة

تم فحص الملفات التالية دون تعديل production code أو backend أو database أو feature flags أو Figma:

- `frontend/src/app/page.tsx`
- `frontend/src/app/globals.css`
- جميع `frontend/src/components/**`
- جميع `frontend/src/lib/**`
- جميع صفحات App Router، بما فيها مسارات الأخبار والإعلانات والقرارات والوثائق والخدمات
- `frontend/package.json` وملفات إعداد Next.js وTypeScript وESLint والبيئة وDocker
- controllers وDTOs العامة ذات الصلة بالجهات والمحتوى والخدمات المتاحة في backend
- `docs/review/phase4.2/README.md`
- `docs/review/phase4.3/README.md`
- `docs/implementation/PHASE_5_IMPLEMENTATION_PLAN.md`
- حالة التشغيل المحلية read-only على `http://localhost:8081`

مرجع التصميم المعتمد:

- Figma file: `cSFveyYsAe08Xr5kiZMXum`
- Page: `04 — Key Screens` (`71:2`)
- Homepage: Desktop `71:10`، Mobile `71:11`
- Ministry: Desktop `71:13`، Mobile `71:14`
- Service Detail: Desktop `71:16`، Mobile `71:17`

لم يُنشأ commit أو push، ولم تبدأ أي مرحلة implementation.

## 1. Executive baseline

| المؤشر | النتيجة |
|---|---|
| Frontend framework | Next.js App Router `16.3.2` + React `19` + TypeScript strict |
| Source inventory | 20 ملف TypeScript/TSX/CSS؛ منها 16 TSX وملف CSS عالمي واحد |
| App pages | 11 `page.tsx` |
| Shared components | 4 فقط: Header، Hero، Footer، SectionHeading |
| Inline style sites | 111؛ منها 49 في Admin و21 في Complaints و14 في Contact |
| UI/styling libraries | لا توجد مكتبة UI أو CSS-in-JS أو utility CSS أو icons |
| Frontend tests | لا يوجد test runner أو component tests أو E2E أو Storybook أو visual regression harness |
| Lint | **PASS** — `npm run lint` |
| Production build | **PASS** — `npm run build` وTypeScript pass |
| Runtime backend health | **PASS** — `GET /health` أعاد `OK` |
| Entity registry | جهة واحدة فقط؛ توجد خمسة entity types |
| Unified public content | `GET /api/v1/content` يعيد 404 لأن public unified-read flag غير مفعّل |
| Figma design system | 191 Variables، 31 component sets/184 variants، 9 patterns/18 responsive variants |

الخلاصة: البنية التقنية الأساسية قابلة للتطوير التدريجي، لكنها ليست طبقة UI قابلة لإسقاط Figma عليها مباشرة. نجاح lint/build لا يلغي فجوات العقود والبيانات وإتاحة المسارات.

## 2. Current frontend architecture

### 2.1 Rendering and composition

- المشروع يستخدم Next.js App Router وليس Pages Router.
- `app/layout.tsx` يضبط `lang="ar"` و`dir="rtl"` ويحمل `globals.css`، لكنه لا يوفر public shell مشتركًا.
- الصفحة الرئيسية وصفحات المحتوى التفصيلية Server Components.
- `Header` و`Login` و`Admin` و`Complaints` Client Components عند الحاجة للتفاعل.
- كل صفحة عامة تقريبًا تكرر `<Header />` و`<main>` و`<Footer />` بدل route group/layout عام.
- صفحات التفاصيل الأربع لا تستخدم Header أو Footer أصلًا؛ لذلك shell والتسلسل البصري غير متسقين بين المسارات.
- metadata في root ما زالت خاصة برئاسة مجلس الوزراء، وكذلك theme color الأحمر، ولا تمثل البوابة الحكومية الوطنية المعتمدة.
- لا توجد `loading.tsx` أو `error.tsx` أو `not-found.tsx` مخصصة. يستخدم Next fallback العام للـ404.

### 2.2 Data flow

- `lib/api.ts` هو عميل fetch مركزي صغير ويختار:
  - `API_BASE_URL` على الخادم.
  - `NEXT_PUBLIC_API_BASE_URL` في المتصفح.
  - fallback افتراضي `http://localhost:8080`.
- كل الطلبات تستخدم `cache: "no-store"`.
- timeout موحد: 1500ms من الخادم و10000ms من المتصفح.
- `ApiError` يحتفظ بـHTTP status فقط؛ لا يحفظ response body أو error code أو request path.
- الصفحة الرئيسية تطلب بالتوازي portal home والقوائم الأربع. هذا جيد للزمن، لكنه يضاعف مصادر composition ويجعل consistency مسؤولية الصفحة.
- عند الفشل تُستخدم بيانات `site-data.ts` التوضيحية؛ بعض حالات fallback لا تعرض للمستخدم بوضوح أن البيانات ليست حية.
- لا توجد طبقة repositories، query keys، schema validation، view-model mappers، أو فصل بين transport DTO وUI model.

### 2.3 Package and tooling assessment

| المجال | الموجود | الحكم |
|---|---|---|
| Runtime | `next`, `react`, `react-dom` فقط | خفيف ومناسب، **REUSE** |
| Type system | TypeScript strict + alias `@/*` | **REUSE** |
| Lint | ESLint + Next config | **REUSE/MODIFY** لإضافة قواعد accessibility عند التنفيذ |
| Styling | CSS عالمي + inline style | **MODIFY** تدريجيًا |
| Components | لا Radix/Headless UI/React Aria | لا تضف dependency قبل use case؛ semantics يمكن بناؤها native-first |
| Icons | لا مكتبة ولا assets في `public` | شعار/أيقونات dependency لم تُحسم |
| Testing | لا Vitest/Jest/RTL/Playwright/axe | **CREATE** قبل page migration |
| Storybook/docs | غير موجود | اختياري؛ ليس شرطًا قبل أول slice إن وُجد component test harness بديل |

## 3. Current routes

نتيجة build الفعلية:

| Route | Rendering | البيانات الحالية | shell الحالي | التقييم |
|---|---|---|---|---|
| `/` | Dynamic SSR | portal home + القوائم الأربع + static fallbacks | Header/Footer | **MODIFY** |
| `/about` | Static | hardcoded presentation copy | Header/Footer | خارج الشاشات الست؛ **REUSE/MODIFY later** |
| `/services` | Static | cards ومسارات ثابتة | Header/Footer | directory prototype؛ **MODIFY** |
| `/contact` | Static | بيانات مثال + form غير مرسل | Header/Footer | **REMOVE-LATER** للمحتوى الوهمي |
| `/complaints` | Static client | `POST /api/support/requests` | Header/Footer | **REUSE/MODIFY** |
| `/login` | Static client | `POST /api/auth/login` | بلا public shell | **REUSE** خارج Phase 5 visual scope |
| `/admin` | Static client | admin content/support APIs | Header/Footer | خارج نطاق الشاشات المعتمدة |
| `/news/[id]` | Dynamic SSR | `GET /api/news/{id}` + local fallback | بلا Header/Footer | **MODIFY** |
| `/announcements/[id]` | Dynamic SSR | `GET /api/announcements/{id}` + local fallback | بلا Header/Footer | **MODIFY** |
| `/decisions/[id]` | Dynamic SSR | `GET /api/decisions/{id}` + local fallback | بلا Header/Footer | **MODIFY** |
| `/documents/[id]` | Dynamic SSR | `GET /api/documents/{id}` + local fallback | بلا Header/Footer | **MODIFY** |

### 3.1 Missing routes required by the approved experience

- لا توجد list routes مستقلة لـ`/news` أو `/announcements` أو `/decisions` أو `/documents`; Header يعتمد على homepage anchors.
- لا يوجد `/search` ولا Unified Search endpoint.
- لا يوجد public government directory route في frontend.
- لا توجد `/ministries/[slug]` أو أي entity template route.
- لا توجد `/services/[serviceSlug]`; المسار الحالي قائمة static فقط.
- لا يوجد route عام لأنواع الجهات الأخرى: authorities، independent entities، governorates.

### 3.2 Route correctness debt

- صفحات التفاصيل تحول `params.id` إلى Number لكنها لا تتحقق صراحةً من integer موجب finite قبل الاتصال.
- catch العام يخلط 404 و5xx وفشل الشبكة ثم يحاول fallback محليًا؛ هذا قد يحول عطلًا حقيقيًا إلى محتوى prototype.
- روابط Header ذات fragments لا تطابق `pathname`، لذلك `aria-current` لا يعبّر بدقة عن section navigation.
- service cards وneed pills تؤدي كلها إلى `/services` بدل وجهة خاصة بالعنصر.
- بعض document fallbacks تشير إلى `/#documents`، وهي ليست صفحة وثيقة فعلية.

## 4. Existing shared components

| Component | الوظيفة الحالية | قابلية إعادة الاستخدام | الفجوة الرئيسية |
|---|---|---|---|
| `Header.tsx` | brand PMO، navigation، search، mobile details | **MODIFY** | ليس Global Government Header المعتمد؛ mobile behavior غير modal-safe؛ البحث غير موحد |
| `Hero.tsx` | عنوان PMO، CTAs، highlights، stats | **MODIFY عميق** | IA والhierarchy مختلفان عن Search Hero؛ يخلط محتوى وإحصاءات وروابط |
| `Footer.tsx` | روابط قليلة وقنوات تواصل | **MODIFY** | PMO-specific؛ لا data-driven groups ولا legal/source contract |
| `SectionHeading.tsx` | eyebrow ثابت + title/description | **MODIFY** | يعرض `h3` دائمًا ويكسر heading hierarchy حسب موضعه |

لا توجد primitives مشتركة للأزرار، الروابط، الحقول، البحث، التنبيهات، badges، breadcrumbs، accordions، cards، empty/loading/error states أو container/section layout. الموجود حاليًا CSS classes عامة، وليس component API يمكن اختباره وربطه بـFigma variants.

## 5. Styling approach

### 5.1 الوضع الحالي

- `globals.css` ملف واحد بحوالي 550 سطرًا.
- root يحتوي عشرة متغيرات أساسية فقط: background/surface/red/text/muted/line/green/shadow.
- كثير من القيم اللونية والمسافات والحدود hardcoded داخل CSS وTSX.
- 111 موضع inline styles يصعّب responsive states وtheme/token migration.
- اللون الأحمر `#B21F2D` هو primary action وlink/navigation accent وgradient، بينما Figma يعتمد Civic Blue للتفاعل ويقصر National Red على الهوية الوطنية.
- الخط Tahoma ثم Segoe UI/Arial، بينما Figma المعتمد يستخدم Noto Sans Arabic.
- container الحالي `1180px` مقابل composition معتمد يقارب `1160px`.
- breakpoints الحالية `980px` و`520px` فقط، مقابل Mobile/Tablet/Desktop foundations عند 320/768/1200/1440.
- grids تتحول غالبًا مباشرةً من 3 columns إلى 1 عند 980px؛ لا توجد tablet composition من 8 columns.
- cards/surfaces/shadows/radii مستخدمة بكثافة، ما يعيد إحساس “كل شيء Card” الذي عالجته Phase 4.1.

### 5.2 نقاط قابلة للحفاظ

- يوجد استخدام جيد مبدئي لبعض logical properties.
- `scroll-margin-top` موجود للأقسام المرتبطة بالـHeader.
- `prefers-reduced-motion` موجود ويلغي smooth scrolling/transitions.
- `:focus-visible` موجود كأساس، لكنه hardcoded وغير شامل لكل form controls.
- CSS لا يعتمد مكتبة معقدة، ما يسمح بإضافة token layer بصورة additive دون rewrite.

### 5.3 حالات styling ناقصة

- focus selector لا يشمل select وtextarea صراحةً.
- الحد الأدنى الشائع لأهداف اللمس 44px، بينما Mobile Figma يتطلب 48px.
- لا توجد state recipes منتظمة لـactive/disabled/error/loading.
- `.notice--success` مستخدمة في JSX دون تعريف styling مكافئ واضح.
- global `a { text-decoration: none; }` يضعف link affordance.
- hover transforms موجودة لبعض cards، دون عقد واضح للحركة أو active state.

## 6. Current data/API bindings

### 6.1 Bindings الموجودة في frontend

| UI/domain | frontend call | backend route | الحالة |
|---|---|---|---|
| Home composition | `api.getPortalHome()` | `GET /api/portal/home` | يعمل، لكن response hardcoded وPMO-oriented |
| News list/detail | `getNews/getNewsById` | `GET /api/news[/id]` | متاح عبر compatibility facade |
| Announcements list/detail | `getAnnouncements/getAnnouncementById` | `GET /api/announcements[/id]` | متاح عبر compatibility facade |
| Decisions list/detail | `getDecisions/getDecisionById` | `GET /api/decisions[/id]` | متاح عبر compatibility facade |
| Documents list/detail | `getDocuments/getDocumentById` | `GET /api/documents[/id]` | متاح عبر compatibility facade |
| Citizen request | `createSupportRequest` | `POST /api/support/requests` | متاح |
| Support inbox/status | admin calls | `GET/PATCH /api/support/requests...` | متاح للمصرح لهم |
| Auth | `api.login` | `POST /api/auth/login` | متاح |
| Legacy admin content | list/summary/create/update/delete | `/api/admin/content...` | متاح للمصرح لهم |

### 6.2 Backend APIs موجودة لكن غير مربوطة بالواجهة

| Backend capability | route | live baseline | frontend binding |
|---|---|---|---|
| Entity types | `GET /api/v1/entity-types` | خمسة types | غير موجود |
| Entity directory | `GET /api/v1/entities` | جهة واحدة: PMO | غير موجود |
| Entity by ID | `GET /api/v1/entities/{id}` | متاح | غير موجود |
| Entity by type/slug | `GET /api/v1/entities/by-slug/{type}/{slug}` | متاح | غير موجود |
| Child entities | `GET /api/v1/entities/{id}/children` | متاح | غير موجود |
| Unified public content | `GET /api/v1/content...` | controller خلف flag؛ 404 حاليًا | غير موجود |
| Entity-filtered content | `GET /api/v1/entities/{id}/content` | خلف unified-read flag | غير موجود |

### 6.3 بيانات مفقودة

- لا يوجد GovernmentService domain أو API أو record موثوق يدعم Service Detail.
- لا يوجد Unified Search API ولا index/query contract.
- لا توجد service categories أو life events APIs.
- لا توجد datasets/projects/budgets/statistics APIs.
- entity DTO يغطي الهوية الأساسية والعلاقة والـwebsite فقط؛ لا يغطي leadership أو contact details أو ساعات العمل أو entity services.
- قاعدة البيانات المحلية لا تحتوي وزارة الصحة المعروضة في Figma؛ تحتوي PMO فقط.
- لا توجد خدمة حقيقية واحدة يمكن ربط CTA الخاص بها بـ`startUrl` موثوق.

### 6.4 بيانات لا يجوز ترقيتها إلى production

- `site-data.ts` و`portalHomeFallback`.
- hardcoded data في `PortalHomeController`.
- بطاقات الخدمات والأهلية والرسوم والمدة والمتطلبات والخطوات في Figma.
- subordinate entities والقيادات وبيانات الاتصال التوضيحية في شاشة Ministry.
- البريد `example.gov.ye` ورقم الهاتف التوضيحي في Contact.
- أي قيمة تحمل “مثال” أو “توضيحي”.

## 7. RTL and Arabic handling

### Strengths

- root document مضبوط `lang="ar"` و`dir="rtl"`.
- المحتوى الأساسي عربي وDOM order غالبًا طبيعي.
- توجد logical CSS properties في عدة مواضع.
- layout لا يعتمد كليًا على `row-reverse`.

### Gaps

- inline styles تستخدم `paddingRight` و`textAlign: "left"` وخصائص physical أخرى.
- لا يوجد `dir="auto"` أو `bdi` للـURLs والأرقام والهواتف والأكواد المختلطة.
- arrows/directional controls لا تملك contract للانعكاس الدلالي.
- الخط الحالي لا يطابق metrics الخاصة بـNoto Sans Arabic، ما سيغير wrapping وارتفاع الصفحات.
- لا توجد اختبارات extreme Arabic length أو كلمات/روابط غير قابلة للكسر.
- Header brand يستخدم `h1` في كل صفحة؛ الصفحة الرئيسية نفسها تبدأ Hero بـ`h2`، فتكون document hierarchy مرتبطة بالعلامة لا بمحتوى الصفحة.

## 8. Accessibility baseline

### Existing positives

- landmarks الأساسية موجودة: header/nav/main/footer في معظم الصفحات العامة.
- forms تستخدم native input/select/textarea.
- Header search يحمل `role="search"`.
- بعض حالات الخطأ تستخدم `role="alert"` والنجاح `role="status"`.
- `aria-current` موجود في navigation من حيث المبدأ.
- reduced motion وfocus-visible موجودان.
- build وlint لا يظهران أخطاء.

### Defects and missing contracts

- لا يوجد skip link ولا `#main-content`.
- لا يوجد focus management عند navigation أو بعد أخطاء forms.
- mobile navigation وsearch يستخدمان `details/summary` بلا overlay أو focus trap أو Escape/return-focus contract أو scroll lock.
- Admin modal عبارة عن fixed div، وليس dialog semantic، ولا يملك focus trap/restore؛ يبقى خارج scope لكنه baseline debt.
- لا يوجد global loading/error/offline/partial-data behavior.
- لا يوجد `aria-live` منظم لنتائج البحث أو async sections.
- SectionHeading يفرض h3؛ heading order غير مضمون.
- brand h1 يتكرر، والصفحة الرئيسية لا تملك h1 خاصًا بمحتواها.
- focus ring غير semantic-token-bound ولا يشمل كل controls.
- أهداف اللمس mobile أصغر من 48px في مواضع.
- الروابط العامة بلا underline؛ بعض cards تبدو تفاعلية لكن link target محصور في العنوان.
- Contact submit `type="button"`، لذلك النموذج الذي يبدو قابلًا للإرسال غير فعّال.
- لا توجد اختبارات keyboard أو screen reader أو axe أو zoom/reflow.

## 9. Responsive baseline

### Existing behavior

- container مرن مع max width.
- بعض grids تستخدم CSS Grid و`auto-fit/minmax`.
- Header يملك تركيب Desktop وآخر Mobile.
- توجد breakpoint لمعظم layouts عند 980px وأخرى دقيقة عند 520px.
- sections ليست مبنية عمومًا على fixed page heights.

### Gaps

- لا توجد tablet strategy أو 8-column grid.
- التحول 3→1 عند 980px يترك فجوة composition كبيرة بين Mobile وDesktop.
- Desktop Header يتم إخفاؤه واستبداله بتفاصيل Mobile، لكن interaction model غير مكتمل.
- inline styles في forms/admin تقلل القدرة على إدارة 320px وzoom.
- لم توجد اختبارات 320/360/768/1024/1200/1440 أو 200% text و400% zoom.
- لا توجد visual goldens في test harness رغم وجود PNGs المعتمدة في `docs/review/phase4.3/after`.
- Mobile Figma frame 360px، بينما بعض review exports الموروثة تعرض content render بعرض 352px؛ يلزم normalization موثق قبل pixel diff.

## 10. Existing duplication and debt

1. تكرار Header/Main/Footer يدويًا بدل public layout.
2. تكرار card markup بين news/announcements/decisions/documents والخدمات.
3. تكرار field styles في Login/Contact/Complaints/Admin.
4. 111 inline style sites، ما يمنع token/state consistency.
5. أربعة detail pages متشابهة جدًا دون content-detail template مشترك.
6. data fallback موزع بين page logic و`site-data.ts`.
7. `PortalHomeContent` وlegacy DTOs غير موحدين؛ الصفحة تستخدم casts وتكييفات ad hoc.
8. endpoint errors تختزل في status ولا توجد typed domain errors.
9. كل public read `no-store` وtimeout واحد، بدل policy حسب نوع البيانات.
10. البحث مجرد filter محلي داخل الصفحة ويكرر مسؤولية المحتوى.
11. الخدمات static، وكل العناصر تقود للوجهة نفسها.
12. الهوية والنصوص والـmetadata ما زالت PMO-specific رغم تحول المنتج إلى national portal.
13. لا يوجد فصل آمن بين prototype fixtures وproduction imports.
14. لا توجد automated accessibility أو contract أو visual regression tests.

## 11. REUSE / MODIFY / CREATE / REMOVE-LATER matrix

| العنصر | التصنيف | السبب/الحد |
|---|---|---|
| Next.js App Router + React + strict TypeScript | **REUSE** | أساس حديث ويبني حاليًا بنجاح |
| root `lang="ar" dir="rtl"` | **REUSE** | أساس Arabic-first صحيح |
| native form elements | **REUSE** | تبقى native-first داخل wrappers مشتركة |
| مفهوم `lib/api.ts` المركزي | **REUSE** | يحافظ على نقطة اتصال واحدة |
| parallel fetching في Home | **REUSE** | بعد فصل section states ومصادر الحقيقة |
| reduced-motion rule | **REUSE** | يربط لاحقًا بـmotion tokens |
| Header | **MODIFY** | تحويله إلى national shell responsive وaccessible |
| Hero | **MODIFY** | يصبح Search Hero task-first ولا يحتفظ بتركيب PMO الحالي |
| Footer | **MODIFY** | groups ومصادر وروابط حقيقية وهوية وطنية |
| SectionHeading | **MODIFY** | heading level/API مرنان، دون eyebrow مفروض |
| `globals.css` | **MODIFY** | migration additive إلى semantic tokens، لا rewrite دفعة واحدة |
| `api.ts` | **MODIFY** | typed modules/errors/timeouts/caching/view models |
| Homepage `page.tsx` | **MODIFY** | section-by-section، مع حالات partial/empty/error |
| legacy content detail pages | **MODIFY** | shared page framework + validation + honest errors |
| `/services` | **MODIFY** | لا يعرض cards وهمية كخدمات فعلية |
| Complaints integration | **MODIFY** | reuse API مع accessible form and states |
| Design token layer | **CREATE** | mapping للـ191 variables عبر semantic aliases المطلوبة فقط |
| Container/Section/Stack primitives | **CREATE** | common framework ومنع تكرار layout |
| Button/Link/IconButton/Field/Search/Alert/Badge | **CREATE** | component APIs وحالات قابلة للاختبار |
| Breadcrumb/Accordion/MobileNavigation | **CREATE** | semantics وfocus/keyboard contract |
| Service/Entity/Content cards | **CREATE** | shared patterns بلا nested actions |
| AsyncSection/Empty/Error/Skeleton | **CREATE** | فشل جزئي صادق بدل fallback صامت |
| public route-group layout | **CREATE** | shell مشترك للصفحات العامة |
| `/ministries/[slug]` | **CREATE مشروط** | فقط على entity API وrecord موجود فعليًا |
| `/services/[serviceSlug]` | **CREATE بعد API** | لا ينشأ ضد fixture؛ حاليًا HOLD |
| loading/error/not-found boundaries | **CREATE** | runtime states و404 صحيحة |
| component/E2E/a11y/visual harness | **CREATE** | شرط قبل page migration |
| Code Connect | **CREATE LATER** | بعد ثبات component APIs؛ ليس جزءًا من التنفيذ الحالي |
| `site-data.ts` كـproduction fallback | **REMOVE-LATER** | يبقى مؤقتًا فقط حتى فصل fixtures؛ لا ينتقل للواجهة الوطنية |
| hardcoded PortalHome composition | **REMOVE-LATER** | لا يصلح source of truth وطنيًا |
| red-as-primary variables/classes | **REMOVE-LATER** | يستبدلها Civic Blue semantic action tokens |
| `details/summary` كقائمة Mobile الرئيسية | **REMOVE-LATER** | يستبدل بزر/drawer contract مكتمل |
| duplicated card/pill/list-card recipes | **REMOVE-LATER** | بعد ترحيل كل consumer إلى components المشتركة |
| repeated inline field styles | **REMOVE-LATER** | بعد field primitives؛ لا bulk delete قبل الترحيل |
| contact example data/nonfunctional form | **REMOVE-LATER** | لا ينشر كمعلومة حكومية |

`REMOVE-LATER` تعني deprecation تدريجيًا بعد وجود بديل واختبارات؛ لا تعني حذفًا في هذه المرحلة.

## 12. Gap analysis against approved Figma screens

### 12.1 Design-system gap

| Figma contract | baseline code | gap |
|---|---|---|
| 191 Variables / semantic tokens | 10 root variables + hardcoded values | كبير |
| 15 Text Styles / Noto Sans Arabic | ad hoc headings + Tahoma | كبير |
| 3 Elevation + restrained surfaces | shadow واحد وcard-heavy | متوسط/كبير |
| 4/8/12 grid | generic grids + 2 breakpoints | كبير |
| 31 component sets / full states | 4 shared components، بلا variant APIs | كبير |
| 9 responsive patterns | Header/Hero/Footer فقط وبتركيب قديم | كبير |
| Civic Blue actions | National Red actions | مانع visual parity |
| 48px Mobile targets | 44px غالبًا | accessibility gap |
| keyboard/focus runtime contract | جزئي وغير مختبر | كبير |

### 12.2 Homepage — approved vs current

الترتيب المعتمد: Header → Search Hero → Services Discovery → Government Entities → Recent Official Content → Open Government and Engagement → Footer.

الوضع الحالي:

- Header وHero PMO-oriented.
- البحث داخل Header وfilter محلي؛ ليس نقطة الدخول الرئيسية ولا Unified Search.
- الصفحة تعرض official statements/news/developments/decisions/services/documents/media/governance principles بتركيب مختلف.
- لا يوجد Government Entities section مربوط بالentity registry.
- services/cards/pills static ولا ترتبط بعقد خدمات.
- open government composition لا تملك datasets/APIs حقيقية.
- تستخدم fallback presentation content عند تعطل البيانات.

النتيجة: لا يمكن الوصول إلى parity بتعديل CSS فقط؛ يلزم composition refactor بعد حسم search والخدمات وسياسة الأقسام غير المدعومة.

### 12.3 Government Entity / Ministry

الترتيب المعتمد: Header → Entity Identity/Breadcrumbs/Navigation/Subordinates → Key Services → News/Announcements → Institutional Resources → Contact/Source → Footer.

الوضع الحالي:

- لا route ولا template ولا frontend entity fetcher.
- backend يوفر الهوية الأساسية والـchildren، لكن registry المحلي يحتوي PMO فقط.
- لا Ministry of Health record.
- لا entity services ولا leadership ولا contact details.
- entity content public endpoint غير متاح حاليًا بسبب flag مستقل.

النتيجة: يمكن بناء shared entity shell لاحقًا، لكن نشر شاشة وزارة الصحة أو ملء الأقسام من Figma ممنوع. أول route حقيقي يجب أن يستخدم جهة موجودة وبالأقسام التي يثبت API وجودها فقط.

### 12.4 Government Service Detail

الترتيب المعتمد: Header → Breadcrumbs/Service Hero/CTA → Key Facts → Eligibility/Requirements → Steps → Related Services → Support/Metadata → Footer.

الوضع الحالي:

- `/services` directory static فقط.
- لا dynamic detail route.
- لا service domain أو DTO أو API أو start URL أو owning-entity relation.
- Accordion runtime state machine لم يحسمه frame؛ Figma يعرض المحتوى للتوثيق البصري فقط.

النتيجة: الشاشة **HOLD** بالكامل كصفحة data-fed. يمكن تنفيذ Accordion primitive مستقل عند اعتماد السلوك، لكن لا يجوز إنشاء صفحة خدمة “حقيقية” من محتوى Figma.

## 13. Risks and conditions before implementation

| Priority | الخطر | الأثر | شرط الإغلاق |
|---|---|---|---|
| P0 | تحويل prototype/Figma content إلى production | معلومات حكومية غير موثقة | provenance policy + فصل fixtures عن production graph |
| P0 | لا Unified Search contract | Primary entry غير صادق | API/route حقيقي أو حالة unavailable معتمدة |
| P0 | لا GovernmentService domain/API | Service Detail وهمية | backend contract + record موثوق + start/support data |
| P0 | Ministry المعتمدة غير موجودة في registry | صفحة جهة بلا مصدر | data governance onboarding؛ لا seed من Figma |
| P1 | silent fallback يخفي outages | ثقة المستخدم ومعلومات قديمة | async state model وفصل empty/error/offline |
| P1 | unified entity content خلف flag | أقسام Ministry غير قابلة للتعبئة | قرار Phase 2 مستقل؛ frontend لا يغير flags |
| P1 | لا test harness | regressions في refactor واسع | component + E2E + axe + visual baseline قبل page slices |
| P1 | mobile nav/focus gaps | فشل keyboard/WCAG | behavior contract واختبارات |
| P1 | design tokens غير ممثلة | drift وتكرار | additive semantic token layer مع mapping موثق |
| P1 | 111 inline styles | صعوبة responsive/state consistency | ترحيل تدريجي مع كل component، لا bulk rewrite |
| P2 | Tahoma مقابل Noto | اختلاف line breaks/heights | font delivery decision قبل visual goldens |
| P2 | لا tablet design screen | احتمالات reflow غير مراجعة | 768/1024 review snapshots بقواعد grid المعتمدة |
| P2 | 352px review render داخل frame 360 | false visual diffs | normalized golden أو re-export 360/720 read-only |
| P2 | API timeout/no-store موحدان | fallback متكرر وضغط غير ضروري | policy حسب endpoint بعد القياس |

### 13.1 شروط GO

1. اعتماد semantic token migration كإضافة بجانب CSS الحالي، لا إعادة كتابة شاملة.
2. اعتماد public shell وshared primitives قبل تعديل الصفحات.
3. منع أي production import من fixtures/fallback presentation data في الصفحات الجديدة.
4. إبقاء Search Hero بلا ادعاء وظيفي حتى وجود Unified Search، أو اعتماد حالة unavailable واضحة.
5. بناء Ministry فقط على entity موجودة في registry وبحقول API المتاحة.
6. إبقاء Service Detail route في HOLD حتى وجود عقد backend وسجل موثوق.
7. إضافة component/E2E/accessibility/visual test tooling قبل بدء migration للشاشات.
8. عدم ربط Phase 5 بأي تغيير compatibility flag أو canary أو write cutover.

## 14. Final recommendation

**GO WITH CONDITIONS**

مبررات القرار:

- **GO** تقنيًا لمرحلة foundations/tokens، shared primitives، common page framework، Header/Footer، واختبارات الأساس: المشروع صغير، TypeScript strict، وlint/build ناجحان، ويمكن إجراء refactor additive منخفض المخاطر.
- **CONDITIONAL GO** للـHomepage shell وMinistry template فقط بعد حسم البحث وسياسة البيانات، ومع عرض بيانات مصدرها API الحقيقي.
- **HOLD** لصفحة Service Detail ولأي section يعتمد خدمات أو حقائق وزارة أو open-government data غير موجودة.

هذه التوصية لا تعتمد بدء التنفيذ تلقائيًا. يلزم اعتماد صريح جديد لنطاق أول implementation slice بعد مراجعة هذه الوثيقة.

## 15. Verification record

- `npm run lint`: **PASS**
- `npm run build`: **PASS**
- routes generated: 11 application routes + Next `/_not-found`
- backend containers: running؛ database healthy
- `GET /health`: **OK**
- `GET /api/v1/entity-types`: خمسة أنواع
- `GET /api/v1/entities`: سجل واحد
- `GET /api/portal/home`: يعمل، لكنه composition hardcoded
- `GET /api/v1/content?size=1`: HTTP 404 بسبب unified public-read configuration
- production code/backend/database/Figma/feature flags: **لم تتغير**
- commit/push/deployment: **لم تنفذ**

# Phase 5 — Design-to-Code Contract

> **الحالة:** Proposed for owner approval  
> **التاريخ:** 2026-08-27  
> **المشروع:** yemen-pmo-platform  
> **Baseline:** PHASE_5_BASELINE_AUDIT.md  
> **Figma source of truth:** cSFveyYsAe08Xr5kiZMXum  
> **نطاق الوثيقة:** عقد تخطيط وربط فقط؛ لا production implementation ولا Figma write ولا backend/database change.

## 1. القرار التنفيذي المختصر

العقد المقترح هو:

1. Figma يبقى مصدر الحقيقة للهوية والـvisual states والـresponsive composition.
2. الكود لا ينسخ نموذج Figma واحدًا لواحد؛ Components تتحول إلى APIs برمجية صغيرة، وPatterns تتحول إلى compositions بحسب الحاجة.
3. Runtime components تستخدم semantic tokens فقط. Primitive tokens تبقى طبقة داخلية، ولا تستدعيها الصفحات مباشرة.
4. لا تُولد 191 CSS custom property آليًا. يُنفذ فقط dependency closure اللازمة لكل دفعة، مع سجل mapping يحفظ بقية القيم.
5. Civic Blue هو لون التفاعل الأساسي. National Red لا يستخدم للأزرار أو الروابط العامة، ويقتصر على العلامة الوطنية والزخرفة المحدودة.
6. لا يدخل أي نص أو رقم أو علاقة حكومية من Figma أو site-data.ts إلى production data.
7. Ministry وService Detail وUnified Search تحافظ على حالات HOLD الموضحة في هذه الوثيقة.

التوصية: **APPROVE CONTRACT، ثم GO مشروط لدفعات Foundations وShared Primitives وPublic Shell فقط.**

## 2. مصادر الحقيقة ونتائج الفحص

### 2.1 Figma

- Foundations: ثماني Variable Collections و191 Variable.
- Styles: 15 Text Styles و3 Elevation Styles و3 Grid Styles.
- Components: 31 Component Sets و184 Variant.
- Patterns: 9 Pattern Sets و18 responsive variants.
- Key Screens: ست شاشات معتمدة في صفحة 04 — Key Screens.
- صفحة Components المرجعية: 22:2، وصفحة Patterns المرجعية: 47:2، وصفحة Key Screens: 71:2.
- الشاشات المعتمدة: Homepage Desktop 71:10، Homepage Mobile 71:11، Ministry Desktop 71:13، Ministry Mobile 71:14، Service Detail Desktop 71:16، Service Detail Mobile 71:17.
- جميع الشاشات RTL-first وتستخدم semantic paint bindings وNoto Sans Arabic.
- touch targets في الشاشات المعتمدة لا تقل عن 48px.

تم تنفيذ قراءة Plugin API مباشرة لقيم Collections وStyles دون تعديل الملف.

### 2.2 Code Connect

- لا يوجد figma.config.json أو ملفات .figma.ts في المستودع.
- استعلام Code Connect read-only للـComponents أعاد أن الميزة تتطلب Dev أو Full seat على Organization أو Enterprise.
- الخطة الحالية لا تسمح بإنشاء أو قراءة property context الخاص بـCode Connect رغم توفر الوصول إلى التصميم.
- لا يُنشأ mapping تلقائي ولا تتم مزامنة في هذه المرحلة.
- هذه الوثيقة هي equivalent mapping contract المؤقت. يمكن لاحقًا إضافة manifest محلي versioned ثم Code Connect بعد ثبات Component APIs وتوفر الخطة المناسبة.

### 2.3 Frontend baseline

- Next.js App Router 16.3.2، React 19، TypeScript strict.
- 11 صفحة و4 shared components فقط.
- ملف CSS عالمي واحد و111 inline-style site.
- لا UI library أو test runner أو visual regression harness.
- lint وproduction build ناجحان.
- لا Ministry route ولا Service Detail route ولا Unified Search.

## 3. Translation rules

### 3.1 Figma representation لا يساوي code object

- Component Set متقارب الوظيفة يتحول إلى component واحد بprops، لا ملف لكل variant.
- Pattern لا يتحول تلقائيًا إلى React component. إذا كان مجرد ترتيب page sections، يبقى composition داخل الصفحة.
- Styles تتحول إلى recipes أو classes، وليس إلى React components.
- Device variants تتحول غالبًا إلى CSS responsive composition أو shell variants، لا prop اسمه device في كل component.
- Figma Error وLoading frames تصف الحالة المرئية؛ مصدر الحالة في الكود هو runtime state.

### 3.2 Public component API

- props تعبر عن المعنى: variant، tone، size، status، disabled، loading.
- لا exposes لـraw Figma node IDs أو layer names.
- لا prop لنسخ CSS مباشر من التصميم.
- action يحدد عنصر HTML: navigation = link، mutation = button، disclosure = button.
- كل component له accessible name وkeyboard contract قبل اعتماده.

### 3.3 Styling boundary

- tokens العامة في ملف global واحد مقترح: styles/tokens.css.
- base/reset وdocument defaults تبقى global.
- component recipes تستخدم CSS Modules أو ملفات component-scoped.
- globals.css الحالي يبقى خلال الانتقال، وتُنقل selectors فقط عند ترحيل consumer فعلي.
- لا CSS-in-JS dependency مطلوبة.
- inline style يسمح فقط لقيمة ديناميكية حقيقية عبر CSS custom property محلية، وليس لمسافات وألوان ثابتة.

## 4. Foundation-to-code contract

### 4.1 Token layers

| الطبقة | الغرض | من يستخدمها | السياسة |
|---|---|---|---|
| Primitive | القيم الخام للألوان والمقاييس | semantic token definitions فقط | private naming؛ لا استخدام مباشر في pages/components |
| Semantic | المعنى: action، text، surface، border، feedback | كل component وpattern | public runtime contract |
| Recipe | typography، elevation، layout composition | CSS component/page recipes | ليست بالضرورة CSS variables منفصلة |
| Component-specific | حالات لا يعبّر عنها semantic token عام | component نفسه فقط | تُنشأ عند تكرار فعلي، لا مسبقًا |
| Data visualization | charts والبيانات الكمية | لا شيء حاليًا | DEFER بالكامل |

### 4.2 Emission policy للـ191 Variables

لا تُنسخ المجموعات الثماني آليًا إلى CSS. التنفيذ على دفعات:

- Color Primitives: emit فقط primitives التي تعتمد عليها semantic colors المستخدمة في الدفعة.
- Color Semantic: هذه هي الواجهة العامة للألوان؛ يضاف token عند دخول consumer فعلي.
- Spacing: يمكن اعتماد السلم الكامل ذي 14 قيمة لأنه صغير ومستخدم أفقيًا.
- Radius: اعتماد القيم الست كاملة.
- Layout: breakpoints تبقى constants موثقة داخل media queries لأن CSS custom properties لا تعمل كحدود media query؛ margins/gutters/touch/focus/icons يمكن أن تصبح tokens.
- Motion: اعتماد durations/easings التسعة؛ reduced-motion يعيد semantic durations إلى 0ms.
- Typography: لا تُنشأ 33 CSS variable لمجرد المطابقة؛ تُحفظ family/weight primitives وتُنفذ 15 typography recipe مع القيم المعتمدة.
- Data Visualization: لا emit قبل use case وبيانات حقيقية.

القاعدة: أي token جديد يحتاج Figma source name، target name، consumer، وسبب إضافته.

### 4.3 Color contract

### Core identity and interaction

| المعنى | Figma semantic | resolved value | target CSS token |
|---|---|---:|---|
| Primary action default | action/primary/bg-default | #005A96 | --yegov-color-action-primary-bg-default |
| Primary action hover | action/primary/bg-hover | #064B78 | --yegov-color-action-primary-bg-hover |
| Primary action active | action/primary/bg-active | #0B3D60 | --yegov-color-action-primary-bg-active |
| Primary action text | action/primary/text | #FFFFFF | --yegov-color-action-primary-text |
| Focus border | border/focus | #167FC4 | --yegov-color-border-focus |
| Link | text/link | #005A96 | --yegov-color-text-link |
| Link hover | text/link-hover | #064B78 | --yegov-color-text-link-hover |
| National accent | national/accent/default | #8D1B2D | --yegov-color-national-accent-default |
| National subtle | national/accent/subtle | #FFF4F5 | --yegov-color-national-accent-subtle |
| National accent text | national/accent/text | #751829 | --yegov-color-national-accent-text |

### Surfaces, text, borders, feedback

- Canvas: neutral/50، resolved #F7F8FA.
- Surface and raised surface: white.
- Subtle surface: neutral/100، resolved #F1F3F5.
- Primary text: neutral/900، resolved #161B22.
- Secondary text: neutral/700، resolved #343D46.
- Subtle text: neutral/600، resolved #4A5561.
- Disabled text: neutral/500، resolved #66717D.
- Default border: neutral/300، resolved #CBD2D9.
- Subtle border: neutral/200، resolved #E3E7EB.
- Success/Warning/Error/Info تستخدم مجموعات feedback semantic الثلاثية: background، border، text.

### National Red usage rule

مسموح:

- brand mark والنص الوطني المحدود.
- الخط الوطني الصغير المعتمد.
- motif أو accent محدود خارج task cards.
- حالات وطنية identity-only الموثقة في Figma.

غير مسموح:

- primary button أو generic link.
- focus ring أو selected navigation.
- error state لمجرد أنه أحمر وطني.
- خلفيات واسعة أو flag-themed decoration.

### 4.4 Typography contract

### Font delivery

- Arabic default: Noto Sans Arabic.
- LTR utility contexts فقط: Inter.
- الخيار الموصى به: self-hosted font files لضمان build وvisual regression ثابتين.
- Tahoma/Segoe UI يبقيان fallback مؤقتًا حتى إضافة الملفات المرخصة والتحقق من metrics.
- قرار font assets يحتاج اعتماد المالك قبل أول visual baseline برمجي.

### Text recipes

| Figma style | size/line | weight | target recipe |
|---|---:|---:|---|
| Display/XL | 56/72 | 700 | displayXl |
| Display/Large | 48/64 | 700 | displayLg |
| Heading/H1 | 40/52 | 700 | headingH1 |
| Heading/H2 | 32/44 | 700 | headingH2 |
| Heading/H3 | 24/36 | 600 | headingH3 |
| Heading/H4 | 20/32 | 600 | headingH4 |
| Body/Large | 18/32 | 400 | bodyLg |
| Body/Medium | 16/28 | 400 | bodyMd |
| Body/Small | 14/24 | 400 | bodySm |
| Label/Large | 16/24 | 600 | labelLg |
| Label/Medium | 14/20 | 600 | labelMd |
| Label/Small | 12/18 | 600 | labelSm |
| Data/Metric Large | 32/40 | 700 | metricLg — DEFER until sourced data |
| Data/Metric Medium | 24/32 | 700 | metricMd — DEFER until sourced data |
| Data/Caption | 12/18 | 400 | dataCaption — DEFER with metrics |

HTML semantics لا تُشتق من اسم style. H1 يستخدم مرة واحدة لموضوع الصفحة، بينما يمكن تطبيق headingH2 على عنصر حسب outline الصحيح.

### 4.5 Spacing, radius, borders, elevation

### Spacing

القيم المعتمدة: 0، 2، 4، 8، 12، 16، 20، 24، 32، 40، 48، 64، 80، 96px.

أسماء target المقترحة تحذف التكرار الموجود في Figma web syntax:

- --yegov-space-0
- --yegov-space-2xs إلى --yegov-space-7xl

### Radius

- none 0
- xs 2px
- sm 4px
- md 8px
- lg 12px
- full 9999px

لا تُعاد قيم 16 و24px الحالية في components الجديدة ما لم يوجد deviation معتمد. full يستخدم pills/status فقط، لا كل container.

### Borders

- border color يأتي من semantic colors.
- default component stroke = 1px recipe ثابت؛ لا يوجد Figma border-width collection منفصل.
- focus ring width = 3px وoffset = 3px من Layout Variables.
- error لا يعتمد على لون border وحده؛ يرتبط برسالة وaria-invalid.

### Elevation

- Elevation/100: 0 1px 3px rgba(0,0,0,.08).
- Elevation/200: 0 4px 12px -2px rgba(0,0,0,.10).
- Elevation/300: طبقتان؛ 0 12px 32px -6px rgba(0,0,0,.12) و0 2px 8px rgba(0,0,0,.06).
- cards العادية تستخدم border أو Elevation/100 عند الحاجة، وليس shadow الحالي الكثيف.
- Elevation/300 محجوز للoverlay/dialog، وهو DEFER في أول دفعة.

### 4.6 Layout and responsive contract

| range | grid | margin | gutter | target behavior |
|---|---:|---:|---:|---|
| 320–767 | 4 | 16 | 16 | single column غالبًا؛ 48px targets |
| 768–1199 | 8 | 32 | 24 | explicit tablet reflow |
| 1200–1439 | 12 | 40 | 24 | desktop composition |
| 1440+ | 12 | centered | 24 | max-width؛ لا تمديد غير محدود |

- Figma Foundation container/max = 1200px.
- الشاشات الست تستخدم composition بعرض 1160px على Desktop.
- العقد المقترح: page container semantic max = 1160px، مشتق وموثق من 1200px ناقص 2 × spacing/lg.
- Mobile content = viewport ناقص 32px؛ عند 360px تكون 328px.
- لا تُنسخ heights الطويلة من Figma إلى CSS؛ page sections auto-height.
- grids تتحول 3→2→1 أو 2→1 حسب composition؛ لا تستخدم auto-fit إذا غير ترتيب القراءة.
- Tablet لا يملك high-fidelity screen؛ يلزم review screenshot عند 768 و1024 قبل قبول كل page slice.

### 4.7 Motion contract

- fast 120ms، standard 200ms، emphasized 300ms، slow 500ms.
- easing standard: cubic-bezier(0.2, 0, 0, 1).
- decelerate: cubic-bezier(0, 0, 0, 1).
- accelerate: cubic-bezier(0.3, 0, 1, 1).
- hover/focus لا يحتاج motion إلزاميًا.
- عند prefers-reduced-motion تصبح durations التفاعلية 0ms، ويلغى smooth scrolling وأي transform غير ضروري.

## 5. Target component architecture

التنظيم المقترح، وليس إذنًا بإنشاء الملفات:

- components/ui: Button، AppLink، IconButton، Field، Input، Select، Textarea، Alert، Badge.
- components/navigation: Breadcrumbs، NavLink، MobileNavigation، Accordion عند دخوله scope.
- components/layout: Container، Section، Stack، Cluster.
- components/shell: GovernmentHeader، GovernmentFooter.
- components/cards: ContentCard، GovernmentEntityCard، ServiceCard عند توفر البيانات.
- components/feedback: AsyncSection، EmptyState، ErrorState، Skeleton.
- components/patterns: compositions المتكررة فعليًا فقط.
- lib/api: client ثم modules حسب domain.
- lib/view-models: adapters بين DTOs وUI.
- styles: tokens، base، ثم component-scoped styles.

### 5.1 Shared shell

PublicLayout مسؤول عن:

- skip link.
- GovernmentHeader وGovernmentFooter.
- main id ثابت.
- route transition focus target.
- document language/direction.

Admin يبقى خارج visual migration الأولى، ولا يفرض shell الخاص به على public pages.

### 5.2 Buttons and links

- Button واحد بvariant primary/secondary، size، loading، disabled.
- AppLink لا يحاول تقليد disabled button؛ إن لم توجد وجهة فلا يُعرض كرابط.
- IconButton يحتاج label إلزاميًا.
- card link = anchor رئيسي واحد؛ يمنع nested interactive targets.
- external links تعلن الوجهة الجديدة إذا فتحت tab جديدًا.

### 5.3 Fields and search

- Field يجمع label/helper/error وaria-describedby.
- Input/Select/Textarea تبقى native-first.
- SearchField هو UI primitive فقط.
- SearchForm أو SearchHero integration يبقى HOLD حتى وجود search route/query contract.
- لا يُشحن زر بحث يعيد filter محدودًا بينما يوحي ببحث وطني موحد.

### 5.4 Cards

- ContentCard يقبل view model موحدًا: id، contentType، title، summary، publishedAt، entity label إن توفر، canonical href.
- GovernmentEntityCard يقبل بيانات Entity API فقط: name، type، summary المتاح، canonical path.
- ServiceCard لا يدخل production graph قبل Government Service contract.
- DataMetric لا يدخل production دون value، unit، source، asOf/update time.

### 5.5 Section headings and metadata

- SectionHeading يقبل heading level أو يعرض عنصرًا يحدده parent.
- eyebrow اختياري، وليس نصًا حكوميًا ثابتًا.
- MetadataList أو StatusMeta يستخدم dl عندما تكون البيانات label/value.
- Badge غير تفاعلي؛ filter chip أو tab مكون مختلف.
- date/time يخرج semantic time مع machine-readable value.

## 6. Figma Component Sets mapping

### 6.1 Consolidation decisions

- Button/Primary وButton/Secondary → Button واحد.
- Icon Button/Primary وSecondary → IconButton واحد.
- Pagination/Page وDirectional → Pagination composition واحدة، مؤجلة.
- Header primitives تبقى private building blocks داخل GovernmentHeader إلى أن يثبت reuse مستقل.
- Footer primitives تبقى private داخل GovernmentFooter.
- Card sets تبقى components مستقلة لأن عقود البيانات مختلفة.

### 6.2 State contract

| state | runtime contract |
|---|---|
| Default | معنى العنصر واضح دون hover |
| Hover | pointer devices فقط؛ لا معلومات حصرية |
| Focus | focus-visible 3px + 3px offset؛ لا يختفي تحت overlay |
| Active | منفصل عن focus؛ aria-current أو aria-pressed حسب المعنى |
| Disabled | native disabled للcontrols؛ لا disabled anchors |
| Error | aria-invalid ورسالة مرتبطة؛ لا لون فقط |
| Loading | الاسم المتاح يبقى ثابتًا؛ يمنع التكرار؛ status غير مزعج |

## 7. Figma Patterns mapping

| Pattern | target composition | هل يصبح component مستقلًا؟ | القرار |
|---|---|---|---|
| Global Government Header | GovernmentHeader + private brand/nav/menu pieces | نعم | MODIFY من Header الحالي |
| Government Search & Discovery | SearchHero composition + SearchField | SearchField نعم؛ Hero page composition | HOLD integration |
| Service Discovery | Section + ServiceCard list | لا قبل تكرار فعلي | DEFER data-fed section |
| Government Entity Identity | EntityIdentity داخل entity template | نعم عند route implementation | HOLD data contract |
| Content Discovery | SectionHeading + ContentCard + AsyncSection | composition قابلة لإعادة الاستخدام | CREATE بعد primitives |
| Open Government & Data | page section + verified links/metrics | لا generic component حاليًا | DEFER |
| Citizen Engagement | section links إلى complaints/contact | لا ما لم تتكرر | MODIFY باستخدام routes حقيقية |
| Common Page Framework | PublicLayout + Container + Section + Breadcrumbs + async states | لا single mega-component | CREATE primitives |
| Government Footer | GovernmentFooter + data-driven groups | نعم | MODIFY من Footer الحالي |

## 8. RTL, accessibility, and runtime contract

### 8.1 RTL/LTR

- root Arabic يظل lang=ar وdir=rtl.
- CSS يستخدم margin-inline، padding-inline، inset-inline وborder-inline.
- DOM order يتبع ترتيب القراءة؛ لا row-reverse لإصلاح markup.
- URLs، phone، codes، Latin dates تستخدم bdi أو dir=auto/dir=ltr محليًا.
- chevrons السابقة/التالية تنعكس دلاليًا؛ search/menu/status icons لا تقلب آليًا.
- component لا يملك dir prop إلا إذا كان يحتوي mixed-direction content مستقلًا.

### 8.2 Keyboard and focus

- أول focusable element هو skip link إلى main.
- Enter للروابط؛ Enter وSpace للأزرار/disclosures.
- Tab order يتبع DOM.
- route navigation ينقل focus إلى H1 أو main فقط وفق آلية موحدة.
- كل overlay يعيد focus إلى trigger.
- no positive tabindex.

### 8.3 Mobile navigation

العقد المقترح:

- trigger button مع aria-expanded وaria-controls.
- drawer modal على Mobile.
- Escape، overlay click، route selection تغلق القائمة.
- focus trap وreturn focus.
- background inert وscroll lock أثناء الفتح.
- reduced motion يحول transition إلى instant.

هذا قرار runtime يحتاج اعتماد المالك؛ Figma يصف التركيب المرئي ولا يحسم focus behavior.

### 8.4 Accordion

العقد المقترح:

- trigger button داخل heading.
- aria-expanded وaria-controls.
- يسمح بفتح أكثر من item.
- أول item مفتوح افتراضيًا في Service Detail runtime المقترح.
- focus يبقى على trigger بعد toggle.
- Mobile Figma يعرض static open list لأن master القديم لم يكن آمنًا تحت 520px؛ هذا لا يفرض static production implementation.

Service Detail نفسها تبقى HOLD؛ يمكن تأجيل Accordion حتى أول use case معتمد.

### 8.5 Async states

كل data-fed section يملك:

- loading
- success with data
- success empty
- error
- offline/network failure
- stale/cached مع timestamp إن استُخدم cache

فشل section لا يسقط shell أو الأقسام الناجحة. لا يُستبدل الفشل ببيانات حكومية توضيحية.

### 8.6 Arabic stress contract

اختبارات القبول تشمل:

- card title من 2 و3 و5 أسطر.
- entity name بطول 80–120 حرفًا.
- H1 بطول 140 حرفًا.
- وصف بثلاثة أضعاف fixture.
- URL أو كلمة طويلة غير قابلة للكسر.
- 320px viewport، 200% text و400% zoom.

لا ellipsis للمحتوى الحكومي الأساسي. line clamp يسمح لقائمة discovery فقط إذا كانت الوجهة تكشف النص كاملًا.

## 9. Data and implementation boundary

### 9.1 UI يمكن بناؤه بعد اعتماد العقد

- token layer والtypography recipes.
- Container/Section/Stack.
- Button/AppLink/IconButton.
- Field/Input/Select/Textarea.
- Alert/Badge.
- Breadcrumbs.
- AsyncSection/Empty/Error/Skeleton.
- GovernmentHeader/GovernmentFooter.
- ContentCard باستخدام legacy compatibility DTOs مع adapter صريح.
- Citizen engagement links وcomplaints flow الحالي بعد accessible wrapper.

### 9.2 UI يحتاج backend/data contract

| UI | dependency | الحالة |
|---|---|---|
| Search Hero الفعلي | Unified Search route/API/results model | HOLD |
| Service discovery | GovernmentService + categories | HOLD |
| Service Detail | GovernmentService detail/start/support contract | HOLD |
| Ministry full page | entity contract الموسع + content availability | HOLD/partial |
| Ministry services/leadership/contact | APIs غير موجودة | HOLD |
| Entity content | unified public read availability | HOLD بقرار Phase 2 |
| Open government | datasets/reports/projects APIs | HOLD |
| Data metrics | source/unit/asOf values | HOLD |

### 9.3 Presentation-only

- كل ما يحمل مثال أو توضيحي.
- وزارة الصحة المعروضة في Figma ما دامت غير موجودة في registry.
- facts والأهلية والمتطلبات والخطوات والرسوم والمدة في Service Detail.
- subordinate entities والقيادات وcontact facts غير الموثقة.
- site-data.ts وPortalHome hardcoded content.

Presentation fixtures يسمح بها فقط في tests وvisual regression، داخل مسار لا يستطيع production import الوصول إليه.

## 10. Inline-style migration contract

Baseline = 111 sites:

- Admin 49
- Complaints 21
- Contact 14
- Login 9
- Services 5
- About 5
- content detail pages 8 إجمالًا

الخطة:

1. لا bulk rewrite.
2. يمنع إضافة inline style ثابت جديد بعد اعتماد أول lint guard.
3. عند بناء primitive، يُرحل فقط consumers الداخلون في نفس slice.
4. Admin خارج نطاق الشاشات الست؛ تبقى 49 حالة حتى Admin phase مستقلة.
5. Contact/Complaints/Login لا تُسحب إلى refactor لمجرد تقليل الرقم.
6. في كل gate يسجل baseline count وnew count، ويمنع الارتفاع.
7. بعد انتقال آخر consumer، توضع legacy class في deprecated list ثم تحذف في slice منفصلة.

الترتيب المقترح للإزالة:

- content detail inline margins → PageHeader/ContentDetail recipe.
- Services static layout → Section/Card recipes عند حسم البيانات.
- Contact/Complaints/Login fields → Field primitives عندما تدخل هذه الصفحات scope.
- Admin أخيرًا في مشروع Administration UI مستقل.

## 11. Implementation batches and validation gates

### Batch 0 — Owner decisions

- اعتماد هذا العقد.
- اعتماد 1160px composition alias.
- اعتماد self-hosted Noto Sans Arabic.
- اعتماد CSS Modules + global tokens.
- اعتماد Mobile drawer behavior.
- اعتماد Accordion behavior أو قرار تأجيله.
- اعتماد Search pre-API policy.

Gate: كل قرار موثق؛ لا code.

### Batch 1 — Foundations adapter and test harness

- token subset اللازمة للدفعة.
- typography recipes/font delivery.
- base RTL/focus/reduced-motion.
- component test runner وPlaywright/axe/visual scaffold.

Gate:

- lint/build.
- token snapshot مقابل القيم في هذه الوثيقة.
- contrast checks.
- no production page visual change غير مقصود.
- no increase في inline-style count.

### Batch 2 — Shared primitives

- Button/AppLink/IconButton.
- Field/Input/Select/Textarea.
- Alert/Badge/Breadcrumbs.
- Async states.

Gate:

- unit/component tests لكل state.
- keyboard/ARIA/48px Mobile.
- RTL/LTR and extreme Arabic fixtures.
- visual snapshots مقابل Figma component states.

### Batch 3 — Public shell

- PublicLayout.
- GovernmentHeader/GovernmentFooter.
- MobileNavigation.
- skip link وroute focus.

Gate:

- desktop/mobile/tablet screenshots.
- keyboard-only menu test.
- Escape/focus trap/return focus.
- link audit: لا placeholder href.

### Batch 4 — Content composition

- ContentCard وContentDiscovery.
- view-model adapter للقوائم الأربع.
- section-scoped async states.

Gate:

- exact DTO/URL mapping.
- no site-data.ts production imports.
- partial failure tests.
- no regression في legacy detail routes.

### Batch 5 — Homepage supported shell

- تركيب الأقسام التي تملك بيانات/روابط حقيقية فقط.
- Search/Services/Open Government تبقى غير منشورة أو بحالة unavailable معتمدة.

Gate:

- full-page screenshots 1440 و360 وtablet review.
- accessibility/visual diff.
- content provenance audit.

### Batch 6 — Entity template

- لا يبدأ قبل data contract.
- أول route يستخدم entity موجودة فعليًا.
- sections غير المدعومة empty/omitted وفق قرار product، لا fixtures.

Gate:

- entity API contract tests.
- 200/404 by slug.
- child entities empty/data.
- source/update and partial-state review.

### Batch 7 — Service Detail

- **HOLD** حتى GovernmentService API وrecord موثوق.

Gate قبل فتح الدفعة:

- DTO/API approved.
- owning entity and canonical slug.
- eligibility/requirements/steps/startUrl/support/source.
- content owner approval.

## 12. Visual validation contract

- source goldens: docs/review/phase4.3/after.
- viewports: 1440 و360، إضافة 768 و1024 review-only.
- browser/font/timezone/locale/network fixtures ثابتة.
- animations disabled في visual runs.
- geometry diff منفصل عن text antialiasing.
- component-state snapshots تسبق full-page diff.
- semantic tests للheadings/landmarks/tab order منفصلة عن pixel diff.
- fixture data موسومة review-only ولا تدخل production bundle.
- 352px mobile export discrepancy لا تعالج بالstretch؛ إما crop موثق أو re-export read-only بعرض صحيح.

## 13. Owner approvals required

1. **CSS architecture:** global semantic tokens + CSS Modules للمكونات.
2. **Font:** self-host Noto Sans Arabic مع fallback مؤقت.
3. **Container:** اعتماد composition max = 1160px المشتق من Foundation 1200px.
4. **Mobile navigation:** modal drawer contract المذكور.
5. **Accordion:** multi-open وأول item مفتوح، أو تأجيل كامل.
6. **Search before API:** التوصية إخفاء الفعل غير الحقيقي؛ البديل disabled/unavailable state مع نص واضح.
7. **Fallback policy:** منع prototype fallback في الصفحات الجديدة، واستخدام section error/empty states.
8. **Code Connect:** اعتماد equivalent mapping document مؤقتًا؛ لا upgrade أو sync مطلوب قبل أول slices.
9. **Scope:** GO فقط لـBatch 1–4؛ Homepage conditional، Ministry partial HOLD، Service Detail HOLD.

## 14. Final mapping matrix

| Figma element | Existing code | Target component/token | Action | Data dependency | Implementation phase |
|---|---|---|---|---|---|
| Color Primitives | عشرة root colors متفرقة | private primitive subset | MODIFY | لا | Batch 1، حسب dependency closure |
| Color Semantic | غير ممثلة | public semantic color tokens | CREATE | لا | Batch 1 |
| Spacing | hardcoded values | 14 spacing tokens | CREATE | لا | Batch 1 |
| Radius | 10/12/16/24 ad hoc | six radius tokens | CREATE | لا | Batch 1 |
| Borders | colors/widths ad hoc | semantic border colors + 1px recipe + 3px focus | MODIFY | لا | Batch 1 |
| Typography Variables/Styles | Tahoma + ad hoc headings | font primitives + 15 recipes | MODIFY | font assets | Batch 1 |
| Elevation | shadow واحد كثيف | elevation 100/200/300 recipes | MODIFY | لا | Batch 1؛ 300 deferred |
| Layout/Grid | 1180px و980/520 | 1160 composition + 4/8/12 grid | MODIFY | owner approval | Batch 1 |
| Motion | transition عام + reduced motion | 9 motion tokens | MODIFY | لا | Batch 1 |
| Data Visualization | غير موجود | لا runtime tokens الآن | DEFER | sourced metrics | Future Open Government |
| Button/Primary | .button--primary أحمر | Button variant=primary | MODIFY | لا | Batch 2 |
| Button/Secondary | .button--secondary | Button variant=secondary | MODIFY | لا | Batch 2 |
| Link | Next Link + global anchor style | AppLink | MODIFY | valid href | Batch 2 |
| Icon Button/Primary | غير موجود | IconButton variant=primary | CREATE | icon asset | Batch 2 |
| Icon Button/Secondary | search summary | IconButton variant=secondary | CREATE | icon asset | Batch 2 |
| Input | native inline fields | Field + Input | MODIFY | لا | Batch 2 |
| Search | header form + local filter | SearchField UI | CREATE | integration يحتاج Unified Search | UI Batch 2؛ integration HOLD |
| Select | native inline select | Field + native Select | MODIFY | options | Batch 2 when consumer enters scope |
| Textarea | native inline textarea | Field + Textarea | MODIFY | لا | Batch 2 |
| Checkbox | لا consumer بالشاشات | Checkbox | DEFER | use case | Future |
| Radio | لا consumer بالشاشات | RadioGroup/Radio | DEFER | use case | Future |
| Switch | لا consumer بالشاشات | Switch | DEFER | use case | Future |
| Tabs/Item | لا tabs runtime | route navigation، لا Tabs | DEFER | entity subroutes | Entity phase |
| Breadcrumb/Item | غير موجود | Breadcrumbs | CREATE | route labels | Batch 2 |
| Pagination/Page Item | لا list pagination | Pagination | DEFER | paginated API/routes | Future lists |
| Pagination/Directional Item | غير موجود | جزء من Pagination | DEFER | paginated API/routes | Future lists |
| Accordion | details في Header فقط | Accordion disclosure | CREATE/DEFER | approved use case | Owner decision؛ Service HOLD |
| Alert/Notice | .notice | Alert | MODIFY | async state | Batch 2 |
| Badge/Status | .pill | Badge | MODIFY | status/type label | Batch 2 |
| Modal/Dialog | Admin fixed overlay | Dialog منفصل | DEFER | Admin scope | Administration phase |
| Tooltip | غير موجود | لا implementation | DEFER | justified use case | Future |
| Header/Brand | PMO brand markup | private GovernmentBrand | MODIFY | approved brand asset/copy | Batch 3 |
| Header/Utility Item | links داخل Header | private UtilityLink | CREATE | valid routes | Batch 3 |
| Navigation/Item | nav links | NavLink | MODIFY | route map | Batch 3 |
| Navigation/Menu Trigger | details summary | MenuTrigger button | CREATE | لا | Batch 3 |
| Footer/Legal Item | روابط محدودة | private FooterLink | CREATE | valid legal route | Batch 3 |
| Footer/Section | static Footer columns | FooterSection | MODIFY | approved footer content | Batch 3 |
| Service Card | generic info-card/static | ServiceCard | CREATE | GovernmentService API | HOLD |
| Government Entity Card | غير موجود | GovernmentEntityCard | CREATE | Entity API | Batch 4/6، records حقيقية فقط |
| News/Content Card | list-card loops | ContentCard | CREATE | compatibility content DTOs | Batch 4 |
| Data/Metric | hero stats | DataMetric | DEFER | value/unit/source/asOf | Future Open Government |
| Global Government Header Pattern | Header.tsx | GovernmentHeader | MODIFY | route/brand config | Batch 3 |
| Government Search & Discovery Pattern | Header search + Home filter | SearchHero composition | DEFER integration | Unified Search | Homepage conditional |
| Service Discovery Pattern | static cards/pills | Section + ServiceCard | DEFER | Services API/categories | HOLD |
| Government Entity Identity Pattern | غير موجود | EntityIdentity composition | CREATE later | entity data contract | Batch 6 HOLD/partial |
| Content Discovery Pattern | أربعة loops مختلفة | ContentDiscovery | CREATE | content adapters | Batch 4 |
| Open Government & Data Pattern | static copy | page composition فقط | DEFER | datasets/reports/projects | Future |
| Citizen Engagement Pattern | complaints/contact routes | plain section composition | MODIFY | routes حقيقية | Homepage conditional |
| Common Page Framework Pattern | SectionHeading جزئي | PublicLayout/Container/Section/Breadcrumbs/AsyncSection | CREATE | لا | Batch 1–3 |
| Government Footer Pattern | Footer.tsx | GovernmentFooter | MODIFY | approved links/contact | Batch 3 |
| Homepage Desktop/Mobile | page.tsx PMO composition | approved national composition | MODIFY | search/services/entity/content decisions | Batch 5 conditional |
| Ministry Desktop/Mobile | لا route | entity template | CREATE | expanded Entity/API content | Batch 6 HOLD/partial |
| Service Detail Desktop/Mobile | لا route | service detail template | CREATE | GovernmentService full contract | Batch 7 HOLD |
| site-data.ts fallback | production imports | review/test fixtures خارج graph | REMOVE-LATER | real APIs | مع كل migrated section |
| 111 inline styles | موزعة على 10 pages | component-scoped recipes | MODIFY تدريجي | لا | كل batch؛ Admin لاحقًا |

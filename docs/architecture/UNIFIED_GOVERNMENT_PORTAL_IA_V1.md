# المنصة الحكومية اليمنية الموحدة — Information Architecture & Domain Architecture V1

> **الحالة:** Approved — مرجع معماري معتمد  
> **تاريخ الاعتماد:** 2026-08-23  
> **نطاق الاعتماد:** IA، URL architecture، Domain Model، الحوكمة، وخارطة الانتقال. لا يُعد الاعتماد تفويضًا تلقائيًا بتنفيذ migrations أو redesign.  
> **تاريخ الجرد:** 2026-08-23  
> **النطاق:** IA، URL architecture، Domain Model، الحوكمة، وخارطة الانتقال  
> **خارج النطاق:** redesign، Figma، CSS، migrations، أو أي تغيير تنفيذي كبير

## وثائق المتابعة

- [سجل القرارات المعمارية](adr/README.md)
- [خطة تنفيذ المرحلة الأولى](PHASE_1_IMPLEMENTATION_PLAN.md)

## 1. القرار المعماري المقترح

يجب إعادة تعريف المنتج من «موقع رئاسة مجلس الوزراء» إلى **بوابة وطنية موحدة** تعمل فوق منصة مشتركة متعددة الجهات. رئاسة مجلس الوزراء تصبح `GovernmentEntity` من النوع `PRIME_MINISTERS_OFFICE` داخل الدليل الحكومي، وليست هي الـtenant الوحيد أو مالك كل محتوى المنصة.

النهج المقترح هو:

1. الاستمرار كـ **Modular Monolith** في المرحلة الحالية؛ لا توجد حاجة عملية الآن إلى microservices.
2. استخدام قاعدة بيانات مشتركة مع **entity-scoped tenancy**؛ تمثل `GovernmentEntity` حد العزل الإداري، بينما تمثل صلاحيات المنصة النطاق المركزي.
3. إنشاء سجل مركزي موحد للجهات، الخدمات، والمحتوى؛ صفحات الوزارات ليست تطبيقات مستقلة، بل قوالب ديناميكية تقرأ من النموذج نفسه.
4. إعطاء كل سجل معرفًا داخليًا ثابتًا، و`slug` عامًا، و`governmentEntityId` أو علاقات جهات صريحة.
5. فصل المحتوى عن موضع عرضه. الخبر الواحد يُخزن مرة واحدة ويظهر في الصفحة الوطنية وصفحة الجهة والبحث عبر الاستعلامات، لا عبر النسخ.
6. جعل الانتقال additive وتدريجيًا، مع الإبقاء على المسارات والوظائف الحالية عبر adapters وredirects حتى اكتمال الترحيل.

## 2. جرد المستودع الحالي

### 2.1 البنية المتوفرة

| الطبقة | الموجود حاليًا | التقييم بالنسبة للرؤية الوطنية |
|---|---|---|
| Frontend | Next.js App Router، TypeScript، RTL، صفحات رئيسية وخدمات وتواصل وشكاوى وإدارة | قابل لإعادة الاستخدام كـshell، لكنه PMO-specific ومعظم الصفحات ثابتة |
| Backend | Spring Boot modular Maven monolith | اتجاه مناسب، لكن عددًا من الوحدات مجرد POM بلا domain implementation |
| Identity | `User`, `Role`, `Permission` وJWT وmethod security | أساس RBAC صالح، لكنه global وغير مرتبط بجهة |
| Content admin | جدول `admin_content` وCRUD وحالات بسيطة | prototype إداري، وليس مخزن المحتوى العام ولا يملك body/revisions/entity ownership |
| Public content | News/Announcements/Decisions/Documents endpoints | بيانات ثابتة داخل services وليست persistence-backed |
| Citizen contact | `support_requests` وإنشاء الطلب وتحديث الحالة | نواة قابلة للتوسيع، لكنها inbox عالمي بلا routing أو tracking code أو entity scope |
| Portal composition | `/api/portal/home` | aggregate مفيد، لكنه hardcoded ومتمحور حول PMO |
| Database | PostgreSQL + Flyway + جداول الهوية والمحتوى الإداري والطلبات | أساس تشغيلي جيد، لكن لا يوجد model للجهات والخدمات والمحتوى المركزي |
| Modules | `organization`, `workflow`, `notifications`, `reporting`, `shared` | حدود أسماء مفيدة، لكنها فارغة حاليًا ولا تحقق المتطلبات بعد |
| Deployment | Docker Compose، Render، Netlify، Neon | مناسب للتجارب والـstaging، وليس architecture تشغيل حكومي نهائي |

### 2.2 مصادر الحقيقة الحالية

توجد اليوم ثلاثة مصادر منفصلة للمحتوى:

1. بيانات fallback داخل `frontend/src/lib/site-data.ts`.
2. قوائم ثابتة داخل services في وحدات `news`, `decisions`, و`documents`.
3. سجلات مستقلة في جدول `admin_content` لا تغذي صفحات المحتوى العام.

هذا يعني أن الإدارة الحالية لا تدير المحتوى الذي يراه الجمهور فعليًا. أول مبدأ في الترحيل يجب أن يكون **مصدر حقيقة واحد لكل record**.

### 2.3 فجوة الصلاحيات الحالية

- الدور `PMO_ADMIN` والصلاحيات مثل `content.write` صلاحيات عالمية.
- لا توجد عضوية أو assignment تربط المستخدم بجهة حكومية.
- `ContentItem` و`SupportRequest` لا يحتويان `governmentEntityId`.
- من يملك `content.write` يستطيع نظريًا تعديل أي سجل يديره الـendpoint نفسه.
- JWT يحتوي roles وpermissions فقط، ولا يمثل entity scope.
- لا يوجد audit log للعمليات الإدارية أو انتقالات النشر.

### 2.4 فجوة workflow

الحالة مخزنة كنص حر في `admin_content.status`، ويقوم controller بتحويل بعض التسميات العربية والإنجليزية. لا توجد:

- transitions مقيدة؛
- مراجعة أو موافقة مسجلة؛
- revision history؛
- separation of duties؛
- scheduled publication؛
- سجل من قام بالفعل ومتى ولماذا.

### 2.5 فجوة البحث

البحث الحالي في الصفحة الرئيسية يجمع العناصر التي تم تحميلها للصفحة فقط. لا يوجد index أو API بحث موحد أو filters أو pagination أو دعم للجهات والخدمات والبيانات.

## 3. مبادئ IA الحاكمة

1. **National first:** الصفحة الرئيسية وطنية، ولا تُبنى حول جهة واحدة.
2. **User needs before bureaucracy:** الخدمات تُكتشف حسب الاحتياج، مع بقاء الاستكشاف حسب الجهة.
3. **One entity, one reusable template:** لا تطبيق منفصل لكل وزارة.
4. **One content record, many placements:** العرض الوطني والجهوي والبحث projections للسجل نفسه.
5. **Stable identifiers, readable URLs:** معرف داخلي ثابت وslug مقروء مع redirects عند تغييره.
6. **Explicit ownership:** كل خدمة ومحتوى وطلب يملك جهة مسؤولة واضحة.
7. **Scoped administration by default:** المنع هو الأصل خارج نطاق الجهة.
8. **Workflow is a domain rule:** لا يُختزل في حقل status نصي.
9. **Arabic first, localization ready:** العربية هي اللغة الأساسية، دون جعل النموذج غير قابل للترجمة.
10. **Backward-compatible migration:** لا حذف للمسارات الحالية قبل وجود replacement وredirect وقياس استخدام.

## 4. Top-level navigation المقترحة

التنقل العام V1:

1. **الرئيسية** `/`
2. **الخدمات الحكومية** `/services`
3. **دليل الجهات الحكومية** `/government`
4. **الأخبار والمحتوى** `/content`
5. **الحكومة المفتوحة** `/open-government`
6. **المشاركة المجتمعية** `/participation`

عناصر utility منفصلة عن التنقل الرئيسي:

- البحث الموحد `/search`
- تتبع طلب `/requests/track`
- اللغة مستقبلًا
- دخول الإدارة `/admin/login`، ولا يظهر كعنصر أساسي للمواطن

لا تكون «رئاسة مجلس الوزراء» عنصر الهوية الوحيد للـHeader الوطني. تظهر كجهة داخل الدليل، ويمكن إبرازها تحريريًا دون كسر النموذج الموحد.

## 5. Sitemap V1

### 5.1 الصفحة الوطنية

- `/` الصفحة الرئيسية الوطنية
  - بحث موحد بارز
  - خدمات شائعة وحسب الاحتياج
  - Life events مميزة عند توفرها
  - الجهات الحكومية
  - آخر الأخبار والإعلانات والقرارات من جميع الجهات
  - مؤشرات وبيانات مفتوحة مختارة
  - مشاريع حكومية مختارة
  - المشاركة المجتمعية وتتبع الطلبات
  - تنبيهات وطنية عند الحاجة
- `/about-platform` عن المنصة الحكومية الموحدة
- `/accessibility` بيان إمكانية الوصول
- `/privacy` الخصوصية وحماية البيانات
- `/terms` شروط الاستخدام
- `/contact` التواصل مع فريق المنصة، لا بديلًا عن طلبات الجهات

### 5.2 دليل الحكومة

- `/government` دليل الجهات الحكومية
  - بحث وتصفية حسب النوع والقطاع والموقع
  - رئاسة مجلس الوزراء
  - الوزارات
  - الهيئات والمؤسسات
  - الجهات المستقلة
  - المحافظات والسلطات المحلية
- `/prime-ministers-office` صفحة رئاسة مجلس الوزراء
- `/ministries` دليل الوزارات
- `/ministries/{entitySlug}` صفحة وزارة
- `/authorities` دليل الهيئات والمؤسسات
- `/authorities/{entitySlug}` صفحة هيئة أو مؤسسة
- `/independent-entities` دليل الجهات المستقلة
- `/independent-entities/{entitySlug}` صفحة جهة مستقلة
- `/governorates` دليل المحافظات
- `/governorates/{entitySlug}` صفحة محافظة أو سلطة محلية

القالب الموحد لأي جهة:

- `/{entityType}/{entitySlug}` Overview
- `/{entityType}/{entitySlug}/about`
- `/{entityType}/{entitySlug}/leadership`
- `/{entityType}/{entitySlug}/services`
- `/{entityType}/{entitySlug}/news`
- `/{entityType}/{entitySlug}/decisions`
- `/{entityType}/{entitySlug}/documents`
- `/{entityType}/{entitySlug}/data`
- `/{entityType}/{entitySlug}/subordinate-entities`
- `/{entityType}/{entitySlug}/contact`

كل هذه المسارات تُنفذ بقالب route موحد وresolver للجهة، لا بمجلد تطبيق أو codebase مستقل لكل جهة.

### 5.3 الخدمات الحكومية

- `/services` دليل الخدمات
  - البحث والتصفية
  - حسب احتياج المستخدم
  - حسب الجهة
  - حسب المستفيد: أفراد، أعمال، حكومة
  - حسب قناة التقديم: إلكترونية، حضورية، مختلطة
- `/services/{serviceSlug}` صفحة الخدمة
  - الوصف والجهة المالكة
  - الأهلية والمتطلبات
  - الرسوم والمدة
  - الخطوات والمستندات
  - قنوات التقديم
  - رابط «ابدأ الخدمة» أو موقع تقديم خارجي موثوق
  - الدعم والأسئلة الشائعة
- `/services/categories`
- `/services/categories/{categorySlug}` مثل:
  - `identity-documents`
  - `travel`
  - `health`
  - `education`
  - `employment`
  - `business`
  - `investment`
  - `transport`
  - `justice`
  - `housing`
  - `social-protection`
  - `agriculture`
- `/services/by-entity`
- `/services/by-entity/{entitySlug}`
- `/life-events` جاهز للتفعيل المرحلي
- `/life-events/{lifeEventSlug}`، مثل بدء عمل تجاري أو ولادة طفل أو السفر

`LifeEvent` لا ينسخ الخدمات؛ يرتب references لخدمات من عدة جهات ضمن journey واحدة.

### 5.4 المحتوى الحكومي المركزي

- `/content` مركز المحتوى
- `/news` الأخبار
- `/news/{contentSlug}`
- `/announcements` الإعلانات
- `/announcements/{contentSlug}`
- `/decisions` القرارات والتعاميم
- `/decisions/{contentSlug}`
- `/documents` مكتبة الوثائق
- `/documents/{contentSlug}`
- `/reports` التقارير
- `/reports/{contentSlug}`
- `/policies` السياسات
- `/policies/{contentSlug}`
- `/regulations` اللوائح والتشريعات
- `/regulations/{contentSlug}`
- `/media` المركز الإعلامي

صفحات القوائم تدعم filters مثل `entity`, `type`, `category`, `dateFrom`, و`dateTo`. صفحة الجهة تستعلم عن السجلات نفسها باستخدام `governmentEntityId`، ولا تنشئ نسخة منها.

### 5.5 الحكومة المفتوحة

- `/open-government` بوابة الحكومة المفتوحة
- `/open-data` فهرس البيانات المفتوحة
- `/open-data/datasets/{datasetSlug}`
- `/statistics` المؤشرات والإحصاءات
- `/statistics/{indicatorSlug}`
- `/reports` التقارير الحكومية
- `/projects` المشاريع الحكومية
- `/projects/{projectSlug}`
- `/budgets` الموازنات
- `/budgets/{budgetSlug}`
- `/policies-and-regulations` السياسات واللوائح
- `/apis` دليل واجهات البرمجة
- `/apis/{apiSlug}`

V1 لا يلزم أن ينفذ كل الأنواع كأنظمة مستقلة. يمكن لبعضها البدء كـcontent subtype منظم، بشرط عدم فقد حقول النطاق الأساسية وإمكانية الفصل لاحقًا.

### 5.6 المشاركة المجتمعية

- `/participation` مركز المشاركة
- `/complaints` تقديم شكوى
- `/reports/submit` تقديم بلاغ؛ يجب التمييز عن التقارير المنشورة
- `/suggestions` تقديم اقتراح
- `/inquiries` تقديم استفسار
- `/surveys` الاستبيانات
- `/surveys/{surveySlug}`
- `/requests/track` تتبع الطلب برمز عام غير قابل للتخمين
- `/requests/{publicTrackingCode}` حالة الطلب بعد تحقق مناسب
- `/participation/how-it-works`
- `/participation/service-standards`

يمكن أن تشترك النماذج في `EngagementCase` واحد مع `caseType` مختلف، مع routing للجهة المختصة وSLA وحالة وسجل مراسلات.

### 5.7 الإدارة

- `/admin/login`
- `/admin` لوحة حسب نطاق المستخدم
- `/admin/platform`
  - إعدادات المنصة
  - taxonomy الوطنية
  - إدارة الجهات
  - الأدوار والسياسات المركزية
  - البحث والفهرسة
  - سجل التدقيق
- `/admin/entities`
- `/admin/entities/{entitySlug}` سياق جهة محددة
  - `overview`
  - `profile`
  - `leadership`
  - `services`
  - `content`
  - `documents`
  - `data`
  - `engagement`
  - `workflow`
  - `users`
  - `settings`
- `/admin/content/{contentId}` محرر المحتوى
- `/admin/review-queue`
- `/admin/publishing-calendar`
- `/admin/audit`

يجب ألا يعتمد authorization على إخفاء روابط UI. كل query وcommand في الـbackend يتحقق من نطاق الجهة.

## 6. URL Architecture

### 6.1 قواعد عامة

- المسارات العامة lowercase وkebab-case وبالإنجليزية التقنية المستقرة، بينما labels عربية.
- الـslug ليس المفتاح الأساسي؛ قاعدة البيانات تستخدم UUID أو ID ثابتًا، والـslug قابل للتغيير مع redirect history.
- الصفحات العامة تستخدم slug؛ APIs الإدارية تفضل IDs الثابتة.
- لا تُضمّن أسماء الوحدات البرمجية أو `pmo` في كل مسار وطني.
- تعتمد canonical URLs، ويُحتفظ بالمسارات القديمة عبر redirects أو adapters.
- مسارات القوائم تستخدم query parameters للتصفية، لا تولد تركيبات path غير محدودة.
- جميع APIs الجديدة تبدأ بـ`/api/v1`.

### 6.2 أمثلة عامة

```text
/ministries/health
/ministries/health/services
/services/passport-renewal
/services?entity=interior&category=identity-documents
/news/cabinet-approves-digital-services-framework
/news?entity=health&dateFrom=2026-01-01
/open-data/datasets/health-facilities
/requests/track
```

### 6.3 أمثلة API V1

```text
GET  /api/v1/entities
GET  /api/v1/entities/{entityId}
GET  /api/v1/entities/{entityId}/children

GET  /api/v1/services
GET  /api/v1/services/{serviceId}
GET  /api/v1/service-categories
GET  /api/v1/life-events/{lifeEventId}

GET  /api/v1/content?type=NEWS&entityId={id}&status=PUBLISHED
GET  /api/v1/content/{contentId}
POST /api/v1/admin/entities/{entityId}/content
POST /api/v1/admin/content/{contentId}/transitions

GET  /api/v1/open-data/datasets
GET  /api/v1/search?q={query}&types=entity,service,news,decision,document,dataset

POST /api/v1/engagement/cases
GET  /api/v1/engagement/cases/{trackingCode}/status
```

### 6.4 توافق المسارات الحالية

| الحالي | الانتقال المقترح |
|---|---|
| `/about` | يبقى مؤقتًا ثم redirect إلى `/prime-ministers-office/about` |
| `/services` | يبقى ويصبح دليل الخدمات الوطني |
| `/complaints` | يبقى كواجهة مختصرة لنوع `COMPLAINT` |
| `/contact` | يصبح تواصل المنصة، مع توجيه طلبات الجهات إلى صفحاتها |
| `/news/{id}` | adapter ثم redirect دائم إلى `/news/{slug}` عند توفر slug |
| `/decisions/{id}` | adapter ثم redirect إلى `/decisions/{slug}` |
| `/documents/{id}` | adapter ثم redirect إلى `/documents/{slug}` |
| `/admin` | يبقى كبوابة دخول ثم يوجه للسياق المسموح |
| `/api/news` وما شابه | compatibility facade فوق `/api/v1/content` خلال فترة الترحيل |

## 7. Domain Model المقترح

### 7.1 Government Directory bounded context

#### `GovernmentEntity`

الكيان المركزي للـtenancy والعرض.

حقول أساسية مقترحة:

- `id`
- `entityTypeId`
- `parentEntityId` عند وجود parent مباشر
- `officialNameAr`, `shortNameAr`
- حقول ترجمة مستقبلية أو relation للترجمات
- `slug`, `slugHistory`
- `status`: `DRAFT`, `ACTIVE`, `INACTIVE`, `ARCHIVED`
- `mandate`, `description`, `logoAssetId`
- `websiteUrl`, قنوات الاتصال، العنوان
- `jurisdictionType`, `governorateId` عند الحاجة
- `sortOrder`, `establishedAt`

#### `EntityType`

قيم مُدارة مثل PMO، Ministry، Authority، Public Institution، Independent Entity، Governorate، Local Authority.

#### `EntityRelationship`

يمثل العلاقات التي لا يكفيها `parentEntityId`:

- `PARENT_OF`
- `SUBORDINATE_TO`
- `OVERSEEN_BY`
- `AFFILIATED_WITH`
- `SERVES_JURISDICTION`

#### `Person` و`LeadershipAssignment`

يفصل الشخص عن منصبه، ويدعم تاريخ بداية ونهاية التكليف، المسمى، الترتيب، وحالة النشر.

### 7.2 Service Catalog bounded context

#### `GovernmentService`

- `id`, `slug`, `name`, `summary`, `description`
- `owningEntityId`
- `status`: Draft/Review/Approved/Published/Archived
- `beneficiaryType`
- `deliveryMode`
- `requirements`, `steps`, `fees`, `processingTime`
- `startUrl` أو channel configuration
- `serviceLevel`, `supportContact`

#### `ServiceEntityRole`

علاقة many-to-many بين الخدمة والجهات مع role مثل `OWNER`, `PROVIDER`, `REGULATOR`, `SUPPORT`.

#### `ServiceCategory`

taxonomy وطنية هرمية. ترتبط الخدمة بعدة تصنيفات، مع تصنيف أساسي اختياري.

#### `LifeEvent`, `LifeEventStep`

يجمع خدمات موجودة من عدة جهات بترتيب وإرشاد سياقي، دون نسخ بيانات الخدمة.

#### `ServiceChannel`

قنوات إلكترونية أو حضورية أو هاتفية، مع URLs ومواقع وساعات وشروط.

### 7.3 Government Content bounded context

#### `ContentItem`

السجل المركزي المشترك:

- `id`, `contentType`, `slug`
- `primaryGovernmentEntityId`
- `title`, `summary`, `body`
- `status`
- `locale`
- `publishedAt`, `scheduledAt`, `archivedAt`
- `createdBy`, `updatedBy`
- `currentRevisionId`
- metadata خاصة بالنوع عند الحاجة

`contentType` يشمل News، Announcement، Decision، Document، Report، Policy، Regulation، Statement، Media، وPage.

#### `ContentEntityLink`

يحافظ على `primaryGovernmentEntityId` ويضيف جهات مشتركة أو مذكورة بأدوار مثل `CO_PUBLISHER`, `SUBJECT`, `RELATED`. هذا يدعم المحتوى المشترك دون duplication.

#### `ContentRevision`

نسخة immutable من الحقول التحريرية لكل حفظ مهم، مع المؤلف والتاريخ ورسالة التغيير.

#### `ContentTaxonomyLink`, `TaxonomyTerm`

تصنيفات ووسوم مركزية قابلة لإعادة الاستخدام.

#### `MediaAsset`, `Attachment`

metadata للملفات، التخزين، النوع، الحجم، checksum، accessibility text، security scan status، ونسخ العرض.

#### `SlugRedirect`

يحفظ redirects عند تغيير slugs ويحمي الروابط القديمة.

### 7.4 Workflow bounded context

#### `WorkflowDefinition`

يعرف المسار المطبق على نوع المورد أو الجهة.

#### `WorkflowInstance`

يربط resource محددًا بالمرحلة الحالية.

#### `WorkflowTransition`

يسجل `fromState`, `toState`, actor، timestamp، comment، والقرار.

#### `Approval`

يسجل المراجع/المعتمد ونتيجة الموافقة، ويدعم separation of duties.

المسار الأساسي:

```text
DRAFT -> IN_REVIEW -> APPROVED -> PUBLISHED -> ARCHIVED
```

المسارات الإضافية المسموحة تشمل `IN_REVIEW -> DRAFT` لإعادة العمل، و`APPROVED -> IN_REVIEW` عند سحب الموافقة قبل النشر. لا يسمح بالانتقال المباشر إلى `PUBLISHED` إلا لصلاحية وسياسة صريحتين.

### 7.5 Identity, tenancy, and authorization

يمكن إعادة استخدام `User`, `Role`, و`Permission`، مع refactor للعلاقات.

#### `RoleAssignment`

- `userId`
- `roleId`
- `scopeType`: `PLATFORM` أو `ENTITY`
- `governmentEntityId` عند scope من نوع Entity
- `validFrom`, `validUntil`, `enabled`

لا يكفي ربط المستخدم بالدور عالميًا كما في `user_roles` الحالية.

#### الأدوار المستهدفة

| الدور | النطاق الافتراضي | المسؤوليات |
|---|---|---|
| Platform Super Admin | Platform | إدارة المنصة والجهات والسياسات المركزية؛ لا يستخدم للنشر اليومي |
| PMO Admin | PMO Entity، وقد تمنح له صلاحيات مركزية منفصلة | إدارة ملف ومحتوى PMO |
| Entity Admin | Entity | إدارة مستخدمي وإعدادات جهته ضمن الحدود المسموحة |
| Editor | Entity | إنشاء وتعديل المسودات |
| Reviewer | Entity | المراجعة وإعادة العمل |
| Publisher | Entity أو Platform | الاعتماد والنشر حسب السياسة |
| Service Manager | Entity | إدارة كتالوج الخدمات الخاص بالجهة |

قاعدة authorization:

```text
ALLOW إذا امتلك المستخدم permission المطلوبة
AND كان resource.entityId ضمن Entity scope للمستخدم
OR امتلك assignment مركزيًا من نوع PLATFORM يسمح بالفعل نفسه.
```

يجب تطبيق هذه القاعدة في service/policy layer وفي repository queries، لا في الواجهة فقط. عند وجود محتوى متعدد الجهات، تظل جهة أساسية مسؤولة، وتُعرّف سياسة واضحة للموافقة المشتركة.

#### `AuditEvent`

سجل append-only للعمليات الحساسة: تسجيل الدخول، إنشاء وتعديل وحذف، تغيير الصلاحيات، workflow transitions، النشر، تصدير البيانات، وتغيير إعدادات الجهة.

### 7.6 Citizen Engagement bounded context

#### `EngagementCase`

- `id`, `publicTrackingCode`
- `caseType`: Complaint, Report, Suggestion, Inquiry
- `assignedEntityId`, `assignedUnitId`
- `subject`, `description`, بيانات التواصل وفق سياسة الخصوصية
- `status`, `priority`, `slaDueAt`
- `createdAt`, `resolvedAt`

#### `CaseMessage`, `CaseAttachment`, `CaseAssignment`, `CaseStatusHistory`

توفر المراسلات، الملفات، التوجيه بين الجهات، والتاريخ الكامل دون الكتابة فوق الحالة السابقة.

#### `Survey`, `SurveyQuestion`, `SurveyResponse`

نموذج منفصل عن الحالات لأنه يمثل مشاركة جماعية، لا طلب خدمة فرديًا.

### 7.7 Open Government bounded context

#### `Dataset`, `DatasetDistribution`

يشمل المالك، الترخيص، التحديث، التغطية، الجودة، والملفات أو API distributions.

#### `StatisticIndicator`, `StatisticObservation`

يفصل تعريف المؤشر عن القيم عبر الزمن والموقع.

#### `GovernmentProject`

الجهة المالكة، الحالة، الموقع، الميزانية، المدة، نسب الإنجاز، ومصادر التحقق.

#### `BudgetRecord`

السنة المالية، الجهة، النوع، المبلغ، العملة، التصنيف، والوثائق المصدرية.

#### `PolicyRegulation`

يمكن أن يبدأ كـContentItem متخصص، مع رقم مرجعي، سلطة الإصدار، تاريخ النفاذ، والحالة القانونية.

#### `ApiProduct`

المالك، الغرض، base URL، documentation، version، access policy، وSLA.

### 7.8 Unified Search

#### `SearchDocument` projection

ليس مصدر الحقيقة. يُشتق من الجهات والخدمات والمحتوى والبيانات، ويحتوي:

- `resourceType`, `resourceId`, `entityIds`
- `title`, `summary`, النص القابل للبحث
- `url`, `locale`, `publishedAt`
- facets مثل category, entityType, serviceCategory, date

في المرحلة الأولى يمكن استخدام PostgreSQL full-text/trigram عبر query service. عند الحاجة التشغيلية يمكن استبدال projection بمحرك بحث خارجي دون تغيير الـpublic API.

## 8. علاقات النموذج الأساسية

| المصدر | العلاقة | الهدف |
|---|---|---|
| GovernmentEntity | has many | GovernmentService |
| GovernmentEntity | has many | ContentItem كجهة أساسية |
| GovernmentEntity | relates to many | GovernmentEntity عبر EntityRelationship |
| GovernmentEntity | has many | LeadershipAssignment |
| GovernmentService | belongs to many | ServiceCategory |
| GovernmentService | relates to many | GovernmentEntity عبر ServiceEntityRole |
| LifeEvent | contains ordered references to | GovernmentService |
| ContentItem | has many | ContentRevision |
| ContentItem | relates to many | GovernmentEntity عبر ContentEntityLink |
| ContentItem | has many | Attachment وTaxonomyTerm |
| User | has scoped roles through | RoleAssignment |
| RoleAssignment | optionally scoped to | GovernmentEntity |
| WorkflowInstance | governs one | ContentItem أو Service أو Dataset |
| EngagementCase | assigned to | GovernmentEntity |
| Dataset/Project/Budget | owned by | GovernmentEntity |
| SearchDocument | projects | أي resource منشور قابل للبحث |

## 9. ما يمكن إعادة استخدامه وما يحتاج refactor

### 9.1 إعادة استخدام مباشرة أو مع تعديلات محدودة

- Next.js App Router والـRTL والـlayout العام.
- Spring Boot modular monolith وMaven multi-module.
- PostgreSQL وFlyway كأساس للمخطط التدريجي.
- `User`, `Role`, `Permission` كأسماء ومفاهيم أساسية.
- method-level security وJWT كبداية، مع إضافة scope policy.
- `SupportRequest` كبيانات مصدر أولي لـ`EngagementCase`.
- وحدات `organization`, `workflow`, `notifications`, و`reporting` كحدود مستقبلية داخل المونوليث.
- Controllers الحالية للمحتوى كـcompatibility facades مؤقتة.
- اختبارات controllers والهوية كبنية اختبار يمكن توسيعها.
- Header/Footer/components الحالية كـimplementation assets لاحقة؛ لا تغيّر في مرحلة اعتماد IA.

### 9.2 يحتاج refactor

- تغيير الهوية النصية والـmetadata من PMO إلى المنصة الوطنية، مع تمثيل PMO كجهة.
- تحويل `organization` من module فارغ إلى Government Directory bounded context.
- استبدال `admin_content` بنموذج محتوى فعلي أو ترحيله إليه؛ الجدول الحالي لا يكفي للنشر.
- توحيد News/Announcement/Decision/Document تحت Content domain مع typed views.
- تحويل القوائم الثابتة في backend وfrontend إلى seeds/adapters مؤقتة ثم persistence.
- تحويل `/api/portal/home` إلى composition/query service يعتمد على البيانات المنشورة.
- استبدال status النصي بـenums وسياسة transitions.
- استبدال author النصي بـ`createdByUserId` وrevision attribution.
- توسيع support requests إلى cases موجهة لجهات وقابلة للتتبع.
- تعديل user-role model إلى scoped role assignments.
- نقل البحث من filtering لمحتوى الصفحة إلى API موحد.
- versioning للـAPI الجديدة مع إبقاء endpoints القديمة مؤقتًا.

### 9.3 مفقود بالكامل

- Government Entity registry والعلاقات التنظيمية.
- Service catalog، service taxonomy، life events.
- Entity-scoped administration وdata isolation.
- Content revisions، approvals، publication scheduling، وaudit trail.
- Unified search index/query layer.
- Open Data, statistics, projects, budgets, policies, APIs domains.
- File/object storage strategy وmalware scanning للوثائق.
- Public tracking الآمن للطلبات وSLA/routing.
- Localization model وslug redirects.
- Notifications الفعلية.
- Data retention، classification، consent، privacy، وrecords management policies.
- Observability وanalytics على مستوى الجهة.

## 10. مخاطر التوسع في البنية الحالية

| الخطورة | الخطر | الأثر عند إضافة عشرات الجهات | المعالجة المعمارية |
|---|---|---|---|
| حرجة | غياب `governmentEntityId` | لا يمكن عزل الإدارة أو الفلترة أو الملكية | GovernmentEntity + scoped ownership قبل توسيع الإدارة |
| حرجة | الأدوار عالمية | مستخدم وزارة قد يعدل محتوى وزارة أخرى | RoleAssignment scoped + policy enforcement |
| حرجة | ثلاثة مصادر للمحتوى | duplication وتضارب ما يراه المواطن وما تديره الإدارة | Unified Content source + adapters |
| عالية | public content hardcoded | كل جهة جديدة تتطلب code deployment | persistence-backed domains وقوالب ديناميكية |
| عالية | `admin_content` shallow | لا body ولا entity ولا revision ولا publish metadata | ContentItem/Revision/Workflow |
| عالية | لا audit trail | ضعف المساءلة والتحقيق في النشر الحكومي | append-only AuditEvent |
| عالية | workflow نصي حر | تجاوز المراجعة وتباين الحالات | state machine + transition policy |
| عالية | support inbox عالمي | كشف بيانات مواطنين لجهات غير مختصة | case assignment + entity-scoped queries |
| عالية | لا API versioning | صعوبة التطوير دون كسر الواجهة الحالية | `/api/v1` وcompatibility facade |
| متوسطة | مسارات رقمية فقط | روابط غير مستقرة وضعف SEO | slugs + canonical + redirect history |
| متوسطة | PMO branding في root | يخلط هوية المنصة بهوية جهة | national shell + PMO entity page |
| متوسطة | modules فارغة كثيرة | إحساس زائف بأن القدرات منفذة | تعريف ownership وdeliverables لكل module |
| متوسطة | البحث داخل homepage فقط | نتائج ناقصة وغير قابلة للفلترة | unified search projection/API |
| متوسطة | DTO لكل نوع بلا core contract | تكرار mapping والحقول | shared content read model دون coupling زائد |
| متوسطة | بيانات وتواريخ ممثلة كنصوص | صعوبة الفرز والتصفية والتوطين | أنواع زمنية وenums وlocalized presentation |

## 11. Migration Roadmap تدريجية

### المرحلة 0 — الاعتماد والقرارات

لا production implementation.

المخرجات:

- اعتماد Sitemap وURL conventions.
- اعتماد GovernmentEntity كحد tenancy.
- اعتماد unified content strategy.
- اعتماد scoped authorization rule.
- تحديد taxonomy owners وسياسة slugs واللغات.
- ADRs للـmodular monolith، IDs، files، search، وworkflow.

شرط الخروج: موافقة product/architecture/security/content governance.

### المرحلة 1 — Platform foundation بصورة additive

المخرجات المستقبلية بعد الموافقة:

- Government Directory domain والجداول الجديدة.
- seed لرئاسة مجلس الوزراء كأول `GovernmentEntity`.
- EntityType وEntityRelationship.
- RoleAssignment scoped مع إبقاء `user_roles` للتوافق المؤقت.
- authorization policy واختبارات منع cross-entity access.
- `/api/v1/entities` read APIs.

لا تُحذف الصفحات أو الجداول الحالية.

شرط الخروج: جهة ثانية تجريبية يمكن إضافتها دون نسخ كود، ومستخدمها لا يستطيع قراءة/تعديل إدارة PMO.

### المرحلة 2 — توحيد المحتوى

- إنشاء ContentItem/Revision/Taxonomy/Attachment/Workflow records.
- backfill سجلات `admin_content` وربطها بجهة PMO.
- تحويل البيانات الثابتة إلى seeds أو content records.
- جعل endpoints القديمة adapters فوق المحتوى الموحد.
- إبقاء `/news/{id}` وغيرها فعالة مع canonical slug redirects.
- جعل الصفحة الرئيسية query projection، لا مصدر بيانات مستقلًا.

شرط الخروج: تعديل خبر من الإدارة ينعكس على الصفحة الوطنية وصفحة الجهة والبحث من السجل نفسه.

### المرحلة 3 — دليل الجهات والقالب الموحد

- `/government` وقوائم الأنواع.
- route template موحد للجهات والأقسام المطلوبة.
- نقل `/about` الحالية إلى صفحة PMO مع redirect.
- إضافة عدة جهات تجريبية من البيانات فقط.

شرط الخروج: إضافة وزارة لا تتطلب إنشاء App أو routes مخصصة.

### المرحلة 4 — Service Catalog

- GovernmentService وServiceCategory وServiceEntityRole.
- البحث حسب الاحتياج والجهة والمستفيد.
- ترحيل بطاقات الخدمات الحالية إلى catalog.
- LifeEvent read model أولي دون محرك orchestration.

شرط الخروج: خدمة واحدة تظهر في الدليل الوطني وصفحة الجهة والتصنيف من record واحد.

### المرحلة 5 — Workflow والإدارة متعددة الجهات

- Draft → Review → Approval → Publish → Archive.
- review queues وpublishing calendar.
- scoped dashboards لكل جهة.
- audit events وseparation of duties.
- Service Manager capabilities.

شرط الخروج: اختبارات authorization/workflow تثبت عدم النشر أو التعديل خارج النطاق.

### المرحلة 6 — Citizen Engagement

- ترحيل `support_requests` إلى EngagementCase أو ربطه به.
- routing حسب الجهة والنوع.
- public tracking code، status history، SLA، messages، attachments.
- surveys كمسار مستقل.

شرط الخروج: الطلب يصل فقط لصندوق الجهة المختصة ويمكن تتبعه دون كشف PII.

### المرحلة 7 — Open Government

- Dataset/Distribution ثم Statistics/Projects/Budgets حسب الأولوية.
- metadata quality rules، licensing، provenance، وتحديثات.
- APIs catalog.

شرط الخروج: كل مورد مفتوح يملك جهة مالكة ومصدرًا وتاريخ تحديث وترخيصًا.

### المرحلة 8 — Unified Search

- Search projection من المصادر المعتمدة فقط.
- facets للنوع والجهة والتصنيف والتاريخ.
- PostgreSQL search أولًا، ثم محرك خارجي فقط إذا أثبت القياس الحاجة.
- reindex and reconciliation jobs.

شرط الخروج: البحث يعيد entities/services/content/datasets مع URLs canonical وفلاتر موحدة.

### المرحلة 9 — إزالة التوافق القديم

- قياس استخدام routes وAPIs القديمة.
- redirects دائمة للمسارات العامة.
- deprecation policy للـAPIs.
- إزالة fallback/seed duplication بعد التحقق.
- تحديث اسم المشروع التقني والـnamespaces فقط عبر خطة منفصلة، وليس شرطًا مبكرًا.

## 12. ترتيب الأولويات المقترح

لا يبدأ العمل بالخدمات أو Open Government قبل بناء الأساسات التالية:

1. GovernmentEntity.
2. Entity-scoped authorization.
3. Unified Content ownership.
4. Workflow and audit foundations.
5. Stable IDs/slugs/API versioning.

هذه العناصر هي قيود التوسع؛ تأجيلها سيحوّل كل وزارة جديدة إلى دين تقني ومخاطر صلاحيات.

## 13. قرارات مطلوبة قبل التنفيذ

1. هل `GovernmentEntity` هو حد tenancy الرسمي، أم توجد جهات تحتاج عدة tenants داخل الجهة؟
2. هل المسارات العامة للجهات تبقى type-based كما هو مقترح، أم نستخدم `/government/entities/{slug}` كـcanonical واحد؟ التوصية: type-based للجمهور مع resolver موحد.
3. هل توجد لغات مطلوبة غير العربية في V1؟ يجب تقرير storage strategy قبل schema.
4. ما تعريف «رئاسة مجلس الوزراء» تنظيميًا داخل الدليل، وما الجهات التابعة لها؟
5. من يملك taxonomy الوطنية للخدمات والمحتوى؟
6. هل نشر محتوى مشترك بين جهتين يحتاج موافقة الجهتين أم جهة أساسية فقط؟
7. ما متطلبات الاحتفاظ والخصوصية للبلاغات والشكاوى وبيانات التواصل؟
8. ما أنواع الملفات وحدودها ومكان التخزين المعتمد؟
9. هل citizen identity/login ضمن V1 أم يظل التتبع برمز آمن؟
10. ما SLA المتوقع للبحث والفهرسة وتحديث البيانات المفتوحة؟

## 14. حواجز التنفيذ

حتى اعتماد هذه الوثيقة:

- لا redesign أو Figma مبني على IA غير معتمدة.
- لا migrations أو حذف جداول.
- لا إنشاء route منفصل لكل وزارة.
- لا إضافة role عالمي جديد لمعالجة مشكلة تحتاج entity scope.
- لا نقل المحتوى يدويًا بين صفحات.
- لا تحويل modules إلى microservices.
- لا حذف endpoints أو الوظائف الحالية.

بعد الاعتماد، يجب تحويل القرارات إلى ADRs وقصص تنفيذ صغيرة، ويبدأ العمل بالمرحلة 1 فقط.

## 15. مرجع ملفات الجرد

- `backend/pom.xml`
- `backend/bootstrap/src/main/resources/db/migration/V1__schema.sql`
- `backend/bootstrap/src/main/resources/db/migration/V2__admin_content.sql`
- `backend/bootstrap/src/main/resources/db/migration/V3__support_requests.sql`
- `backend/bootstrap/src/main/resources/db/migration/V4__support_requests_status.sql`
- `backend/bootstrap/src/main/java/ye/gov/pmo/bootstrap/controller/PortalHomeController.java`
- `backend/bootstrap/src/main/java/ye/gov/pmo/bootstrap/controller/AdminContentController.java`
- `backend/bootstrap/src/main/java/ye/gov/pmo/bootstrap/controller/SupportRequestController.java`
- `backend/identity/src/main/java/ye/gov/pmo/identity/security/SecurityConfig.java`
- `backend/identity/src/main/java/ye/gov/pmo/identity/security/JwtService.java`
- `backend/identity/src/main/java/ye/gov/pmo/identity/entity/User.java`
- `backend/identity/src/main/java/ye/gov/pmo/identity/entity/Role.java`
- `backend/identity/src/main/java/ye/gov/pmo/identity/entity/Permission.java`
- `backend/news`, `backend/decisions`, `backend/documents`
- `frontend/src/app`
- `frontend/src/lib/api.ts`
- `frontend/src/lib/site-data.ts`

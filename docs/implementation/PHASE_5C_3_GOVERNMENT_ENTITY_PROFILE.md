# Phase 5C.3 — Government Entity Profile Contract

> **الحالة:** Implemented — owner review required
>
> **التاريخ:** 2026-08-29
>
> **نقطة البداية:** `main` بعد checkpoint `07c3333d5312cd1947210c9a09d362d701e29de5` الخاص بـPhase 5C.2
>
> **النطاق:** إغلاق الحد الأدنى لعقد Government Directory وEntity Profile فقط؛ لا Ministry UI أو Government Services أو Search أو Open Data أو Homepage activation أو Figma أو feature-flag changes
>
> **التوصية:** **READY TO COMMIT** بعد مراجعة المالك. أصبح backend profile contract جاهزًا، لكن شاشة Ministry تبقى **READY WITH CONDITIONS** لأن المستودع لا يحتوي سجل Ministry حقيقيًا موثقًا، ولا Government Services domain، ولا collections موثقة تكمل الشاشة المعتمدة.

## 1. Executive decision

اعتمدت 5C.3 نموذجًا صغيرًا يحافظ على الموجود بدل إنشاء domain موازٍ:

- UUID الحالي هو الهوية الدائمة والعلاقة المرجعية الداخلية.
- `(publicPathSegment, slug)` هو الـpublic locator المقروء.
- `canonicalPath` هو المسار الحالي الذي يجب أن تعتمد عليه الواجهة.
- تغيير slug أو entity type يحفظ locator السابق كـalias؛ لا يعاد استخدامه لجهة أخرى.
- public reads تعرض فقط جهة `ACTIVE` تنتمي إلى entity type نشط.
- `GET /api/v1/entities` بقي array وبقي صالحًا لاستهلاك Homepage الحالي.
- أضيف read model paged مستقل للدليل كي لا يُكسر العقد القديم.
- الحقول غير المتوفرة تبقى `null`؛ لم تُنشأ شعارات أو أسماء وزراء أو عناوين أو أرقام أو إحصاءات أو خدمات أو سجلات حكومية.

هذا ليس CMS جديدًا ولا نسخة من Editorial Verification. سجل الجهات master data إداري؛ بوابة النشر فيه هي `ACTIVE` مع صلاحية `entities.manage`/entity-scoped authorization والتدقيق الموجود. أضيف `officialSourceReference` اختياريًا لتثبيت مرجع رسمي قابل للمراجعة، لكن لم يُرقَّ seed الحالي ولم تُخترع له provenance.

## 2. Current implementation before 5C.3

### 2.1 Schema and records

كان جدول `government_entities` يحتوي:

- UUID `id`.
- `entity_type_id` إلى `entity_types`.
- `parent_entity_id` مباشر واختياري.
- `official_name_ar`, `short_name_ar`, `slug`, `status`.
- `description`, `website_url`.
- timestamps وactor IDs للإنشاء والتعديل.

الـstatus taxonomy الموجودة هي `DRAFT | ACTIVE | INACTIVE | ARCHIVED`. وكان uniqueness للـslug على `(entity_type_id, slug)` فقط، من دون تاريخ أو alias.

توجد أنواع عامة فعلًا ولا يفترض النظام أن كل جهة Ministry:

| code | public path |
|---|---|
| `PRIME_MINISTERS_OFFICE` | `/prime-ministers-office` |
| `MINISTRY` | `/ministries/{slug}` |
| `AUTHORITY` | `/authorities/{slug}` |
| `INDEPENDENT_ENTITY` | `/independent-entities/{slug}` |
| `GOVERNORATE` | `/governorates/{slug}` |

البيانات الحالية تحتوي سجلًا واحدًا فقط لرئاسة مجلس الوزراء. لا يوجد Ministry record موثق. seed الحالي لا يملك `created_by` أو source reference، ولذلك لا يُستخدم كدليل على اعتماد بيانات إضافية.

### 2.2 Existing API

كانت المسارات التالية موجودة وتبقى متوافقة:

- `GET /api/v1/entity-types`
- `GET /api/v1/entities`
- `GET /api/v1/entities/{uuid}`
- `GET /api/v1/entities/by-slug/{publicPathSegment}/{slug}`
- `GET /api/v1/entities/{uuid}/children`
- `POST /api/v1/admin/entities`
- `PUT /api/v1/admin/entities/{uuid}`
- `POST /api/v1/admin/entities/{uuid}/relationships`

توجد كذلك `entity_relationships` وعلاقة `primary_entity_id` في Unified Content. لم يكن هناك Government Services domain أو public relationship listing.

### 2.3 Current frontend consumption

Homepage تستخدم `GET /api/v1/entities` عبر adapter قائم، وتتوقع array وتقرأ الحقول الأساسية مثل `id`, `officialName`, `status`, `description`, و`canonicalPath`. لذلك لم يُحوّل هذا المسار إلى pagination ولم تُغير أسماء حقوله الحالية. الحقول الجديدة additive فقط.

## 3. Final entity contract

### 3.1 Public detail

```ts
type GovernmentEntityDetail = {
  id: string;                    // UUID stable identity
  locale: "ar";
  type: {
    id: number;                  // retained for backward compatibility
    code: "PRIME_MINISTERS_OFFICE" | "MINISTRY" | "AUTHORITY" |
          "INDEPENDENT_ENTITY" | "GOVERNORATE";
    nameAr: string;
    publicPathSegment: string;
  };
  officialName: string;          // official Arabic name
  officialNameEn?: string | null;
  shortName?: string | null;
  slug: string;
  canonicalPath: string;
  status: "ACTIVE";             // public endpoints only
  description?: string | null;
  mandate?: string | null;
  websiteUrl?: string | null;
  contact?: {
    email?: string | null;
    phone?: string | null;
    address?: string | null;     // Arabic official address
  } | null;
  officialSourceReference?: string | null;
  updatedAt: string;
  parent?: {
    id: string;
    officialName: string;
    canonicalPath: string;
  } | null;
};
```

الحقول المطلوبة لإنشاء record هي: `entityTypeCode`, `officialNameAr`, `slug`, `status`. بقية profile fields اختيارية لأن غياب الدليل لا يسمح باختراع value. `officialEmail` يتحقق منه بنيويًا، وتوجد bounds لجميع النصوص.

### 3.2 Directory summary

```ts
type GovernmentEntitySummary = {
  id: string;
  locale: "ar";
  type: EntityTypeReference;
  officialName: string;
  shortName?: string | null;
  slug: string;
  canonicalPath: string;
  description?: string | null;
  updatedAt: string;
};

type EntityDirectoryResponse = {
  items: GovernmentEntitySummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
```

الـsummary لا يكرر mandate/contact/source. صفحة profile تطلب detail عند الحاجة.

### 3.3 Explicitly excluded fields

لم تُضف الحقول التالية لعدم وجود مصدر موثق أو حاجة تعاقدية مكتملة:

- minister/leader profile.
- statistics أو KPI counts.
- logo/emblem/image reference.
- social links.
- invented jurisdiction/competencies.
- services أو service counts.
- media gallery.

يمكن إضافة media asset لاحقًا فقط بعد اعتماد asset source/governance، لا كـURL حر أو fallback وهمي.

## 4. Public API

### 4.1 Paged directory

```http
GET /api/v1/entity-directory
```

Query contract:

| parameter | required | rule |
|---|---:|---|
| `type` | no | entity type `code`, case-insensitive؛ يجب أن يكون type نشطًا |
| `parentId` | no | UUID لجهة عامة `ACTIVE` |
| `page` | no | default `0`, minimum `0` |
| `size` | no | default `20`, range `1..100` |

الترتيب ثابت وصريح: `officialNameAr ASC, id ASC`. لا توجد filters أو sort modes إضافية في هذا الـMVP. invalid type/page/size يعيد عقد V1 الموحد `400 VALIDATION_ERROR`.

### 4.2 Detail and locator reads

- `GET /api/v1/entities/{uuid}`: detail بالهوية الدائمة.
- `GET /api/v1/entities/by-slug/{publicPathSegment}/{slug}`: detail بالـlocator الحالي أو alias محفوظ.
- missing/inactive/type-inactive يعيد `404 RESOURCE_NOT_FOUND` دون كشف وجود السجل.
- alias lookup يعيد `200` مع `slug` و`canonicalPath` الحاليين. لا يصدر backend HTTP redirect لأن هذا endpoint JSON؛ يستطيع route adapter لاحقًا مقارنة requested path مع `canonicalPath` وتنفيذ permanent redirect.

### 4.3 Backward-compatible reads

`GET /api/v1/entities` ما زال:

- array غير paged.
- ACTIVE-only.
- مرتبًا بالاسم العربي كما كان.
- يحتفظ بكل الحقول القديمة وأسمائها.
- يضيف profile fields فقط، لذلك استهلاك Homepage الحالي لم ينكسر.

`GET /api/v1/entities/{id}/children` بقي array ويخفي child types غير النشطة. إذا كانت parent غير عامة، يعيد 404.

## 5. Admin behavior and authorization

لم تنشأ operations أو permissions جديدة:

- create يحتاج `entities.manage`.
- update يحتاج `entities.manage` أو الـentity-scoped authorization الموجود.
- actor ووقت التعديل يستمران في `created_by/updated_by` وAudit events الموجودة.
- `officialSourceReference` يقبل فقط مرجعًا رسميًا عامًا مقصودًا للنشر؛ لا يستخدم لحفظ notes داخلية أو أسرار.

إنشاء record بحالة `ACTIVE` هو قرار نشر master-data من actor مخوّل. بالنسبة للإدخال اليدوي الرسمي، actor + audit هما provenance الإدارية الدنيا؛ وللمصادر الخارجية أو المستوردة يجب تعبئة `officialSourceReference` قبل اعتماد البيانات تشغيليًا. لم يُفرض `NOT NULL` الآن كي لا يُكسر seed القديم أو clients الحالية.

## 6. Slug and redirect strategy

### 6.1 Identity and current locator

- UUID لا يتغير عند إعادة التسمية أو تغيير النوع.
- slug يقبل lowercase ASCII digits/hyphens فقط، بطول أقصى 160.
- current uniqueness يبقى `(entity_type_id, slug)`.
- public locator الكامل هو `(publicPathSegment, slug)`، لا slug وحده.

### 6.2 Aliases

أضيف جدول `government_entity_slug_aliases` يحفظ:

- entity UUID.
- historical `public_path_segment`.
- historical `slug`.
- `created_at`, `created_by`.
- unique constraint على locator التاريخي الكامل.

عند تغير slug أو type:

1. يُحجز locator الجديد.
2. يُحفظ locator القديم alias داخل transaction نفسها.
3. يمنع create/update لاحق من استخدام alias لجهة أخرى.
4. يسمح للجهة نفسها بالعودة إلى alias قديم؛ عندها يصبح locator الحالي، ويحفظ locator الذي خرجت منه alias جديدًا.

لأن uniqueness موزعة على current table وalias table، تقوم عمليات create/update بقفل pessimistic قصير لصف/صفوف `entity_types` وبترتيب ID ثابت. هذا يمنع سباق طلبين إداريين من حجز locator حالي وتاريخي متطابق داخل أكثر من application replica من دون إنشاء locator registry معقدة.

لا توجد redirect table مستقلة أو redirect chain؛ كل alias يشير مباشرة إلى entity UUID، ومن ثم إلى canonical locator الحالي.

## 7. Schema and migration

أضيفت migration مستقلة تالية لـV7:

`backend/bootstrap/src/main/resources/db/migration/V8__government_entity_profile.sql`

### 7.1 Nullable profile columns

| column | type | migration behavior |
|---|---|---|
| `official_name_en` | `varchar(255)` | null لكل السجلات الحالية |
| `mandate` | `text` | null |
| `official_email` | `varchar(320)` | null |
| `official_phone` | `varchar(80)` | null |
| `official_address_ar` | `varchar(1000)` | null |
| `official_source_reference` | `varchar(1000)` | null |

### 7.2 Alias table

الجدول الجديد additive وله FK إلى `government_entities` مع `ON DELETE CASCADE`، وFK اختياري إلى actor `users`. توجد constraints تمنع uppercase والشرطة المائلة في locator المخزن، وindex على entity UUID.

لا تعدل migration أي record، ولا تملأ Ministry، ولا تمنح source reference للسجل الحالي، ولا تغير migration سابقة.

## 8. Verification and provenance decision

لم يُنسخ نموذج 5C.2 (`UNVERIFIED/VERIFIED/REJECTED`) إلى Entity domain للأسباب التالية:

1. الجهات master data إدارية وليست revisions تحريرية.
2. دورة `DRAFT/ACTIVE/INACTIVE/ARCHIVED` القائمة تكفي للـpublic eligibility إذا كان التعديل محكومًا بالصلاحية والتدقيق.
3. نسخ verification state سيخلق workflow ثانيًا غير مطلوب من دون مالك policy مستقل.

القرار الأدنى:

- public eligibility = entity `ACTIVE` + type active.
- manual provenance = authenticated actor + audit trail.
- external/import provenance = `officialSourceReference` موثق.
- سجل حقيقي لا يصبح جاهزًا للعرض لمجرد وجوده في DB؛ onboarding checklist واعتماد مالك البيانات ما زالا مطلوبين.

## 9. Relationship strategy

### 9.1 Entity hierarchy

`parent_entity_id` يبقى العلاقة العامة الوحيدة في profile MVP. parent لا يظهر في public response إذا كان parent نفسه أو type الخاص به غير نشط.

### 9.2 Existing entity relationships

`entity_relationships` موجود ويدعم أنواع علاقات إدارية، لكن لم يُفتح public endpoint له في 5C.3 بسبب غياب records موثقة وحاجة واضحة في شاشة Ministry الأساسية. فتح كل العلاقات الآن سيعرض semantic data غير محسومة.

### 9.3 Future links

- Entity → Content: موجود تخزينيًا عبر `content_items.primary_entity_id` وعلاقات المحتوى؛ القراءة العامة تبقى ضمن verified Unified Content contract.
- Entity → Services: يجب أن يستخدم Government Service مستقبلًا owner/provider UUID؛ لم يُنشأ table أو endpoint أو fake relationship هنا.

فشل أو غياب collection مستقبلية لا يجب أن يسقط entity profile نفسها.

## 10. Frontend and Figma impact

لم يتغير Frontend أو Figma.

بعد checkpoint مستقل واعتماد بيانات حقيقية يمكن إضافة typed frontend adapter للـdetail/directory. على شاشة Ministry:

- title/type/description/mandate/contact/website تأتي من detail contract.
- الحقول الاختيارية الغائبة تختفي أو تستخدم ContentState المعتمد؛ لا placeholder facts.
- أي requested alias يمكن تحويله إلى `canonicalPath` الحالي.
- services/news/resources تبقى requests مستقلة ولا تُستنتج من entity detail.

لا تزال صورة Figma تحتوي presentation content لا يجوز نقله كproduction data.

## 11. Tests

أضيف `GovernmentEntityProfileIntegrationTest` ويغطي:

- directory paged مع type filter والترتيب/metadata.
- detail lookup والحقول الاختيارية.
- اسم عربي طويل ضمن الحد.
- missing optional fields من دون كسر response.
- legacy `GET /api/v1/entities` ما زال array ويعرض record ACTIVE.
- إخفاء INACTIVE من list/directory/detail/slug.
- missing ID وinvalid directory inputs ضمن Unified API Error Contract.
- تغيير slug وحفظ alias والوصول عبره إلى canonical locator الحالي.
- منع إعادة استخدام alias لجهة أخرى.
- عودة الجهة إلى alias سابق من دون redirect chain.
- تطبيق Flyway V8 وHibernate schema validation.

نتائج التحقق النهائية تسجل بعد اكتمال run النهائي:

| verification | result |
|---|---|
| compile/test compile | PASS |
| targeted entity + Homepage compatibility | **5/5 PASS** |
| targeted entity + error contract قبل قفل التزامن | **15/15 PASS** |
| full backend suite | **93/93 PASS** |
| Maven reactor | BUILD SUCCESS |
| feature/config diff | لا تغييرات |
| Frontend diff | لا تغييرات |

تحذيرات Mockito dynamic agent وLF/CRLF الموجودة في البيئة ليست failures ولا تغير العقد.

## 12. Source files changed

### New

- `backend/bootstrap/src/main/resources/db/migration/V8__government_entity_profile.sql`
- `backend/bootstrap/src/test/java/ye/gov/pmo/bootstrap/GovernmentEntityProfileIntegrationTest.java`
- `backend/organization/src/main/java/ye/gov/pmo/organization/dto/EntityDirectoryResponse.java`
- `backend/organization/src/main/java/ye/gov/pmo/organization/dto/GovernmentEntitySummaryResponse.java`
- `backend/organization/src/main/java/ye/gov/pmo/organization/entity/GovernmentEntitySlugAlias.java`
- `backend/organization/src/main/java/ye/gov/pmo/organization/repository/GovernmentEntitySlugAliasRepository.java`
- `docs/implementation/PHASE_5C_3_GOVERNMENT_ENTITY_PROFILE.md`

### Modified

- `backend/organization/src/main/java/ye/gov/pmo/organization/controller/GovernmentEntityController.java`
- `backend/organization/src/main/java/ye/gov/pmo/organization/dto/GovernmentEntityRequest.java`
- `backend/organization/src/main/java/ye/gov/pmo/organization/dto/GovernmentEntityResponse.java`
- `backend/organization/src/main/java/ye/gov/pmo/organization/entity/GovernmentEntity.java`
- `backend/organization/src/main/java/ye/gov/pmo/organization/repository/EntityTypeRepository.java`
- `backend/organization/src/main/java/ye/gov/pmo/organization/repository/GovernmentEntityRepository.java`
- `backend/organization/src/main/java/ye/gov/pmo/organization/service/GovernmentEntityService.java`

`backend/**/target/**` التاريخية/المولدة، صور Phase 4، و`.design-system-state-yemen-gov-foundations-v1.json` ليست ضمن 5C.3 ولم تُنظف أو تُضمّن عمدًا.

## 13. Compatibility risks

1. **Additive JSON fields:** `/api/v1/entities` وdetail يحملان fields جديدة. المستهلك الحالي tolerant ومختبر، لكن أي external consumer يرفض unknown properties يحتاج تحديثًا؛ لا توجد إشارة إلى مستهلك كهذا في المستودع.
2. **Alias resolution returns 200, not redirect:** API يعيد current `canonicalPath`. يجب على frontend route لاحقًا إصدار redirect إن كانت canonical URLs مطلوبة لمحركات البحث.
3. **Source reference is governance input:** التحقق بنيوي/طولي فقط؛ النظام لا يجلب URL ولا يثبت ملكيته. صحة المرجع مسؤولية actor وdata owner.
4. **Existing PMO seed remains unsourced:** لم يُرفع أو يُغيّر تلقائيًا. يجب مراجعته خارج هذه migration قبل اعتباره نموذجًا لonboarding جهات إضافية.
5. **No public relationship list:** هذا مقصود لحماية النطاق، لكنه يعني أن أقسام الجهات التابعة لا تزال تحتاج قرار بيانات وعقدًا مستقلًا إذا طلبتها الشاشة.
6. **No verified Ministry record:** contract صالح لكن لا توجد production data تسمح بتشغيل الصفحة النهائية.

لا توجد مخاطرة مباشرة على Unified Content canaries: لم تتغير content projections أو routing أو feature flags، وتبقى حالات NEWS/ANNOUNCEMENT/DECISION/DOCUMENT كما كانت عند نقطة البداية.

## 14. Migration and rollback considerations

- V8 forward-only وadditive؛ تطبيقها على البيانات الحالية لا يكتب values ولا يغير status/slug.
- rollback تطبيقي مؤقت ممكن مع إبقاء columns/table؛ الكود السابق يتجاهلها.
- لا يُنصح بإسقاط alias table بعد بدء تغيير slugs لأن ذلك يكسر historical locators.
- إذا فشل deployment قبل أي slug change، لا يوجد data migration لاستعادتها.
- بعد بدء alias writes، rollback يجب أن يحافظ على الجدول والبيانات حتى لو توقف resolver مؤقتًا.
- لا تتداخل V8 مع V7 أو جداول Unified Content أو future Services migration.

## 15. Ministry readiness

بعد 5C.3:

| gate | state | evidence/remaining work |
|---|---|---|
| stable entity identity | PASS | UUID موجود ومحفوظ |
| canonical slug locator | PASS | current locator + aliases |
| public list/detail | PASS | ACTIVE-only وunified errors |
| Arabic/profile fields | PASS WITH OPTIONAL DATA | contract موجود؛ values لا تُخترع |
| contact/website/mandate | PASS WITH OPTIONAL DATA | storage/DTO موجود؛ يحتاج onboarding موثق |
| Ministry record | HOLD | لا يوجد سجل Ministry حقيقي موثق |
| logo/leader/stats | HOLD / NOT REQUIRED | لا مصدر ولا contract معتمد |
| entity content | READY WITH CONDITIONS | Unified Content verified feed موجود؛ يلزم content حقيقي مرتبط بالجهة |
| entity services | HOLD | Government Services domain غير موجود |
| Ministry frontend | NOT STARTED | خارج نطاق 5C.3 |

النتيجة: **Ministry = READY WITH CONDITIONS** على مستوى الـentity profile، وليست READY كاملة للشاشة المعتمدة. يمكن لاحقًا تنفيذ skeleton/profile portions فقط بعد onboarding سجل Ministry معتمد واعتماد route/adapter، لكن الشاشة الكاملة تبقى مشروطة بـGovernment Services وبالمحتوى المرتبط الفعلي.

## 16. Final recommendation

**READY TO COMMIT** كcheckpoint مستقل لـPhase 5C.3 بعد مراجعة المالك.

- contract صغير ويعيد استخدام Government Directory الحالية.
- migration additive ومحافظة ولا تخترع بيانات.
- legacy Homepage consumption محفوظ.
- slug history مستقرة ومحمية من تعارضات الطلبات الإدارية المتوازية.
- public reads fail closed للجهات غير النشطة.
- الاختبارات المستهدفة والكاملة ناجحة.

لا تبدأ Ministry frontend أو Government Services أو Service Detail أو Search أو Open Data أو Homepage activation قبل قرار مستقل ووجود بيانات حكومية موثقة.

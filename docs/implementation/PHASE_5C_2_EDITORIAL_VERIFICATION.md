# Phase 5C.2 — Editorial Verification & Provenance

> **الحالة:** Implemented — owner review required
>
> **التاريخ:** 2026-08-29
>
> **نقطة البداية:** `main` عند `449a78b0cf4e057aaebbe52f9847814efe7719a0`
>
> **النطاق:** طبقة حوكمة صغيرة للمحتوى الموحد فقط؛ لا Frontend أو Entity Profile أو Government Services أو Search أو Open Data أو Figma أو feature-flag changes
>
> **التوصية:** **READY TO COMMIT** بعد مراجعة المالك. أصبح شرط الاعتماد التحريري قابلًا للتنفيذ تقنيًا، لكن قسم مستجدات Homepage لا يُرفع عنه الحظر فعليًا حتى تتحقق سجلات حقيقية ويُربط القسم بقراءة V1 الرسمية.

## 1. Executive decision

لم يعد `PUBLISHED` يعني أن السجل معتمد تحريريًا. صار لكل `ContentItem` قرار مستقل مرتبط بنسخة النشر نفسها:

- `publicationStatus`: دورة العمل التقنية الموجودة (`DRAFT`, `IN_REVIEW`, `APPROVED`, `PUBLISHED`, `ARCHIVED`).
- `editorialVerificationStatus`: قرار الحوكمة الجديد (`UNVERIFIED`, `VERIFIED`, `REJECTED`).

القاعدة العامة للبوابة هي:

```text
officially visible = published revision exists
                   + not archived
                   + editorial status is VERIFIED
                   + verified revision equals the currently published revision
```

أي نشر أو إعادة نشر يضبط الاعتماد إلى `UNVERIFIED`. لا يرث revision جديد اعتماد revision سابق، ولا تُرقّى seed/backfill records تلقائيًا.

## 2. Current-state findings

### 2.1 Publication and source state before 5C.2

| المجال | مصدر القراءة الحالي | تمثيل النشر | الملاحظة الحوكمية قبل 5C.2 |
|---|---|---|---|
| NEWS | legacy facade أو unified projection حسب router | legacy record أو `content_items.status/published_revision_id` | لا verification مستقل |
| ANNOUNCEMENT | legacy facade أو unified projection حسب router | نفسه | لا verification مستقل |
| DECISION | legacy حاليًا؛ unified mapping موجود | نفسه | لا verification مستقل |
| DOCUMENT | legacy حاليًا؛ unified mapping موجود | نفسه | لا verification مستقل |
| Unified V1 content | `ContentItem` + `ContentRevision` | workflow status وpublished revision | `PUBLISHED` كان كافيًا للـpublic query |

الـbackfill الحالي يستورد snapshot من `backfill/unified-content-v1.json`، وينشئ `legacy_content_mappings`، ويحفظ manifest version وlegacy source keys داخل `display_metadata`. هذه معلومات تتبع للاستيراد، لكنها ليست قرارًا تحريريًا ولا actor decision. الإدخال اليدوي يملك actor IDs ودورة نشر، لكنه كذلك لم يكن يملك verification منفصلًا.

### 2.2 Audit and authorization already available

- `AuditService` موجود ويسجل actor، action، resource، entity scope، outcome، correlation ID، وmetadata.
- `CurrentActorProvider` يوفر actor موثوقًا من سياق المصادقة.
- صلاحيات `content.publish` و`content.manage` الحالية تكفي لهذه الدفعة؛ لم يُنشأ role أو permission taxonomy جديد.
- صلاحية القرار تُفحص ضمن scope الجهة المالكة للمحتوى.

## 3. Final contract

### 3.1 Status taxonomy

```java
enum EditorialVerificationStatus {
    UNVERIFIED,
    VERIFIED,
    REJECTED
}
```

- `UNVERIFIED`: الافتراضي لكل سجل قديم أو جديد، وكذلك بعد كل publish/re-publish.
- `VERIFIED`: اعتماد رسمي للـpublished revision الحالية فقط.
- `REJECTED`: قرار رفض تحريري؛ لا provenance نشطة ولا public visibility.

لا توجد حالات pending/reviewing/expired إضافية. دورة التحرير الأساسية تبقى في `ContentStatus` ولا تُكرر هنا.

### 3.2 Provenance taxonomy

```java
enum EditorialSourceType {
    OFFICIAL_MANUAL_ENTRY,
    OFFICIAL_SOURCE_REFERENCE,
    APPROVED_IMPORT
}
```

`VERIFIED` يتطلب معًا:

- `sourceType` من الـallowlist أعلاه.
- `sourceReference` غير فارغ وبحد أقصى 1000 حرف.
- `verifiedRevisionId` مساوٍ للـpublished revision الحالية.
- `verifiedAt` و`verifiedBy` من الخادم وسياق المصادقة، لا من request العميل.

`sourceReference` ليس taxonomy بديلًا؛ هو المرجع القابل للمراجعة داخل نوع مصدر ثابت. قد يكون canonical official URL، رقم/مرجع سجل داخلي رسمي، أو معرف manifest/import معتمد. لا يكفي وجود metadata backfill القديمة وحدها لمنح `APPROVED_IMPORT`.

### 3.3 Admin operation

```http
PUT /api/v1/admin/content/{contentId}/editorial-verification
Content-Type: application/json
X-Correlation-ID: optional
```

```json
{
  "status": "VERIFIED",
  "sourceType": "OFFICIAL_SOURCE_REFERENCE",
  "sourceReference": "https://official.example/reference"
}
```

القواعد:

- `status` مطلوب.
- `VERIFIED` مسموح فقط عندما يكون المحتوى `PUBLISHED` وله published revision.
- `VERIFIED` يتطلب `sourceType` و`sourceReference`.
- `UNVERIFIED` و`REJECTED` لا يقبلان provenance؛ يمسحان حقول الاعتماد النشطة.
- actor يحتاج `content.publish` أو `content.manage` على الجهة.
- عدم وجود السجل `404`، نقص الصلاحية `403`, request غير صالح `400`, وقرار verify على محتوى غير منشور `409`، وفق عقد أخطاء V1 المعتمد في 5C.1.

`AdminContentResponse` أضيف له حقل additive فقط:

```ts
editorialVerification: {
  status: "UNVERIFIED" | "VERIFIED" | "REJECTED";
  verifiedRevisionId?: string;
  sourceType?: "OFFICIAL_MANUAL_ENTRY" | "OFFICIAL_SOURCE_REFERENCE" | "APPROVED_IMPORT";
  sourceReference?: string;
  verifiedAt?: string;
  verifiedBy?: number;
}
```

لم تتغير أي success DTO عامة أو legacy compatibility DTO.

## 4. Schema impact

أضيفت migration مستقلة:

`V7__editorial_verification_provenance.sql`

إضافات `content_items`:

| العمود | النوع | السياسة |
|---|---|---|
| `editorial_verification_status` | `varchar(20) not null` | default `UNVERIFIED` |
| `editorial_verified_revision_id` | `uuid` | FK إلى `content_revisions`; يجب أن يساوي published revision عند `VERIFIED` |
| `provenance_source_type` | `varchar(40)` | enum check محدود |
| `provenance_source_reference` | `varchar(1000)` | مطلوب وغير فارغ عند `VERIFIED` |
| `editorial_verified_at` | timestamp with time zone | server timestamp |
| `editorial_verified_by` | bigint | FK إلى `users` |

تضيف migration قيود اتساق تمنع الحالة `VERIFIED` دون revision/source/reference/actor/time، وتمنع إبقاء هذه القيم مع `UNVERIFIED` أو `REJECTED`. أضيف index للقراءة العامة حسب verification/type/publication time.

لا توجد تغييرات في جداول Entities أو Services أو legacy tables، ولا إعادة كتابة للبيانات الحالية.

## 5. Migration policy for existing data

- كل الصفوف الحالية تبدأ `UNVERIFIED` عبر default محافظة.
- الـ12 backfill mappings، وأي seed أو imported row، لا تحصل على `VERIFIED` تلقائيًا.
- وجود `display_metadata`, legacy mapping، manifest reviewed، أو status `PUBLISHED` لا يكفي.
- لا توجد data migration تخمّن source reference أو verification actor.
- اعتماد `APPROVED_IMPORT` لاحقًا قرار صريح من actor مخوّل مع مرجع import قابل للمراجعة.

هذه السياسة قد تجعل V1 official feed فارغة بعد migration، وهذا سلوك fail-closed مقصود وليس data loss. Compatibility facades تبقى متاحة وفق خطة canary الحالية.

## 6. Public API behavior

القراءات الرسمية التالية أصبحت verified-only:

- `GET /api/v1/content`
- `GET /api/v1/content/{uuid}`
- `GET /api/v1/content/by-slug/{type}/{slug}`
- أي entity-content public query يستخدم `PublicContentService`

السجل المنشور لكن غير verified:

- لا يدخل list items أو totals.
- detail يعامله كغير موجود ويرجع `404 RESOURCE_NOT_FOUND` دون كشف أنه draft/unverified.

يبقى public unified-read controller تحت feature flag الحالي ولم يُفعّل في هذه الدفعة. لذلك يصف هذا القسم سلوك القراءة عند تفعيلها لاحقًا بقرار مستقل؛ لا تغيّر 5C.2 حالة التشغيل الحية أو graduation.

الـpublic response لا يعرض actor أو provenance في هذه الدفعة؛ هذه metadata إدارية. لم تُضف fields إلى public success shape.

## 7. Unified-content and canary compatibility

فُصلت قراءتان بوضوح:

1. **Official V1 read:** تشترط verification وتستخدمها الواجهة الوطنية مستقبلًا.
2. **Compatibility/shadow read:** تقرأ published revision كما كان العقد المجمد قبل 5C.2.

`UnifiedLegacyProjectionService` و`ContentShadowComparisonService` يستخدمان الآن methods داخلية صريحة باسم `ForCompatibility`. وبذلك لا تتغير numeric IDs أو ordering أو hashes أو counts للواجهات:

- `/api/news`
- `/api/announcements`
- `/api/decisions`
- `/api/documents`

حالة config بقيت دون تعديل:

| النوع | configured route |
|---|---|
| NEWS | `UNIFIED` |
| ANNOUNCEMENT | `UNIFIED` |
| DECISION | `LEGACY` |
| DOCUMENT | `LEGACY` |

لم تتغير feature flags أو graduation state. الفصل الداخلي مقصود ومؤقت طوال فترة compatibility؛ لا يجوز أن تعتمد Homepage على facades غير المفلترة كـofficial feed.

## 8. Audit behavior

كل قرار ناجح يسجل أحد actions التالية:

- `CONTENT_EDITORIAL_VERIFIED`
- `CONTENT_EDITORIAL_REJECTED`
- `CONTENT_EDITORIAL_VERIFICATION_RESET`

يسجل الحد الأدنى من metadata: status، verified revision ID، source type، مع actor/entity/resource/time/correlation ID من audit subsystem الحالي. لا يوضع source reference الكامل في audit metadata لتقليل تكرار بيانات محتملة الحساسية؛ يبقى في السجل الإداري نفسه.

محاولة verify غير صالحة تسجل failure audit مستقلًا ثم تعيد `409`. لا يظهر exception داخلي في response.

## 9. Revision and transition behavior

- النشر يربط `publishedRevision` بالنسخة الحالية ثم يمسح أي verification سابقة إلى `UNVERIFIED`.
- إنشاء revision وحده لا يغير اعتماد النسخة المنشورة الحالية.
- إعادة نشر revision جديدة لا ترث source decision القديم.
- public query تتحقق أيضًا أن `editorialVerifiedRevision == publishedRevision`، ودعم ذلك قيد قاعدة بيانات.
- الأرشفة تبقى مانع public مستقلًا؛ الاحتفاظ بتاريخ الاعتماد داخل audit log لا يعتمد على إبقاء حقول active provenance بعد تغييرات مستقبلية.

## 10. Tests and verification

الاختبارات الجديدة/المحدثة تغطي:

- `PUBLISHED + UNVERIFIED` غير ظاهر رسميًا، مع admin state واضح.
- `VERIFIED` يحتفظ بنوع المصدر والمرجع والrevision والactor ويصبح public.
- draft لا يمكن اعتماده (`409 CONFLICT`).
- actor بلا publish/manage permission يحصل على `403 FORBIDDEN`.
- publish ثم verify ثم re-publish يعيد الحالة إلى `UNVERIFIED` حتى verify جديد.
- V1 list/detail behavior مع verified fixture.
- Flyway V7 وschema constraints ضمن integration startup.
- compatibility projection وshadow parity بعد فصل official filtering.
- عقد أخطاء 5C.1 ما زال سليمًا.

نتائج التحقق في 2026-08-29:

| التحقق | النتيجة |
|---|---|
| compile للموديولات المتأثرة | PASS |
| targeted integration suite | **24/24 PASS** |
| full backend suite | **89/89 PASS** |
| Maven reactor | BUILD SUCCESS |
| feature/config diff | لا تغييرات |
| Frontend diff | لا تغييرات |

تحذيرات Mockito dynamic-agent وLF/CRLF الموجودة في البيئة ليست test failures ولا تغير runtime contract.

## 11. Source files changed

### New

- `backend/content/src/main/java/ye/gov/pmo/content/domain/EditorialVerificationStatus.java`
- `backend/content/src/main/java/ye/gov/pmo/content/domain/EditorialSourceType.java`
- `backend/content/src/main/java/ye/gov/pmo/content/domain/InvalidEditorialVerificationException.java`
- `backend/content/src/main/java/ye/gov/pmo/content/dto/EditorialVerificationRequest.java`
- `backend/bootstrap/src/main/resources/db/migration/V7__editorial_verification_provenance.sql`
- `backend/bootstrap/src/test/java/ye/gov/pmo/bootstrap/EditorialVerificationIntegrationTest.java`
- `docs/implementation/PHASE_5C_2_EDITORIAL_VERIFICATION.md`

### Modified

- `backend/content/src/main/java/ye/gov/pmo/content/entity/ContentItem.java`
- `backend/content/src/main/java/ye/gov/pmo/content/dto/AdminContentResponse.java`
- `backend/content/src/main/java/ye/gov/pmo/content/controller/UnifiedContentAdminController.java`
- `backend/content/src/main/java/ye/gov/pmo/content/repository/ContentItemRepository.java`
- `backend/content/src/main/java/ye/gov/pmo/content/service/AdminContentService.java`
- `backend/content/src/main/java/ye/gov/pmo/content/service/PublicContentService.java`
- `backend/bootstrap/src/main/java/ye/gov/pmo/bootstrap/compatibility/UnifiedLegacyProjectionService.java`
- `backend/bootstrap/src/main/java/ye/gov/pmo/bootstrap/shadow/ContentShadowComparisonService.java`
- `backend/bootstrap/src/test/java/ye/gov/pmo/bootstrap/UnifiedContentAuthoringIntegrationTest.java`
- `backend/bootstrap/src/test/java/ye/gov/pmo/bootstrap/UnifiedContentReadIntegrationTest.java`

`backend/**/target/**` التاريخية/المولدة، صور المراجعة، وملف design-system state ليست ضمن تنفيذ 5C.2 ولم تُنظف.

## 12. Compatibility risks

1. **V1 official feed becomes fail-closed:** بعد تطبيق V7 لن تظهر records الحالية في `/api/v1/content` حتى verify صريح. هذا هو الهدف الحوكمي، لكنه يحتاج runbook تحريري قبل تفعيل مستهلك عام.
2. **Compatibility bypass must remain private:** methods ذات suffix `ForCompatibility` ضرورية لحماية canary المجمد، لكنها لا تصلح لمستهلكات البوابة الجديدة ويجب إزالتها فقط ضمن deprecation/cutover معتمد لاحقًا.
3. **No verification expiry:** لا توجد periodic review أو expiration في هذه الدفعة. هذا قرار مؤجل وليس افتراضًا صامتًا.
4. **Reference validation is structural, not remote:** النظام يتحقق من النوع/الوجود/الطول، ولا يجلب URL أو يثبت ملكيته. صحة المرجع مسؤولية actor والتحرير.
5. **Existing publisher authority is reused:** لم تُنشأ صلاحية verification مستقلة. إذا احتاجت الحوكمة فصل publisher عن verifier، فهذا تغيير authorization مستقل يحتاج اعتمادًا لاحقًا.

## 13. Migration and rollback considerations

- Flyway migration forward-only ومستقلة عن domains اللاحقة.
- rollback تطبيقي آمن نسبيًا عبر إعادة code السابق مع إبقاء الأعمدة الإضافية؛ لا يلزم حذف data أو خفض schema بصورة فورية.
- لا يُنصح بإسقاط V7 بعد استخدامها، لأن ذلك يحذف provenance وactor decisions.
- عند rollback تشغيلي مؤقت يجب تعطيل أي مستهلك يعتمد على verified-only semantics، وإبقاء audit records.
- لا تغير migration الـlegacy tables أو compatibility flags، لذلك fallback/canary rollback mechanics تبقى كما هي.

## 14. Homepage readiness

بعد 5C.2:

- **الحاجز التقني للاعتماد التحريري: CLOSED.** أصبح ممكنًا اعتماد سجل منشور بعينه مع provenance وactor وrevision binding.
- **حالة البيانات الفعلية: ما زالت HOLD.** لم تُعتمد تلقائيًا أي News/Announcement seed أو backfill record.
- **قسم المستجدات: READY WITH CONDITIONS، وليس GO تلقائيًا.** يمكن رفع HOLD عنه فقط بعد:
  1. مراجعة واعتماد records حقيقية عبر operation الإدارية.
  2. وجود سجل verified واحد على الأقل مناسب للعرض.
  3. ربط Frontend adapter بـV1 verified feed، لا compatibility facade أو `site-data.ts`.
  4. validation للمحتوى العربي، empty/error states، والـordering.
  5. اعتماد مالك المحتوى للعرض الفعلي.

لا يتغير HOLD الخاص بـGovernment Services أو Unified Search أو Open Data، ولا يكتمل Ministry من هذه الدفعة.

## 15. Final recommendation

**READY TO COMMIT** كcheckpoint مستقل لـPhase 5C.2 بعد مراجعة المالك.

- العقد صغير ومفصول عن publication workflow.
- migration محافظة ولا تثق بأي existing record تلقائيًا.
- public V1 fail-closed، بينما canary compatibility محفوظة ومختبرة.
- authorization/audit يعيدان استخدام البنية الحالية.
- الاختبارات المستهدفة والكاملة ناجحة.

لا تبدأ 5C.3 ولا تعتمد مستجدات Homepage فعليًا قبل governance/data onboarding صريح.

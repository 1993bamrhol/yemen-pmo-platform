# Phase 1 Implementation Plan — Platform Foundation

> **الحالة:** Implemented and verified locally — staging gates pending  
> **التاريخ:** 2026-08-23  
> **يعتمد على:** `UNIFIED_GOVERNMENT_PORTAL_IA_V1.md` وADRs المقترحة في `adr/`  
> **سجل التنفيذ:** نُفذت الأساسات في 2026-08-23. راجع `PHASE_1_IMPLEMENTATION_RECORD.md` للأثر الفعلي ونتائج التحقق والبوابات المتبقية قبل النشر.

## 1. الهدف

بناء الحد الأدنى الآمن الذي يسمح بإضافة جهات حكومية متعددة دون نسخ تطبيقات أو منح صلاحيات عالمية. تنتهي المرحلة بوجود Government Directory قابل للقراءة، وسجل PMO كأول جهة، ونموذج scoped authorization مثبت بالاختبارات.

## 2. نتائج المرحلة

عند اكتمال Phase 1 يجب أن نستطيع:

1. إنشاء جهة حكومية من نوع معروف وبهوية ثابتة وslug.
2. تمثيل parent/child وعلاقات التبعية دون hardcoding.
3. قراءة دليل الجهات عبر `/api/v1/entities`.
4. منح مستخدم دورًا داخل جهة محددة.
5. إثبات أن مستخدم Entity A لا يستطيع تنفيذ عمليات إدارية على Entity B.
6. تمثيل PMO كجهة فعلية مع إبقاء الوظائف الحالية عاملة.
7. تسجيل عمليات إدارة الجهات والتعيينات الحساسة في audit trail أولي.

## 3. خارج النطاق

- redesign أو Figma.
- إعادة بناء الصفحة الرئيسية الوطنية.
- Unified Content migration.
- Service Catalog وLife Events.
- Open Government.
- Citizen Engagement migration.
- Unified Search.
- فصل microservices.
- حذف `user_roles`, `admin_content`, أو endpoints الحالية.
- تغيير namespace `ye.gov.pmo` في هذه المرحلة.

## 4. القرارات المعتمدة

| القرار | التوصية | أثر التأجيل |
|---|---|---|
| نوع ID للكيانات الجديدة | UUID | معتمد |
| uniqueness للـslug | فريد داخل `entityType` مع redirect history لاحقًا | معتمد |
| هل PMO نوع مستقل؟ | نعم: `PRIME_MINISTERS_OFFICE` | معتمد |
| source of truth للصلاحيات | RoleAssignment من DB، مع cache قصير اختياري | معتمد |
| من يحق له إنشاء جهة؟ | Platform permission فقط | معتمد |
| هل Entity Admin يدير assignments؟ | داخل جهته ولأدوار مسموحة فقط | معتمد |

## 5. Workstreams

### WS1 — Organization domain

الموضع: `backend/organization`.

المخرجات المستقبلية:

- `GovernmentEntity`
- `EntityType`
- `EntityRelationship`
- repositories وapplication services
- public read DTOs
- validation للـslug والحالة والعلاقات الدائرية

قواعد أساسية:

- لا controller في `bootstrap` يحتوي domain logic.
- لا يعتمد `organization` على `identity` لتنفيذ القراءة العامة.
- العلاقة مع المستخدمين تكون بالـIDs وعقود واضحة لتجنب circular module dependency.

### WS2 — Scoped identity

الموضع: `backend/identity`.

المخرجات المستقبلية:

- `RoleAssignment`
- `ScopeType`
- assignment repository/service
- `EntityAuthorizationPolicy`
- APIs إدارية للتعيينات وفق privilege boundaries
- compatibility mapping من `user_roles`

قواعد منع التصعيد:

- Entity Admin لا يمنح Platform scope.
- Entity Admin لا يمنح role أعلى من allowlist محددة.
- المستخدم لا يعدل assignment الخاص به.
- كل create/revoke assignment يسجل AuditEvent.
- التعطيل أو انتهاء `validUntil` يمنع الوصول فورًا أو ضمن cache SLA معتمد.

### WS3 — API V1 contracts

Public read APIs:

```text
GET /api/v1/entities
GET /api/v1/entities/{id}
GET /api/v1/entities/by-slug/{type}/{slug}
GET /api/v1/entities/{id}/children
```

Admin APIs المقترحة:

```text
POST  /api/v1/admin/entities
PUT   /api/v1/admin/entities/{id}
POST  /api/v1/admin/entities/{id}/relationships
GET   /api/v1/admin/entities/{id}/assignments
POST  /api/v1/admin/entities/{id}/assignments
DELETE /api/v1/admin/entities/{id}/assignments/{assignmentId}
```

لا تُنفذ mutation APIs قبل توفر policy واختبارات العزل.

### WS4 — Audit foundation

حد أدنى في Phase 1:

- actor user ID
- action
- resource type/ID
- entity scope
- timestamp
- success/failure outcome
- correlation ID
- metadata محدودة لا تحتوي أسرارًا أو PII غير ضرورية

العمليات الأولى المسجلة:

- إنشاء أو تعديل جهة.
- إضافة أو إزالة علاقة جهة.
- منح أو سحب RoleAssignment.
- محاولة cross-entity مرفوضة.

### WS5 — Compatibility and PMO bootstrap

- إنشاء PMO entity بصورة migration/seed idempotent بعد اعتماد schema.
- ربط admin الحالي بـPMO scoped assignment.
- إبقاء `PMO_ADMIN` و`user_roles` فعالين مؤقتًا.
- عدم تغيير frontend routes الحالية في Phase 1.
- إضافة feature flag أو configuration لتفعيل scoped authorization تدريجيًا إن لزم.

## 6. Schema proposal للمراجعة فقط

لا ينفذ هذا القسم migrations.

### `entity_types`

| الحقل | النوع المقترح | القيود |
|---|---|---|
| id | smallint أو UUID | PK |
| code | varchar(50) | unique, immutable |
| name_ar | varchar(150) | not null |
| public_path_segment | varchar(80) | unique |
| active | boolean | not null |

### `government_entities`

| الحقل | النوع المقترح | القيود |
|---|---|---|
| id | UUID | PK |
| entity_type_id | FK | not null |
| parent_entity_id | UUID FK | nullable |
| official_name_ar | varchar(255) | not null |
| short_name_ar | varchar(150) | nullable |
| slug | varchar(160) | not null |
| status | varchar(30) | constrained enum |
| description | text | nullable |
| website_url | varchar(500) | nullable |
| created_at/updated_at | timestamptz | not null |
| created_by/updated_by | user FK أو UUID | auditable |

القيود والفهارس:

- unique `(entity_type_id, slug)`.
- index على `parent_entity_id`, `status`, و`entity_type_id`.
- منع `parent_entity_id = id`.
- فحص cycle في application service، مع اختبار concurrency مناسب.

### `entity_relationships`

| الحقل | النوع المقترح |
|---|---|
| id | UUID |
| source_entity_id | UUID FK |
| target_entity_id | UUID FK |
| relationship_type | constrained varchar |
| valid_from/valid_to | date nullable |
| created_at/created_by | audit fields |

unique فعال للعلاقة نفسها، ومنع self-relation.

### `role_assignments`

| الحقل | النوع المقترح | القيود |
|---|---|---|
| id | UUID | PK |
| user_id | bigint FK حاليًا | not null |
| role_id | bigint FK حاليًا | not null |
| scope_type | varchar(20) | PLATFORM أو ENTITY |
| government_entity_id | UUID FK | required for ENTITY, null for PLATFORM |
| valid_from/valid_until | timestamptz | nullable |
| enabled | boolean | not null |
| granted_by | bigint FK | not null |
| created_at | timestamptz | not null |

يجب فرض check constraint يطابق scope مع entity ID، وفهرس على `(user_id, enabled)` و`government_entity_id`.

### `audit_events`

يبدأ كجدول append-only. لا update/delete من application APIs العادية.

## 7. API response draft

```json
{
  "id": "5d6f7a6d-66d8-49f1-b957-7b39c173a3dd",
  "type": {
    "code": "MINISTRY",
    "name": "وزارة",
    "pathSegment": "ministries"
  },
  "officialName": "وزارة الصحة العامة والسكان",
  "shortName": "وزارة الصحة",
  "slug": "health",
  "canonicalPath": "/ministries/health",
  "status": "ACTIVE",
  "parent": null
}
```

Pagination response لا يبتكر صيغة محلية إذا كانت هناك convention ستعتمد لبقية `/api/v1`. يجب تثبيت convention في ADR/API guidelines قبل أول endpoint.

## 8. Authorization matrix الأولية

| الفعل | Platform Super Admin | Entity Admin | Editor | Reviewer | Publisher | Service Manager |
|---|---:|---:|---:|---:|---:|---:|
| إنشاء جهة | نعم | لا | لا | لا | لا | لا |
| تعديل ملف الجهة | نعم | داخل الجهة | لا | لا | لا | لا |
| إدارة مستخدمي الجهة | نعم | داخل allowlist | لا | لا | لا | لا |
| قراءة draft content | نعم حسب السياسة | داخل الجهة | داخل الجهة | داخل الجهة | داخل الجهة | خدمات الجهة فقط |
| إنشاء draft | حسب السياسة | داخل الجهة | داخل الجهة | لا | لا | خدمات فقط |
| Review | حسب السياسة | حسب الدور الإضافي | لا | داخل الجهة | لا | لا |
| Publish | حسب السياسة | حسب الدور الإضافي | لا | لا | داخل الجهة | لا |

هذه المصفوفة baseline وليست بديلًا لتعريف permissions granular.

## 9. ترتيب التنفيذ المقترح

### Slice 1 — Contracts and tests first

- تثبيت enums والأسماء وعقود DTO.
- كتابة اختبارات policy المطلوبة كاختبارات فاشلة أولًا.
- توثيق API errors وpagination.

### Slice 2 — Read-only Government Directory

- domain + persistence بعد اعتماد migration.
- PMO seed idempotent.
- public GET APIs.
- لا admin mutations بعد.

### Slice 3 — RoleAssignment and policy

- scoped assignments.
- authorization evaluator.
- compatibility مع PMO admin.
- اختبارات cross-entity.

### Slice 4 — Controlled admin mutations

- entity management permissions.
- assignment grant/revoke مع anti-escalation.
- audit events.

### Slice 5 — Operational hardening

- pagination/limits.
- structured logs وcorrelation IDs.
- performance indexes والتحقق من query plans.
- backup/restore and rollback rehearsal في staging.

## 10. Test strategy

### Unit

- slug normalization والقيود.
- entity relationship validation.
- scope evaluation.
- role grant allowlist.

### Repository

- entity filters لا تعيد موارد جهة أخرى.
- uniqueness والـcheck constraints.
- expired/disabled assignments.

### Integration/security

- anonymous يستطيع GET للجهات ACTIVE فقط.
- Platform Admin ينشئ جهة.
- Entity Admin يعدل جهته فقط.
- Entity Admin A يتلقى 403 عند محاولة تعديل B.
- query IDs غير المعروفة تعيد 404 دون تسريب معلومات.
- revoked assignment يفقد الوصول ضمن SLA المعتمد.
- endpoints القديمة تبقى ناجحة.

### Regression

- اختبارات identity الحالية.
- Portal home APIs الحالية.
- admin login/content/support الحالية.
- frontend build وlint.

## 11. Security and privacy gates

- threat model خاص بـcross-tenant access وIDOR.
- عدم تسجيل JWT أو كلمات المرور أو PII في audit metadata.
- فحص كل endpoint إداري لوجود entity scope.
- rate limits وmonitoring للـadmin/login لاحقًا ضمن hardening.
- مراجعة سياسة Platform Super Admin وbreak-glass access.
- منع mass assignment للحقول الحساسة في DTOs.

## 12. Rollout strategy

1. migrations additive فقط بعد اعتماد منفصل.
2. نشر الكود مع feature flag للـscoped enforcement إن احتاج التوافق.
3. seed PMO وbackfill admin الحالي في transaction/idempotent job.
4. تشغيل reconciliation report قبل التفعيل.
5. تفعيل read APIs.
6. تفعيل enforcement في staging ثم production تدريجيًا.
7. مراقبة 401/403 والأخطاء وaudit coverage.

الـrollback يعطل feature flag ويعود للمسار القديم دون حذف الجداول الجديدة. لا تستخدم rollback بحذف البيانات.

## 13. Definition of Done

- ADRs الخاصة بالمرحلة Accepted.
- migrations راجعها شخصان على الأقل وشُغلت على نسخة staging.
- اختبارات cross-entity تمر وتغطي read/write/grant/revoke.
- PMO ممثل كـGovernmentEntity.
- لا route أو وظيفة حالية محذوفة.
- APIs الجديدة versioned وموثقة.
- audit يسجل العمليات الحساسة المحددة.
- build/tests/lint ناجحة.
- لا findings حرجة في security review الخاص بـIDOR والعزل.
- وثائق التشغيل والrollback محدثة.

## 14. بوابة الموافقة التالية

قبل بدء implementation يلزم:

1. قبول أو تعديل ADR-0001 إلى ADR-0005.
2. حسم القرارات الستة في القسم 4.
3. اعتماد حدود Phase 1 وDefinition of Done.
4. تفويض صريح لكتابة migrations وكود الإنتاج.

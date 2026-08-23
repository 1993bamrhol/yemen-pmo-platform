# Phase 1 Implementation Record — Platform Foundation

> **الحالة:** Implemented and verified locally  
> **التاريخ:** 2026-08-23  
> **المرجع:** `UNIFIED_GOVERNMENT_PORTAL_IA_V1.md` و`PHASE_1_IMPLEMENTATION_PLAN.md`

## ما تم تنفيذه

- migration إضافية `V5__platform_foundation.sql` دون حذف أو تعديل الجداول القديمة.
- `EntityType`, `GovernmentEntity`, parent/child و`EntityRelationship` داخل `backend/organization`.
- PMO كجهة فعلية ثابتة الهوية، مع أنواع الجهات ومساراتها العامة الأساسية.
- Public Government Directory APIs تحت `/api/v1`.
- mutation APIs لإنشاء/تعديل الجهات، وإنشاء العلاقات المركزية.
- `RoleAssignment` بنطاقي `PLATFORM` و`ENTITY`، وفترات صلاحية وتعطيل.
- policy تمنع الإدارة العابرة للجهات وتطبق allowlist للأدوار التي يمنحها مدير الجهة.
- seed idempotent للأدوار والصلاحيات، مع إبقاء `PMO_ADMIN` و`user_roles` للتوافق.
- audit trail لإنشاء/تعديل الجهات، إنشاء العلاقات، منح/سحب التعيينات، ومحاولات الوصول المرفوضة.

## عقود API المنفذة

```text
GET    /api/v1/entity-types
GET    /api/v1/entities
GET    /api/v1/entities/{id}
GET    /api/v1/entities/by-slug/{type}/{slug}
GET    /api/v1/entities/{id}/children
POST   /api/v1/admin/entities
PUT    /api/v1/admin/entities/{id}
POST   /api/v1/admin/entities/{id}/relationships
GET    /api/v1/admin/entities/{id}/assignments
POST   /api/v1/admin/entities/{id}/assignments
DELETE /api/v1/admin/entities/{id}/assignments/{assignmentId}
```

## قواعد الأمان المثبتة

- إنشاء الجهات والعلاقات محصور في `entities.manage` على مستوى المنصة.
- مدير الجهة يستطيع تعديل الجهة المعيّن فيها فقط.
- مدير الجهة يستطيع قراءة ومنح وسحب assignments داخل جهته فقط وللأدوار المسموحة.
- لا يستطيع المستخدم سحب assignment الخاص به.
- assignments المنتهية أو المعطلة لا تدخل في authorities الفعالة.
- رفض cross-entity يسجل `ENTITY_ACCESS_DENIED` في `audit_events`.

## التحقق المنفذ

- `mvn -pl bootstrap -am test`: **BUILD SUCCESS**.
- 14 اختبارًا في identity و32 اختبارًا في bootstrap؛ صفر failures وصفر errors.
- Flyway طبّق migrations من V1 إلى V5 على H2، وHibernate `ddl-auto=validate` نجح.
- الاختبار التكاملي الجديد يغطي: القراءة العامة، إنشاء جهتين، إنشاء علاقة، تعديل الجهة المسموحة، رفض تعديل جهة أخرى، رفض علاقة غير مركزية، grant/revoke داخل الجهة، ورفض قراءة assignments لجهة أخرى.

## تحقق PostgreSQL/Docker المحلي

نُفذ في 2026-08-23 على stack المشروع الفعلي:

- PostgreSQL `16.15` والحاوية بحالة healthy.
- ترقية قاعدة موجودة من Flyway V4 إلى V5 بنجاح؛ migration واحدة طُبقت دون إعادة إنشاء القاعدة.
- `ddl-auto=validate` وبدء Spring Boot نجحا بعد الترقية.
- `/actuator/health` أعاد `UP`.
- `/api/v1/entities` أعاد PMO بالهوية الثابتة والمسار `/prime-ministers-office`.
- `/api/v1/entity-types` أعاد الأنواع الخمسة المعتمدة.
- تسجيل دخول المدير نجح وأعاد `PLATFORM_SUPER_ADMIN`، وPMO assignment أصبح قابلًا للقراءة.
- frontend production container أعاد HTTP 200.
- أُنشئت نسخة `pg_dump` بصيغة custom، واستعيدت في قاعدة مؤقتة منفصلة؛ احتوت schema V5 وسجل PMO وRoleAssignments، ثم حُذفت القاعدة والنسخة المؤقتتان فقط.
- لم تُحذف جداول أو بيانات من قاعدة التشغيل.

## التوافق

- لم تُحذف route أو وظيفة حالية.
- لم تُحذف `user_roles` أو `admin_content`.
- لا تغيير على frontend routes ضمن هذه المرحلة.
- مدير PMO الحالي يحصل على assignments الجديدة مع إبقاء الدور العالمي القديم مؤقتًا.

## بوابات ما قبل production

التنفيذ مكتمل في المستودع، لكن النشر للإنتاج يتطلب خارج هذا التغيير:

1. مراجعة migration من شخص ثانٍ وتشغيلها على staging خارجي حديث.
2. تجهيز صورة إصدار سابقة versioned لإجراء application rollback rehearsal؛ لا تُحذف جداول V5 عند الرجوع.
3. مراجعة خطط الاستعلام بأحجام بيانات ممثلة للإنتاج.
4. threat-model/IDOR review مستقل، ومراجعة صلاحيات break-glass.
5. مراقبة 401/403 وaudit coverage بعد تفعيل staging.

## الخطوة المعمارية التالية

Phase 2 المقترحة هي Unified Government Content: نقل الأخبار والقرارات والإعلانات والوثائق تدريجيًا إلى `ContentItem` مركزي مرتبط بـ`governmentEntityId`، مع إبقاء APIs الحالية عبر compatibility adapters أثناء الانتقال.

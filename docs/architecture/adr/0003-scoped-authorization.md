# ADR-0003: الصلاحيات عبر Scoped Role Assignments

- **الحالة:** Accepted
- **التاريخ:** 2026-08-23
- **المرجع:** `../UNIFIED_GOVERNMENT_PORTAL_IA_V1.md`

## السياق

العلاقة الحالية `user_roles` عالمية، بينما المطلوب أدوار متكررة عبر الجهات مثل Editor وReviewer وPublisher. إنشاء role مختلف لكل وزارة سيؤدي إلى تضخم وصعوبة مراجعة الصلاحيات.

## القرار

فصل تعريف الدور عن منحه:

- `Role` يعرف مجموعة permissions.
- `RoleAssignment` يمنح الدور لمستخدم في نطاق `PLATFORM` أو `ENTITY`.
- عند `ENTITY` يجب وجود `governmentEntityId`.
- authorization يحتاج permission صحيحة ونطاقًا يطابق resource.

```text
ALLOW = permissionGranted
        AND (platformScopeAllowsAction OR assignedEntityIds contains resource.entityId)
```

## الأدوار القياسية

- Platform Super Admin
- PMO Admin
- Entity Admin
- Editor
- Reviewer
- Publisher
- Service Manager

`PMO Admin` ليس صلاحية مركزية تلقائيًا؛ هو Entity Admin لجهة PMO، ويمكن منحه assignment مركزيًا منفصلًا عند الحاجة.

## التطبيق

- policy/service مركزي في backend يقيّم الفعل والنطاق.
- repository methods الإدارية تستعلم ضمن entity scope ولا تعتمد على filter لاحق فقط.
- method security تستدعي policy مثل `@entityAuthorization.canWrite(entityId)` بدل `content.write` وحدها.
- الواجهة تخفي الأفعال غير المتاحة لتحسين UX فقط؛ backend هو مصدر القرار.
- العمليات المركزية الحساسة تسجل في `AuditEvent`.

## JWT

لا يُنصح بتضمين جميع entity assignments في JWT إذا كان عددها متغيرًا أو يجب إلغاؤها فورًا. يحتفظ token بهوية المستخدم ومعلومات جلسة محدودة، وتُقرأ التعيينات الحالية من مصدر موثوق أو cache قصير العمر. يتطلب اختيار الصيغة النهائية تقييم الأداء والإلغاء.

## التوافق

تبقى `user_roles` مؤقتًا أثناء الترحيل. يُعامل الدور القديم `PMO_ADMIN` كتعيين PMO scoped عند backfill، ولا يحذف حتى اجتياز اختبارات التوافق.

## اختبارات إلزامية

- Editor في Entity A يستطيع إنشاء draft في A.
- Editor في A لا يستطيع قراءة draft أو تعديله في B.
- Reviewer لا ينشر ما لم يمتلك permission النشر.
- Platform Super Admin لا يتجاوز separation of duties تلقائيًا إلا بسياسة صريحة.
- تغيير assignment ينعكس دون انتظار صلاحية JWT قديم طويلة.

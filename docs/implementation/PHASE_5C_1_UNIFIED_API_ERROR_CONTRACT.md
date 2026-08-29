# Phase 5C.1 — Unified API Error Contract

> **الحالة:** Implemented locally — owner review required
>
> **التاريخ:** 2026-08-29
>
> **النطاق:** أخطاء Controllers الحالية تحت `/api/v1/**` فقط؛ لا تغيير في success DTOs أو البيانات أو schema أو migrations أو feature flags أو compatibility routing

## 1. Decision

تعتمد واجهات V1 الحالية envelope واحدًا للأخطاء. تم قصر MVC contract على Controllers الموسومة بـ`@ApiV1`، بينما تعالج طبقة Spring Security طلبات `401/403` عندما يبدأ المسار بـ`/api/v1/`. تبقى واجهات compatibility القديمة تحت `/api/news`, `/api/announcements`, `/api/decisions`, و`/api/documents` دون تغيير في body أو status أو routing.

لا يوجد correlation/request-id infrastructure موحد حاليًا، ولذلك لم يُضف حقل شكلي غير موثوق. يمكن إضافته لاحقًا فقط بعد توفير filter يضمن معرفًا واحدًا في الطلب والسجلات والاستجابة.

## 2. Response schema

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "المورد المطلوب غير موجود.",
  "status": 404,
  "path": "/api/v1/entities/ffffffff-ffff-ffff-ffff-ffffffffffff",
  "timestamp": "2026-08-29T11:00:00Z"
}
```

`details` يظهر فقط في أخطاء field validation التي لديها تفاصيل آمنة:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "تعذر قبول الطلب. تحقق من القيم المرسلة.",
  "status": 400,
  "path": "/api/v1/admin/entities",
  "timestamp": "2026-08-29T11:00:00Z",
  "details": [
    {
      "field": "officialNameAr",
      "reason": "NOT_BLANK",
      "message": "يجب ألا يكون الحقل فارغًا."
    }
  ]
}
```

قواعد الحقول:

| Field | Contract |
|---|---|
| `code` | رمز آلي ثابت من الجدول أدناه، ولا يعتمد على نص الاستثناء |
| `message` | رسالة عربية آمنة عامة؛ لا تحمل تفاصيل DB أو workflow أو auth |
| `status` | HTTP status نفسه كرقم |
| `path` | request URI دون query string |
| `timestamp` | UTC ISO-8601 instant |
| `details` | اختياري؛ لا يظهر عندما لا توجد field errors آمنة |

## 3. Status and code mapping

| HTTP | Stable code | استخدامه الحالي |
|---:|---|---|
| 400 | `VALIDATION_ERROR` | malformed JSON، binding/type failures، Bean Validation، وطلبات business غير الصالحة التي كانت 400 أصلًا |
| 401 | `UNAUTHORIZED` | غياب/فشل المصادقة قبل دخول Controller |
| 403 | `FORBIDDEN` | فشل authorization في filter أو method security |
| 404 | `RESOURCE_NOT_FOUND` | resource غير موجود مع الحفاظ على 404 الأصلي |
| 409 | `CONFLICT` | `ResponseStatusException` أو domain exception موسوم `@ResponseStatus(CONFLICT)` |
| 422 | `UNPROCESSABLE_ENTITY` | حالة معالجة غير قابلة للتنفيذ إذا استخدمها contract لاحقًا |
| 500+ | `INTERNAL_ERROR` | خطأ داخلي؛ يحفظ status الأصلي إن كان exception صريحًا ويمنع تسريب التفاصيل |
| 4xx أخرى | `REQUEST_ERROR` | fallback محدود مع الحفاظ على status الأصلي |

أسباب field validation المعتمدة حاليًا: `NOT_BLANK`, `NOT_NULL`, `SIZE`, `PATTERN`, `EMAIL`, `MIN`, `MAX`, و`INVALID` كـfallback. لا تُعاد rejected values أو regex أو النص الخام للاستثناء.

## 4. Controller boundary

الـControllers الحالية الموسومة بـ`@ApiV1`:

- Government Entity public/admin endpoints.
- Unified Content public/admin endpoints.
- Role Assignment admin endpoints.
- Content backfill reconciliation/apply endpoints.
- Compatibility status وshadow comparison endpoints.

كل Controller جديد تحت `/api/v1/**` يجب أن يحمل `@ApiV1`. هذه العلامة تمنع توسيع العقد تلقائيًا إلى APIs القديمة وتوفر نقطة مراجعة صريحة عند إضافة domain جديد.

## 5. Security and disclosure rules

- لا stack traces أو Java class names أو SQL/JDBC/DB messages أو secrets أو rejected values في response.
- لا يعاد سبب auth الخام أو صلاحيات المستخدم المفقودة.
- تفاصيل الاستثناءات غير المتوقعة تكتب في server logs فقط وتعود للعميل كـ`INTERNAL_ERROR` آمن.
- استثناءات المجال الموسومة بـ`@ResponseStatus` تحتفظ بالـHTTP status الأصلي، لكن لا يعود نصها الخام.
- لا يغيّر هذا العقد قواعد permit/authentication/authorization الحالية.

## 6. Frontend guidance and compatibility

الـfrontend الحالي في `frontend/src/lib/api.ts` يبني `ApiError` من `response.status` فقط ولا يقرأ error body، ولذلك لا يحتاج تعديلًا لهذه الدفعة ولا يوجد كسر في consumer حالي. في دفعة لاحقة يمكن توسيع `ApiError` اختياريًا إلى `{ status, code, message, details }` مع fallback آمن للواجهات القديمة أو للاستجابات غير JSON.

مخاطر التوافق:

- أجسام أخطاء V1 السابقة كانت غير موحدة وقد تتغير للعميل الذي اعتمد عليها خارج هذا المستودع. الـstatus codes لم تتغير.
- legacy compatibility endpoints لم تُوسم ولم تتغير، حفاظًا على canary hashes والعقود الحالية.
- unmatched routes التي لا تُحل إلى Controller ليست ضمن annotation-scoped MVC advice؛ وقد تعالجها قواعد الأمن/Boot الافتراضية. هذا slice يوحد أخطاء الموارد والطلبات التي تصل إلى V1 Controllers الحالية، وليس routing fallback عالميًا.
- `details` اختيارية عمدًا؛ على العميل ألا يفترض وجودها.

## 7. Verification

أضيف `ApiV1ErrorContractIntegrationTest` لتغطية فعلية عبر MockMvc وH2/Flyway:

- malformed JSON: 400؛
- Bean Validation مع field identifiers/reasons آمنة: 400؛
- unauthenticated protected V1 route: 401؛
- authenticated without authority: 403؛
- missing Government Entity: 404؛
- invalid Unified Content request: 400، لإثبات الاتساق عبر domain ثانٍ؛
- annotated domain conflict: 409 مع الحفاظ على status؛
- explicit unprocessable request: 422؛
- unexpected internal exception: 500 دون تسريب JDBC/SQL detail.

نتيجة التحقق المحلي:

- الاختبار الموجه: 10 اختبارات، 0 failures، 0 errors؛
- كامل reactor عبر `mvn test`: 85 اختبارًا (14 identity + 6 content + 65 bootstrap)، 0 failures، 0 errors؛
- compile لجميع modules اللازمة لـbootstrap: PASS؛
- `git diff --check` على المصدر المقصود مع استبعاد `backend/**/target/**`: PASS؛ ظهرت تنبيهات LF/CRLF المعروفة فقط.
- فحص routing محلي read-only عند `2026-08-29T16:15:47Z`: `comparisonError=null`، وكل الأنواع `shadowReady=true`؛ NEWS وANNOUNCEMENT بقيا `UNIFIED`، وDECISION وDOCUMENT بقيا `LEGACY`، مع 0 automatic fallbacks للجميع.

اختبارات compatibility القديمة بقيت ناجحة، وهي الحارس لعدم تغيير success responses والعقود الرقمية.

## 8. Explicit non-changes

- لا database أو migration أو records.
- لا feature flags أو compatibility counters/routing logic.
- لا تغيير لحالة NEWS أو ANNOUNCEMENT أو DECISION أو DOCUMENT.
- لا editorial/entity/services/search/open-data contract implementation.
- لا frontend production change.
- لا cleanup لـ`backend/**/target/**` التاريخية.
- لا deployment أو commit أو push.

## 9. Recommendation

**READY TO COMMIT** فقط إذا نجحت الاختبارات الموجهة ومجموعة backend المتأثرة، وبقي diff خاليًا من config/flag/schema/frontend changes. لا يجيز ذلك بدء slice أخرى دون مراجعة المالك.

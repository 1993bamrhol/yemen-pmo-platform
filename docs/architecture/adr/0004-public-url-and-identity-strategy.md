# ADR-0004: URLs نوعية للجهات وهوية داخلية ثابتة

- **الحالة:** Accepted
- **التاريخ:** 2026-08-23
- **المرجع:** `../UNIFIED_GOVERNMENT_PORTAL_IA_V1.md`

## السياق

المستخدم يتوقع روابط واضحة مثل `/ministries/health`، بينما النظام يحتاج هوية ثابتة لا تنكسر عند إعادة التسمية أو تغيير النوع التنظيمي.

## القرار

- استخدام type-based public routes مثل `/ministries/{slug}` و`/authorities/{slug}`.
- استخدام قالب صفحة واحد وentity resolver مشترك لكل الأنواع.
- استخدام UUID أو ID ثابت داخل النظام والـadmin APIs.
- حفظ slug history وإنشاء redirects دائمة عند تغييره.
- استخدام canonical URL واحدة لكل مورد.
- استخدام `/api/v1` للعقود الجديدة.

## تغيير نوع الجهة

إذا تغيرت جهة من Authority إلى Ministry:

1. يتغير canonical path.
2. يسجل المسار السابق في redirect history.
3. لا تتغير هوية السجل أو علاقاته أو محتواه.

## التوافق

- تبقى routes الرقمية الحالية فعالة خلال الترحيل.
- بعد توفر slugs، تعيد صفحات `/news/{id}` وما يماثلها redirect إلى canonical slug URL.
- لا تُحذف API قديمة قبل إصدار deprecation معلن وقياس العملاء.

## البدائل

- `/government/entities/{slug}` أبسط تقنيًا لكنه أقل وضوحًا للمواطن؛ يمكن توفيره كـresolver داخلي أو redirect فقط.
- UUID داخل URL العام مستقر لكنه غير مقروء وغير مناسب للـSEO.

# ADR-0005: مخزن محتوى حكومي موحد مع Compatibility Facades

- **الحالة:** Accepted
- **التاريخ:** 2026-08-23
- **المرجع:** `../UNIFIED_GOVERNMENT_PORTAL_IA_V1.md`

## السياق

المحتوى الحالي موجود في fallback للواجهة وقوائم Java ثابتة وجدول `admin_content`. لا توجد علاقة بين ما تعدله الإدارة وما تعرضه صفحات الأخبار والقرارات والوثائق.

## القرار

إنشاء `ContentItem` مركزي مع:

- `contentType`
- `primaryGovernmentEntityId`
- title/summary/body وmetadata النوع
- lifecycle status مضبوط
- current revision وعلاقات revisions
- publication timestamps
- taxonomy وattachments
- علاقات جهات إضافية عند المحتوى المشترك

News وAnnouncement وDecision وDocument تصبح read views أو typed projections فوق السجل الموحد، لا مخازن مستقلة متكررة.

## العرض

السجل المنشور نفسه يمكن أن يظهر في:

- الصفحة الوطنية؛
- صفحة الجهة؛
- قائمة نوع المحتوى؛
- نتائج البحث؛
- related content blocks.

لا يُنشأ سجل جديد لكل placement.

## الترحيل

1. إنشاء النموذج الجديد بصورة additive بعد اعتماد migrations.
2. backfill `admin_content` وبيانات PMO الحالية.
3. جعل `/api/news`, `/api/decisions`, وغيرها compatibility facades.
4. نقل الواجهة إلى `/api/v1/content` تدريجيًا.
5. إزالة القوائم الثابتة بعد reconciliation.

## النتائج

- مصدر حقيقة واحد وworkflow موحد.
- يلزم تصميم subtype metadata بعناية لتجنب جدول عام بلا قواعد.
- الملفات الكبيرة لا تخزن داخل جدول المحتوى؛ تخزن metadata ومرجع object storage.

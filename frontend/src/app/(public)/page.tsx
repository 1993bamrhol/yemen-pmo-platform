import { Suspense } from "react";

import {
  CardGrid,
  ContentState,
  GovernmentEntityCard,
} from "@/components/content";
import {
  PageContainer,
  Section,
  SectionHeader,
} from "@/components/layout";
import { Badge, SearchField, TextLink } from "@/components/ui";
import { api } from "@/lib/api";

import styles from "./Homepage.module.css";

function SearchUnavailable() {
  return (
    <div className={styles.searchUnavailable}>
      <SearchField
        disabled
        helperText="سيُفعّل البحث بعد اعتماد واجهة بيانات موثقة."
        id="government-search"
        label="البحث الحكومي الموحد"
        placeholder="البحث غير متاح حاليًا"
      />
    </div>
  );
}

function GovernmentEntitiesLoading() {
  return (
    <ContentState
      itemCount={2}
      label="جارٍ تحميل الجهات الحكومية"
      state="loading"
    />
  );
}

async function GovernmentEntitiesContent() {
  const result = await loadGovernmentEntities();

  if (!result.ok) {
    return (
      <ContentState
        description="تعذر الوصول إلى دليل الجهات. يمكن متابعة بقية الصفحة بصورة مستقلة."
        state="error"
        title="تعذر تحميل الجهات الحكومية"
      />
    );
  }

  const activeEntities = result.data.filter(
    (entity) =>
      entity.status === "ACTIVE" &&
      typeof entity.officialName === "string" &&
      entity.officialName.trim().length > 0,
  );

  if (!activeEntities.length) {
    return (
      <ContentState
        description="لم يُرجع دليل الجهات أي جهة عامة قابلة للعرض."
        state="empty"
        title="لا توجد جهات متاحة حاليًا"
      />
    );
  }

  return (
    <CardGrid>
      {activeEntities.map((entity) => (
        <GovernmentEntityCard
          entityType={entity.type?.name || undefined}
          key={entity.id}
          name={entity.officialName}
          summary={entity.description || undefined}
        />
      ))}
    </CardGrid>
  );
}

async function loadGovernmentEntities() {
  try {
    return {
      data: await api.getGovernmentEntities(),
      ok: true as const,
    };
  } catch {
    return { ok: false as const };
  }
}

export default function HomePage() {
  return (
    <>
      <section aria-labelledby="homepage-title" className={styles.hero}>
        <PageContainer className={styles.heroContainer}>
          <div className={styles.heroCopy}>
            <span aria-hidden="true" className={styles.nationalMark}>
              <span className={styles.nationalMarkRed} />
              <span className={styles.nationalMarkBlue} />
            </span>
            <h1 className={styles.heroTitle} id="homepage-title">
              بوابتك إلى المعلومات الحكومية المتاحة
            </h1>
            <p className={styles.heroDescription}>
              تصفح المعلومات والمسارات العامة المرتبطة بمصادر تشغيلية متاحة في
              المنصة.
            </p>
            <SearchUnavailable />
          </div>
          <span aria-hidden="true" className={styles.heroMotif} />
        </PageContainer>
      </section>

      <Section aria-labelledby="services-heading" spacing="roomy">
        <SectionHeader
          description="سيظهر دليل الخدمات هنا بعد اعتماد مصدر بيانات حكومي موثوق."
          headingId="services-heading"
          title="خدمات حكومية سريعة"
        />
        <ContentState
          description="لا توجد واجهة بيانات معتمدة للخدمات الحكومية في هذا الإصدار، لذلك لم تُعرض بطاقات توضيحية."
          state="empty"
          title="دليل الخدمات غير متاح حاليًا"
        />
      </Section>

      <Section
        aria-labelledby="entities-heading"
        className={styles.entitiesSection}
        spacing="roomy"
        tone="subtle"
      >
        <SectionHeader
          description="الجهات التي يعيدها دليل الجهات العام بحالتها النشطة فقط."
          headingId="entities-heading"
          title="الجهات الحكومية"
        />
        <Suspense fallback={<GovernmentEntitiesLoading />}>
          <GovernmentEntitiesContent />
        </Suspense>
      </Section>

      <Section aria-labelledby="updates-heading" spacing="roomy">
        <SectionHeader
          description="لن يظهر محتوى في هذا القسم قبل اكتمال الاعتماد التحريري لمصدره."
          headingId="updates-heading"
          title="آخر المستجدات الرسمية"
        />
        <ContentState
          description="واجهات المحتوى متاحة تقنيًا، لكن عناصرها الحالية لم تُعتمد بعد كمحتوى رسمي صالح للنشر."
          state="empty"
          title="المستجدات غير متاحة في هذا الإصدار"
        />
      </Section>

      <Section
        aria-labelledby="open-government-heading"
        className={styles.openGovernmentSection}
        spacing="roomy"
        tone="subtle"
      >
        <SectionHeader
          headingId="open-government-heading"
          title="حكومة أكثر انفتاحًا وقربًا"
        />
        <div className={styles.engagementGrid}>
          <article className={styles.engagementPanel}>
            <Badge emphasis="outline">قيد الإتاحة</Badge>
            <h3 className={styles.engagementTitle}>البيانات والتقارير الحكومية</h3>
            <p className={styles.engagementDescription}>
              لم يُعتمد بعد كتالوج بيانات أو تقارير عامة يمكن ربطه بهذا القسم.
            </p>
          </article>

          <article className={styles.engagementPanel}>
            <Badge emphasis="outline" tone="success">
              مسار متاح
            </Badge>
            <h3 className={styles.engagementTitle}>شارك بملاحظتك أو استفسارك</h3>
            <p className={styles.engagementDescription}>
              استخدم النموذج العام لإرسال ملاحظة أو استفسار إلى المنصة.
            </p>
            <TextLink href="/complaints">الانتقال إلى نموذج المشاركة</TextLink>
          </article>
        </div>
      </Section>
    </>
  );
}

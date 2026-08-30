import { ContentState } from "@/components/content";
import { Section } from "@/components/layout";
import { TextLink } from "@/components/ui";

import styles from "./ServiceDetail.module.css";

export default function ServiceDetailNotFound() {
  return (
    <Section aria-labelledby="service-not-found-title" spacing="roomy">
      <h1 className={styles.stateTitle} id="service-not-found-title">
        الخدمة غير متاحة
      </h1>
      <ContentState
        action={<TextLink href="/services">العودة إلى دليل الخدمات</TextLink>}
        description="قد لا تكون الخدمة موجودة، أو لم تستوفِ بعد شروط النشر والاعتماد العام."
        state="empty"
        title="تعذر العثور على خدمة عامة بهذا الرابط"
      />
    </Section>
  );
}

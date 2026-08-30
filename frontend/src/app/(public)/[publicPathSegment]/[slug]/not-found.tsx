import { ContentState } from "@/components/content";
import { Section } from "@/components/layout";
import { TextLink } from "@/components/ui";

import styles from "./EntityProfile.module.css";

export default function EntityProfileNotFound() {
  return (
    <Section aria-labelledby="entity-not-found-title" spacing="roomy">
      <h1 className={styles.stateTitle} id="entity-not-found-title">
        الجهة غير متاحة
      </h1>
      <ContentState
        action={<TextLink href="/">العودة إلى الصفحة الرئيسية</TextLink>}
        description="قد لا تكون الجهة موجودة بهذا الرابط، أو لا تكون حالتها مؤهلة للعرض العام."
        state="empty"
        title="تعذر العثور على جهة حكومية عامة بهذا الرابط"
      />
    </Section>
  );
}

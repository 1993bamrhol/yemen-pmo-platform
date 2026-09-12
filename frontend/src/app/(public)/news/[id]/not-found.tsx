import { ContentState } from "@/components/content";
import { Section } from "@/components/layout";
import { TextLink } from "@/components/ui";

import styles from "./NewsDetail.module.css";

export default function NewsDetailNotFound() {
  return (
    <Section aria-labelledby="news-not-found-title" spacing="roomy">
      <h1 className={styles.stateTitle} id="news-not-found-title">
        الخبر غير متاح
      </h1>
      <ContentState
        action={<TextLink href="/">العودة إلى الصفحة الرئيسية</TextLink>}
        description="قد لا يكون الخبر موجودًا، أو لم يستوفِ شروط النشر والاعتماد العام."
        state="empty"
        title="تعذر العثور على خبر رسمي بهذا الرابط"
      />
    </Section>
  );
}

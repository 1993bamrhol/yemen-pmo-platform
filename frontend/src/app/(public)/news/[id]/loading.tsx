import { ContentState } from "@/components/content";
import { Section } from "@/components/layout";

import styles from "./NewsDetail.module.css";

export default function NewsDetailLoading() {
  return (
    <Section aria-labelledby="news-loading-title" spacing="roomy">
      <h1 className={styles.visuallyHidden} id="news-loading-title">
        جارٍ تحميل الخبر
      </h1>
      <ContentState
        className={styles.loadingState}
        itemCount={2}
        label="جارٍ تحميل الخبر الرسمي"
        state="loading"
      />
    </Section>
  );
}

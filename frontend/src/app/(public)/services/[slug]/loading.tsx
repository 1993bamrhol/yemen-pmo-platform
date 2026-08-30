import { ContentState } from "@/components/content";
import { Section } from "@/components/layout";

import styles from "./ServiceDetail.module.css";

export default function ServiceDetailLoading() {
  return (
    <Section aria-labelledby="service-loading-title" spacing="roomy">
      <h1 className={styles.visuallyHidden} id="service-loading-title">
        جارٍ تحميل تفاصيل الخدمة
      </h1>
      <ContentState
        className={styles.loadingState}
        itemCount={2}
        label="جارٍ تحميل تفاصيل الخدمة الحكومية"
        state="loading"
      />
    </Section>
  );
}

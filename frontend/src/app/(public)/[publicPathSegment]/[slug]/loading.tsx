import { ContentState } from "@/components/content";
import { Section } from "@/components/layout";

import styles from "./EntityProfile.module.css";

export default function EntityProfileLoading() {
  return (
    <Section aria-labelledby="entity-loading-title" spacing="roomy">
      <h1 className={styles.visuallyHidden} id="entity-loading-title">
        جارٍ تحميل بيانات الجهة الحكومية
      </h1>
      <ContentState
        className={styles.loadingState}
        itemCount={3}
        label="جارٍ تحميل ملف الجهة الحكومية"
        state="loading"
      />
    </Section>
  );
}
